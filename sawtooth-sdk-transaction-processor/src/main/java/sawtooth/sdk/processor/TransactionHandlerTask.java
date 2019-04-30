/* Copyright 2019 Hyperledger Sawtooth Contributors
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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import sawtooth.sdk.messaging.Stream;
import sawtooth.sdk.processor.exceptions.InternalError;
import sawtooth.sdk.processor.exceptions.InvalidTransactionException;
import sawtooth.sdk.protobuf.Message;
import sawtooth.sdk.protobuf.TpProcessRequest;
import sawtooth.sdk.protobuf.TpProcessResponse;

/**
 * A runnable task to execute a TransactionProcessorHandler for a single
 * transaction.
 */
public class TransactionHandlerTask implements Runnable {

  /** Logging class for this processor. */
  private static final Logger LOGGER = Logger.getLogger(TransactionHandlerTask.class.getName());

  /** Message this task with handle. */
  private final Message message;

  /** Stream this task will use to communicate. */
  private final Stream stream;

  /** TP Handler this task will use. */
  private final TransactionHandler handler;

  /**
   * Create a TransactionHandler task.
   * @param msg         The message to be processed
   * @param replyStream The stream on which the handler will respond
   * @param txHandler   the handler
   */
  public TransactionHandlerTask(final Message msg, final Stream replyStream, final TransactionHandler txHandler) {
    this.message = msg;
    this.stream = replyStream;
    this.handler = txHandler;
  }

  @Override
  public final void run() {
    try {
      TpProcessRequest transactionRequest = TpProcessRequest.parseFrom(message.getContent());
      Context state = new StreamContext(stream, transactionRequest.getContextId());

      TpProcessResponse.Builder builder = TpProcessResponse.newBuilder();
      try {
        handler.apply(transactionRequest, state);
        builder.setStatus(TpProcessResponse.Status.OK);
      } catch (InvalidTransactionException ite) {
        LOGGER.log(Level.FINE, "Invalid Transaction: " + ite.toString());
        builder.setStatus(TpProcessResponse.Status.INVALID_TRANSACTION);
        builder.setMessage(ite.getMessage());
        if (ite.getExtendedData() != null) {
          builder.setExtendedData(ByteString.copyFrom(ite.getExtendedData()));
        }
      } catch (InternalError ie) {
        LOGGER.log(Level.WARNING, "Internal Error: " + ie.toString());
        builder.setStatus(TpProcessResponse.Status.INTERNAL_ERROR);
        builder.setMessage(ie.getMessage());
        if (ie.getExtendedData() != null) {
          builder.setExtendedData(ByteString.copyFrom(ie.getExtendedData()));
        }
      }
      stream.sendBack(Message.MessageType.TP_PROCESS_RESPONSE, message.getCorrelationId(),
          builder.build().toByteString());

    } catch (InvalidProtocolBufferException ipbe) {
      LOGGER.log(Level.INFO, "Received Bytestring that wasn't requested that isn't TransactionProcessRequest", ipbe);
    }
  }
}
