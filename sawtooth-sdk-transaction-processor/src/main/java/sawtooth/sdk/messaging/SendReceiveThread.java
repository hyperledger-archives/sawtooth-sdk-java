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

package sawtooth.sdk.messaging;

import com.google.protobuf.InvalidProtocolBufferException;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZLoop;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import sawtooth.sdk.processor.exceptions.ValidatorConnectionError;

import sawtooth.sdk.protobuf.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An internal messaging implementation used by the Stream class.
 */
class SendReceiveThread implements Runnable {

  /**
   * The address to connect to.
   */
  private String url;

  /**
   * The ZMQ dealer socket that will connect to the validator's Router socket.
   */
  private ZMQ.Socket socket;

  /**
   * Lock associated with the Condition.
   */
  private Lock lock = new ReentrantLock();

  /**
   * Condition to wait for setup to happen.
   */
  private Condition condition = lock.newCondition();

  /**
   * Futures to be resolved.
   */
  private ConcurrentHashMap<String, Future> futures;

  /**
   * Incoming messages.
   */
  private LinkedBlockingQueue<MessageWrapper> receiveQueue;

  /**
   * The Zeromq context.
   */
  private ZContext context;

  /**
   * Constructor.
   * @param address  The address to connect to.
   * @param hashMap  The futures to resolve.
   * @param receiver The incoming messages.
   */
  SendReceiveThread(final String address, final ConcurrentHashMap<String, Future> hashMap,
      final LinkedBlockingQueue<MessageWrapper> receiver) {
    super();
    this.url = address;
    this.futures = hashMap;
    this.receiveQueue = receiver;
    this.context = null;
  }

  /**
   * Inner class for passing messages.
   */
  class MessageWrapper {
    /**
     * The protobuf Message.
     */
    private Message message;

    /**
     * Constructor.
     * @param msg The protobuf Message.
     */
    MessageWrapper(final Message msg) {
      this.message = msg;
    }

    /**
     * Return the Message associated with this MessageWrapper.
     * @return Message the message.
     */
    public Message getMessage() {
      return message;
    }
  }

  /**
   * DisconnectThread is run to handle the validator disconnecting on the other
   * side of the ZMQ connection.
   */
  private class DisconnectThread extends Thread {

    /**
     * Queue to put new messages on.
     */
    private LinkedBlockingQueue<MessageWrapper> receiveQueue;

    /**
     * Futures to be resolved.
     */
    private ConcurrentHashMap<String, Future> futures;

    /**
     * Constructor.
     * @param receiver The queue that receives new messages.
     * @param hashMap  The futures that will be resolved.
     */
    DisconnectThread(final LinkedBlockingQueue<MessageWrapper> receiver,
        final ConcurrentHashMap<String, Future> hashMap) {
      this.receiveQueue = SendReceiveThread.this.receiveQueue;
      this.futures = SendReceiveThread.this.futures;
    }

    /**
     * Put a key and associated value in the futures.
     * @param key   correlation id
     * @param value the future.
     */
    void putInFutures(final String key, final Future value) {
      this.futures.put(key, value);
    }

    /**
     * Clear the receiveQueue of all messages, in anticipation of sending an error
     * message.
     */
    void clearReceiveQueue() {
      this.receiveQueue.clear();
    }

    /**
     * Put a message in the ReceiveQueue.
     * @param wrapper The message wrapper.
     * @throws InterruptedException An Interrupt happened during the method call.
     */
    void putInReceiveQueue(final MessageWrapper wrapper) throws InterruptedException {
      this.receiveQueue.put(wrapper);
    }

    /**
     * Return an enumeration of the coorelation ids.
     * @return coorelation ids.
     */
    ConcurrentHashMap.KeySetView<String, Future> getFuturesKeySet() {
      return this.futures.keySet();
    }

  }

  /**
   * Inner class for receiving messages.
   */
  private class Receiver implements ZLoop.IZLoopHandler {

