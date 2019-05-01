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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import sawtooth.sdk.messaging.Stream;
import sawtooth.sdk.processor.exceptions.InternalError;
import sawtooth.sdk.processor.exceptions.InvalidTransactionException;
import sawtooth.sdk.protobuf.Message;
import sawtooth.sdk.protobuf.Message.MessageType;
import sawtooth.sdk.protobuf.TpProcessResponse;

public class TransactionHandlerTaskTest {

  TpProcessResponse response;

  @Test
  public void testRun() {

    Stream replyStream = mock(Stream.class);
    TransactionHandler txHandler = mock(TransactionHandler.class);
    TransactionHandlerTask task = new TransactionHandlerTask(Message.getDefaultInstance(), replyStream, txHandler);

    try {
      response = null;
      doAnswer(new Answer<Void>() {
        public Void answer(InvocationOnMock invocation) {
          Object[] args = invocation.getArguments();
          ByteString contents = (ByteString) args[2];
          try {
            response = TpProcessResponse.parseFrom(contents);
          } catch (InvalidProtocolBufferException exc) {
            fail("Sent back an invalid message!");
          }
          return null;
        }
      }).when(replyStream).sendBack(any(), any(), any());

      task.run();
      verify(replyStream, times(1)).sendBack(eq(MessageType.TP_PROCESS_RESPONSE), any(String.class),
          any(ByteString.class));
      assertNotNull(response);
      assertTrue(response.getStatus() == TpProcessResponse.Status.OK);

      response = null;
      InvalidTransactionException ite = new InvalidTransactionException("Expected InvalidTransactionException",
          new byte[] {});
      doThrow(ite).when(txHandler).apply(any(), any());
      task.run();
      verify(replyStream, times(2)).sendBack(eq(MessageType.TP_PROCESS_RESPONSE), any(String.class),
          any(ByteString.class));
      assertNotNull(response);
      assertTrue(response.getStatus() == TpProcessResponse.Status.INVALID_TRANSACTION);

      response = null;
      InternalError ie = new InternalError("Expected InternalError", new byte[] {});
      doThrow(ie).when(txHandler).apply(any(), any());
      task.run();
      verify(replyStream, times(3)).sendBack(eq(MessageType.TP_PROCESS_RESPONSE), any(String.class),
          any(ByteString.class));
      assertNotNull(response);
      assertTrue(response.getStatus() == TpProcessResponse.Status.INTERNAL_ERROR);

    } catch (InvalidTransactionException | InternalError exc) {
      fail("No exceptions should be thrown");
    }
  }

}
