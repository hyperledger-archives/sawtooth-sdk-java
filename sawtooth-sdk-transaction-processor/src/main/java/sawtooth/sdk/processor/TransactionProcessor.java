/* Copyright 2016, 2017 Intel Corporation
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
------------------------------------------------------------------------------*/

package sawtooth.sdk.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import sawtooth.sdk.messaging.Future;
import sawtooth.sdk.messaging.Stream;
import sawtooth.sdk.messaging.ZmqStream;
import sawtooth.sdk.processor.exceptions.ValidatorConnectionError;
import sawtooth.sdk.protobuf.Message;
import sawtooth.sdk.protobuf.PingResponse;
import sawtooth.sdk.protobuf.TpProcessRequest;
import sawtooth.sdk.protobuf.TpRegisterRequest;
import sawtooth.sdk.protobuf.TpUnregisterRequest;
import sawtooth.sdk.protobuf.TransactionHeader;

/** Sawtooth transaction processor. */
public class TransactionProcessor implements Runnable {

  /** Logging class for this processor. */
  private static final Logger LOGGER = Logger.getLogger(TransactionProcessor.class.getName());

  /**
   * Default time to wait for the threads to shutdown.
   */
  public static final long DEFAULT_SHUTDOWN_TIMEOUT = 30;

  /** Streaming class for this processor. */
  private Stream stream;

  /** List of transaction handlers for this processor. */
  private Map<String, Map<String, TransactionHandler>> handlers;

  /** Whether or not this processor has been registered. */
  private AtomicBoolean registered;

  /** Flag signifying whether or not this thread should keep running. */
  private AtomicBoolean keepRunning;

  /** An ExecutorService for this processor. */
  private ExecutorService executorService;

  /** The current maxOccupancy of this processor. */
  private int maxOccupancy;

  /**
   * Send an unregister request to the validator.
   * @throws InterruptedException     thread has been interrupted
   * @throws TimeoutException         unregister request has timed out.
   * @throws ValidatorConnectionError lost connection with the validator
   */
  private void unregister() throws InterruptedException, TimeoutException, ValidatorConnectionError {
    if (this.registered.get()) {
      TpUnregisterRequest unregisterRequest = TpUnregisterRequest.newBuilder().build();
      LOGGER.info("Send TpUnregisterRequest");
      Future fut = this.stream.send(Message.MessageType.TP_UNREGISTER_REQUEST, unregisterRequest.toByteString());
      fut.getResult(1);
      this.registered.compareAndSet(true, false);
    }
  }

  /**
   * constructor.
   * @param address the zmq address
   */
  public TransactionProcessor(final String address) {
    this(new ZmqStream(address));
  }

