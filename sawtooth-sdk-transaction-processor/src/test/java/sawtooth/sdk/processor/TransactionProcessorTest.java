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

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.protobuf.ByteString;

import net.bytebuddy.utility.RandomString;
import sawtooth.sdk.messaging.Future;
import sawtooth.sdk.messaging.FutureByteString;
import sawtooth.sdk.messaging.Stream;
import sawtooth.sdk.processor.exceptions.InternalError;
import sawtooth.sdk.processor.exceptions.InvalidTransactionException;
import sawtooth.sdk.processor.exceptions.ValidatorConnectionError;
import sawtooth.sdk.protobuf.Message;
import sawtooth.sdk.protobuf.Message.MessageType;
import sawtooth.sdk.protobuf.TpProcessRequest;
import sawtooth.sdk.protobuf.TransactionHeader;

import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TransactionProcessorTest {

  private static final String testFamily = RandomString.make(6);
  private static final String testVersion = RandomString.make(4);

  @Test
  public void testAddFindHandler() {
    Stream stream = mock(Stream.class);
    TransactionProcessor tp = new TransactionProcessor(stream);

    TransactionHandler handler = mock(TransactionHandler.class);
    when(handler.transactionFamilyName()).thenReturn(testFamily);
    when(handler.getVersion()).thenReturn(testVersion);

    TransactionHandler handler2 = mock(TransactionHandler.class);
    when(handler2.transactionFamilyName()).thenReturn(testFamily);
    when(handler2.getVersion()).thenReturn(testVersion);

    tp.addHandler(handler);

    TransactionHeader header = TransactionHeader.newBuilder().setFamilyName(testFamily).setFamilyVersion(testVersion)
        .build();
    TpProcessRequest req = TpProcessRequest.newBuilder().setHeader(header).build();
    Message msg = Message.newBuilder().setMessageType(MessageType.TP_PROCESS_REQUEST).setContent(req.toByteString())
        .build();
    TransactionHandler testHandler = tp.findHandler(msg);
    assertTrue("TransactionProcessor should find the same handler we used before!", handler.equals(testHandler));

    tp.addHandler(handler2);
    header = TransactionHeader.newBuilder().setFamilyName(testFamily).setFamilyVersion(testVersion).build();
    req = TpProcessRequest.newBuilder().setHeader(header).build();

    msg = Message.newBuilder().setMessageType(MessageType.TP_PROCESS_REQUEST).setContent(req.toByteString()).build();
    testHandler = tp.findHandler(msg);
    assertTrue("TransactionProcessor should not find the same handler we used before, since we replaced it!",
        !handler.equals(testHandler));

    tp.addHandler(handler2);
    header = TransactionHeader.newBuilder().setFamilyName(testFamily).setFamilyVersion(testVersion).build();
    req = TpProcessRequest.newBuilder().setHeader(header).build();

    msg = Message.newBuilder().setMessageType(MessageType.TP_PROCESS_REQUEST).setContent(req.toByteString()).build();
    testHandler = tp.findHandler(msg);
    assertTrue("TransactionProcessor should find the same handler we used before, since we added it again!",
        handler2.equals(testHandler));

    header = TransactionHeader.newBuilder().setFamilyName(testFamily + "someother")
        .setFamilyVersion(testVersion + "someother").build();
    req = TpProcessRequest.newBuilder().setHeader(header).build();
    Message msgNewFamily = Message.newBuilder().setMessageType(MessageType.TP_PROCESS_REQUEST)
        .setContent(req.toByteString()).build();
    testHandler = tp.findHandler(msgNewFamily);
    assertNull("TransactionProcessor should not find a handler for this message with a new family!", testHandler);

    header = TransactionHeader.newBuilder().setFamilyName(testFamily).setFamilyVersion(testVersion + "someother")
        .build();
    req = TpProcessRequest.newBuilder().setHeader(header).build();
    Message msgNewVersion = Message.newBuilder().setMessageType(MessageType.TP_PROCESS_REQUEST)
        .setContent(req.toByteString()).build();
    testHandler = tp.findHandler(msgNewVersion);
    assertNull("TransactionProcessor should not find a handler for this message with an old family but new version!",
        testHandler);
  }

  @Test
  public void testRun() {
    Stream stream = mock(Stream.class);
    TransactionProcessor tp = new TransactionProcessor(stream);

    TransactionHeader header = TransactionHeader.newBuilder().setFamilyName(testFamily).setFamilyVersion(testVersion)
        .build();
    TpProcessRequest req = TpProcessRequest.newBuilder().setHeader(header).build();
    Message msg = Message.newBuilder().setMessageType(MessageType.TP_PROCESS_REQUEST).setContent(req.toByteString())
        .build();

    TransactionHeader headerNoFamily = TransactionHeader.newBuilder().setFamilyName("someother").setFamilyVersion(testVersion)
        .build();
    TpProcessRequest reqNoFamily = TpProcessRequest.newBuilder().setHeader(headerNoFamily).build();
    Message msgNoFamily = Message.newBuilder().setMessageType(MessageType.TP_PROCESS_REQUEST).setContent(reqNoFamily.toByteString())
        .build();
    
    Message pingMessage = Message.newBuilder().setMessageType(MessageType.PING_REQUEST).build();
    
    when(stream.receive()).thenReturn(msg,msgNoFamily,null,pingMessage);
    Future f=new FutureByteString(RandomString.make(70));
    try {
      f.setResult(ByteString.EMPTY);
    } catch (ValidatorConnectionError exc2) {
    }
    when(stream.send(any(), any())).thenReturn(f);

    TransactionHandler handler = mock(TransactionHandler.class);
    when(handler.transactionFamilyName()).thenReturn(testFamily);
    when(handler.getVersion()).thenReturn(testVersion);
    
    tp.addHandler(handler);
    
    try {
      tp.handleMessage(msg);
      tp.handleMessage(msgNoFamily);
      tp.handleMessage(null);
      tp.handleMessage(pingMessage);
    
      verify(handler,times(1)).apply(any(), any());
      verify(stream,times(2)).sendBack(any(), any(), any());
    } catch (InvalidTransactionException | InternalError exc1) {
      fail("No Exceptions should be thrown");
    } 
    tp.stopProcessor();
  }

  @Test
  public void testSetGetMaxOccupancy() {
    Stream stream = mock(Stream.class);
    TransactionProcessor tp = new TransactionProcessor(stream);

    int maxOccupancy = tp.getMaxOccupancy();
    int newMaxOccuppancy = maxOccupancy + 1;
    tp.setMaxOccupancy(newMaxOccuppancy);
    maxOccupancy = tp.getMaxOccupancy();
    assertTrue("setMaxOccuppancy has not applied!", maxOccupancy == newMaxOccuppancy);
  }

}