    /**
     * The futures that will be resolved.
     */
    private ConcurrentHashMap<String, Future> futures;

    /**
     * The threadsafe queue that new messages will be put on.
     */
    private LinkedBlockingQueue<MessageWrapper> receiveQueue;

    /**
     * Constructor.
     * @param hashMap  The futures that will be resolved.
     * @param receiver The new messages that will get added to.
     */
    Receiver(final ConcurrentHashMap<String, Future> hashMap, final LinkedBlockingQueue<MessageWrapper> receiver) {
      this.futures = hashMap;
      this.receiveQueue = receiver;
    }

    @Override
    public int handle(final ZLoop loop, final ZMQ.PollItem item, final Object arg) {
      ZMsg msg = ZMsg.recvMsg(item.getSocket());
      Iterator<ZFrame> multiPartMessage = msg.iterator();

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      while (multiPartMessage.hasNext()) {
        ZFrame frame = multiPartMessage.next();
        try {
          byteArrayOutputStream.write(frame.getData());
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
      try {
        Message message = Message.parseFrom(byteArrayOutputStream.toByteArray());
        if (this.futures.containsKey(message.getCorrelationId())) {
          Future future = this.futures.get(message.getCorrelationId());
          future.setResult(message.getContent());
          this.futures.remove(message.getCorrelationId(), future);
        } else {
          MessageWrapper wrapper = new MessageWrapper(message);
          this.receiveQueue.put(wrapper);
        }
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      } catch (InvalidProtocolBufferException ipe) {
        ipe.printStackTrace();
      } catch (ValidatorConnectionError vce) {
        vce.printStackTrace();
      }

      return 0;
    }
  }

  @Override
  public void run() {
    this.context = new ZContext();
    socket = this.context.createSocket(ZMQ.DEALER);
    socket.monitor("inproc://monitor.s", ZMQ.EVENT_DISCONNECTED);
    final ZMQ.Socket monitor = this.context.createSocket(ZMQ.PAIR);
    monitor.connect("inproc://monitor.s");
    new DisconnectThread(this.receiveQueue, this.futures) {
      @Override
      public void run() {
        while (true) {
          // blocks until disconnect event recieved
          ZMQ.Event event = ZMQ.Event.recv(monitor);
          if (event.getEvent() == ZMQ.EVENT_DISCONNECTED) {
            try {
              MessageWrapper disconnectMsg = new MessageWrapper(null);
              for (String key : this.getFuturesKeySet()) {
                Future future = new FutureError();
                this.putInFutures(key, future);
              }
              this.clearReceiveQueue();
              this.putInReceiveQueue(disconnectMsg);
            } catch (InterruptedException ie) {
              ie.printStackTrace();
            }
          }
        }
      }
    }.start();

    socket.setIdentity((this.getClass().getName() + UUID.randomUUID().toString()).getBytes());
    socket.connect(url);
    lock.lock();
    try {
      condition.signalAll();
    } finally {
      lock.unlock();
    }
    ZLoop eventLoop = new ZLoop(this.context);
    ZMQ.PollItem pollItem = new ZMQ.PollItem(socket, ZMQ.Poller.POLLIN);
    eventLoop.addPoller(pollItem, new Receiver(futures, receiveQueue), new Object());
    eventLoop.start();
  }

  /**
   * Used by the Stream class to send a message.
   * @param message protobuf Message
   */
  public final void sendMessage(final Message message) {
    lock.lock();
    try {
      if (socket == null) {
        condition.await();
      }
    } catch (InterruptedException ie) {
      ie.printStackTrace();
    } finally {
      lock.unlock();
    }
    ZMsg msg = new ZMsg();
    msg.add(message.toByteString().toByteArray());
    msg.send(socket);
  }

  /**
   * Ends the zmq communication.
   */
  public void stop() {
    this.socket.close();
    this.context.destroy();
  }

}