  /**
   * TransactionProcessor with a custom Stream implementation primarily for unit
   * testing at this time.
   * @param customStream The custom stream implementation
   */
  public TransactionProcessor(final Stream customStream) {
    this.stream = customStream;
    this.handlers = Collections.synchronizedMap(new HashMap<>());
    this.registered = new AtomicBoolean(false);
    this.keepRunning = new AtomicBoolean(true);
    this.setMaxOccupancy(Runtime.getRuntime().availableProcessors());
    this.executorService = Executors.newWorkStealingPool(getMaxOccupancy());
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        stopProcessor();
      }
    });
  }

  /**
   * Finish processing any remaining messages.
   * @throws TimeoutException timeout waiting for stream.receive(1)
   */
  private void flushMessages() throws TimeoutException {
    Message message = stream.receive(1);
    LOGGER.info("Finish processing any left over messages");
    while (message != null) {
      if (message.getMessageType() == Message.MessageType.TP_PROCESS_REQUEST) {
        submitTaskForMessage(message);
      }
      message = stream.receive(1);
    }
  }

  /**
   * add a handler that will be run from within the run method.
   * @param handler implements that TransactionHandler interface
   */
  public final void addHandler(final TransactionHandler handler) {
    String version = handler.getVersion();
    String family = handler.transactionFamilyName();
    Map<String, TransactionHandler> newFamilyMap = Collections.synchronizedMap(new HashMap<>());
    Map<String, TransactionHandler> currentFamilyMap = this.handlers.putIfAbsent(family, newFamilyMap);
    if (currentFamilyMap == null) {
      currentFamilyMap = newFamilyMap;
    }
    TransactionHandler currentHandler = currentFamilyMap.get(version);
    if (currentHandler == null || !currentHandler.equals(handler)) {
      // never heard of this family, this version of the family before, or this is a
      // different
      // handler for this family and version
      this.registered.compareAndSet(true, false);
      currentFamilyMap.put(version, handler);
    }
  }

  /**
   * Getter for maxOccupancy.
   * @return the current maxOccupancy of this processor
   */
  public final int getMaxOccupancy() {
    return maxOccupancy;
  }

  /**
   * Setter for maxOccupancy.
   * @param max maximum parallelism currently for this processor
   */
  public final void setMaxOccupancy(final int max) {
    this.maxOccupancy = max;
  }

  /**
   * Signal that this thread should stop.
   */
  public final void stopProcessor() {
    try {
      unregister();
    } catch (InterruptedException exc) {
      LOGGER.log(Level.INFO, "Interrupted while unregistering", exc);
    } catch (TimeoutException exc) {
      LOGGER.log(Level.INFO, "Timeout while unregistering", exc);
    } catch (ValidatorConnectionError exc) {
      LOGGER.log(Level.INFO, "Connection error while unregistering", exc);
    }
    this.keepRunning.compareAndSet(true, false);
  }

  /**
   * Find the handler that should be used to process the given message.
   * @param message The message that has the TpProcessRequest that the header that
   *                will be checked against the handler.
   * @return the handler that should be used to processor the given message
   */
  public final TransactionHandler findHandler(final Message message) {
    try {
      TpProcessRequest transactionRequest = TpProcessRequest.parseFrom(message.getContent());
      TransactionHeader header = transactionRequest.getHeader();
      String familyName = header.getFamilyName();
      String familyVersion = header.getFamilyVersion();
      if (handlers.containsKey(familyName)) {
        Map<String, TransactionHandler> familyHandlers = handlers.get(familyName);
        if (familyHandlers.containsKey(familyVersion)) {
          return familyHandlers.get(familyVersion);
        }
      }
      LOGGER.info("Missing handler for header: " + header.toString());
    } catch (InvalidProtocolBufferException ipbe) {
      LOGGER.log(Level.INFO, "Received Message is not a TransactionProcessRequest", ipbe);
    }
    return null;
  }

  /**
   * Find a handler for this message and submit a new task for it. If no handler
   * is found ignore and return.
   * @param message message to process
   */
  private void submitTaskForMessage(final Message message) {
    TransactionHandler handler = findHandler(message);
    if (handler == null) {
      // No handler available for this message, noop
      return;
    }
    TransactionHandlerTask task = new TransactionHandlerTask(message, this.stream, handler);
    executorService.submit(task);
  }

  /**
   * Respond to a ping request.
   * @param pingMessage incoming ping
   */
  private void respondToPing(final Message pingMessage) {
    LOGGER.log(Level.FINE, "Received Ping Message");
    PingResponse pingResponse = PingResponse.newBuilder().build();
    this.stream.sendBack(Message.MessageType.PING_RESPONSE, pingMessage.getCorrelationId(),
        pingResponse.toByteString());
  }

  @Override
  public final void run() {
    while (keepRunning.get()) {
      registerHandlers();
      Message currentMessage;
      if (!this.handlers.isEmpty()) {
        currentMessage = this.stream.receive();
        if (currentMessage != null) {
          if (currentMessage.getMessageType() == Message.MessageType.PING_REQUEST) {
            respondToPing(currentMessage);
          } else if (currentMessage.getMessageType() == Message.MessageType.TP_PROCESS_REQUEST) {
            submitTaskForMessage(currentMessage);
          } else {
            LOGGER.info("Unknown Message Type: " + currentMessage.getMessageType());
          }
        } else {
          // Disconnect
          LOGGER.info("The Validator disconnected, trying to register.");
          this.registered.compareAndSet(true, false);
        }
      }
    }
    try {
      flushMessages();
      executorService.shutdown();
      executorService.awaitTermination(DEFAULT_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
    } catch (TimeoutException exc) {
      // We are exiting so log the exception and go away
      LOGGER.log(Level.FINE, exc.getMessage());
    } catch (InterruptedException exc) {
      LOGGER.log(Level.FINER, "Interrupted during shutdown", exc);
    }
  }

  /**
   * Send a registration request for all handlers in this processor.
   */
  private void registerHandlers() {
    if (!this.registered.get()) {
      for (String family : this.handlers.keySet()) {
        Map<String, TransactionHandler> handlerMap = this.handlers.get(family);
        for (TransactionHandler handler : handlerMap.values()) {
          TpRegisterRequest registerRequest = TpRegisterRequest.newBuilder().setFamily(handler.transactionFamilyName())
              .addAllNamespaces(handler.getNameSpaces()).setVersion(handler.getVersion()).build();
          LOGGER.info(String.format("Registering handlers family=%s version=%s", handler.transactionFamilyName(),
              handler.getVersion()));
          try {
            Future fut = this.stream.send(Message.MessageType.TP_REGISTER_REQUEST, registerRequest.toByteString());
            fut.getResult();
            this.registered.compareAndSet(false, true);
          } catch (InterruptedException ie) {
            LOGGER.log(Level.WARNING, "Interrupted while registering TransactionHandler", ie);
          } catch (ValidatorConnectionError vce) {
            LOGGER.log(Level.WARNING, vce.toString());
          }
        }
      }
    }
  }
}
