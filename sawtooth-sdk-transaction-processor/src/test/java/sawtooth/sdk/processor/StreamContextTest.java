package sawtooth.sdk.processor;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.protobuf.ByteString;

import net.bytebuddy.utility.RandomString;
import sawtooth.sdk.messaging.FutureByteString;
import sawtooth.sdk.messaging.Stream;
import sawtooth.sdk.processor.exceptions.InternalError;
import sawtooth.sdk.processor.exceptions.InvalidTransactionException;
import sawtooth.sdk.protobuf.TpEventAddResponse;
import sawtooth.sdk.protobuf.TpReceiptAddDataResponse;
import sawtooth.sdk.protobuf.TpStateDeleteResponse;
import sawtooth.sdk.protobuf.TpStateEntry;
import sawtooth.sdk.protobuf.TpStateGetResponse;
import sawtooth.sdk.protobuf.TpStateSetResponse;

public class StreamContextTest {

  @Test
  public void testAddEvent() {
    Stream stream = mock(Stream.class);

    Context ctx = new StreamContext(stream, "test-context-id");

    Map<String, String> testMap = new HashMap<>();
    testMap.put("testKey1", "testValue1");
    testMap.put("testKey2", "testValue2");
    ByteString bs = ByteString.copyFromUtf8("testData");

    FutureByteString okResponse = new FutureByteString("test-correlation-id");
    okResponse
        .setResult(TpEventAddResponse.newBuilder().setStatus(TpEventAddResponse.Status.OK).build().toByteString());

    FutureByteString errResponse = new FutureByteString("test-correlation-id");
    errResponse
        .setResult(TpEventAddResponse.newBuilder().setStatus(TpEventAddResponse.Status.ERROR).build().toByteString());

    FutureByteString garbageResponse = new FutureByteString("test-correlation-id");
    garbageResponse.setResult(ByteString.copyFrom("something-garbage-like".getBytes()));

    try {
      when(stream.send(any(), any())).thenReturn(okResponse);
      ctx.addEvent("test-event", testMap.entrySet(), bs);
    } catch (InternalError exc) {
      fail("Happy path should not generate an InternalError");
    }

    try {
      when(stream.send(any(), any())).thenReturn(garbageResponse);
      ctx.addEvent("test-event", testMap.entrySet(), bs);
      fail("An error should have been thrown since we responded with something wrong!");
    } catch (InternalError ie) {
      // Expected
    }

    try {
      when(stream.send(any(), any())).thenReturn(errResponse);
      ctx.addEvent("test-event", testMap.entrySet(), bs);
      fail("An error should have been thrown since we responded with something wrong!");
    } catch (InternalError ie) {
      // Expected
    }
  }

  @Test
  public void testAddReceiptData() {
    Stream stream = mock(Stream.class);

    Context ctx = new StreamContext(stream, "test-context-id");

    Map<String, String> testMap = new HashMap<>();
    testMap.put("testKey1", "testValue1");
    testMap.put("testKey2", "testValue2");
    ByteString bs = ByteString.copyFromUtf8("testData");

    FutureByteString okResponse = new FutureByteString("test-correlation-id");
    okResponse.setResult(
        TpReceiptAddDataResponse.newBuilder().setStatus(TpReceiptAddDataResponse.Status.OK).build().toByteString());

    FutureByteString errResponse = new FutureByteString("test-correlation-id");
    errResponse.setResult(
        TpReceiptAddDataResponse.newBuilder().setStatus(TpReceiptAddDataResponse.Status.ERROR).build().toByteString());

    FutureByteString garbageResponse = new FutureByteString("test-correlation-id");
    garbageResponse.setResult(ByteString.copyFrom("something-garbage-like".getBytes()));

    try {
      when(stream.send(any(), any())).thenReturn(okResponse);
      ctx.addReceiptData(bs);
    } catch (InternalError exc) {
      fail("Happy path should not generate an InternalError");
    }

    try {
      when(stream.send(any(), any())).thenReturn(garbageResponse);
      ctx.addReceiptData(bs);
      fail("An error should have been thrown since we responded with something wrong!");
    } catch (InternalError ie) {
      // Expected
    }

    try {
      when(stream.send(any(), any())).thenReturn(errResponse);
      ctx.addReceiptData(bs);
      fail("An error should have been thrown since we responded with something wrong!");
    } catch (InternalError ie) {
      // Expected
    }
  }

  @Test
  public void testDeleteState() {
    Stream stream = mock(Stream.class);

    Context ctx = new StreamContext(stream, "test-context-id");

    List<String> deleteList = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      deleteList.add(RandomString.make(70));
    }

    FutureByteString okResponse = new FutureByteString("test-correlation-id");
    TpStateDeleteResponse deleteReponse = TpStateDeleteResponse.newBuilder().setStatus(TpStateDeleteResponse.Status.OK)
        .addAllAddresses(deleteList).build();
    okResponse.setResult(deleteReponse.toByteString());

    FutureByteString emptyResponse = new FutureByteString("test-correlation-id");
    TpStateDeleteResponse emptyDeleteReponse = TpStateDeleteResponse.newBuilder()
        .setStatus(TpStateDeleteResponse.Status.OK).addAllAddresses(new ArrayList<String>()).build();
    emptyResponse.setResult(emptyDeleteReponse.toByteString());

    FutureByteString errResponse = new FutureByteString("test-correlation-id");
    errResponse.setResult(TpStateDeleteResponse.newBuilder().setStatus(TpStateDeleteResponse.Status.AUTHORIZATION_ERROR)
        .build().toByteString());

    FutureByteString garbageResponse = new FutureByteString("test-correlation-id");
    garbageResponse.setResult(ByteString.copyFrom("something-garbage-like".getBytes()));

    try {
      when(stream.send(any(), any())).thenReturn(okResponse);
      Collection<String> addressesDeleted = ctx.deleteState(deleteList);
      // Compare the in and out addresses, without trusting that either are ordered
      Set<String> inSet = new HashSet<>(deleteList);
      Set<String> outSet = new HashSet<>(addressesDeleted);
      if (!inSet.equals(outSet)) {
        fail("Addresses requested to be deleted do not match those which were actually deleted");
      }
    } catch (InternalError exc) {
      exc.printStackTrace();
      fail("Happy path should not generate an InternalError");
    } catch (InvalidTransactionException exc) {
      exc.printStackTrace();
      fail("Happy path should not generate an InvalidTransactionException");
    }

    try {
      when(stream.send(any(), any())).thenReturn(emptyResponse);
      Collection<String> addressesDeleted = ctx.deleteState(deleteList);
      assertTrue("addressesDeleted should be empty", addressesDeleted.isEmpty());
    } catch (InternalError exc) {
      fail("Returning an empty list of deleted addresses should not result in an InternalError");
    } catch (InvalidTransactionException exc) {
      exc.printStackTrace();
      fail("Empty path should not generate an InvalidTransactionException");
    }

    try {
      when(stream.send(any(), any())).thenReturn(garbageResponse);
      Collection<String> addressesDeleted = ctx.deleteState(deleteList);
      fail("An error should have been thrown since we responded with something wrong!");
    } catch (InternalError ie) {
      // Expected
    } catch (InvalidTransactionException exc) {
      exc.printStackTrace();
      fail(" should not generate an InvalidTransactionException");
    }

    try {
      when(stream.send(any(), any())).thenReturn(errResponse);
      Collection<String> addressesDeleted = ctx.deleteState(deleteList);
      fail("An error should have been thrown since we responded with something wrong!");
    } catch (InternalError ie) {
      ie.printStackTrace();
      fail(" should not generate an InternalError");
    } catch (InvalidTransactionException exc) {
      // Expected
    }
  }

  @Test
  public void testGetState() throws UnsupportedEncodingException {
    Stream stream = mock(Stream.class);

    Context ctx = new StreamContext(stream, "test-context-id");

    Map<String, ByteString> getMap = new HashMap<>();
    List<TpStateEntry> entryList = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      String key = RandomString.make(70);
      ByteString value = ByteString.copyFrom(RandomString.make(10), "UTF-8");
      getMap.put(key, value);
      TpStateEntry entry = TpStateEntry.newBuilder().setAddress(key).setData(value).build();
      entryList.add(entry);
    }

    FutureByteString okResponse = new FutureByteString("test-correlation-id");
    TpStateGetResponse validReponse = TpStateGetResponse.newBuilder().setStatus(TpStateGetResponse.Status.OK)
        .addAllEntries(entryList).build();
    okResponse.setResult(validReponse.toByteString());

    FutureByteString emptyResponse = new FutureByteString("test-correlation-id");
    TpStateGetResponse emptyGetReponse = TpStateGetResponse.newBuilder().setStatus(TpStateGetResponse.Status.OK)
        .addAllEntries(new ArrayList<TpStateEntry>()).build();
    emptyResponse.setResult(emptyGetReponse.toByteString());

    FutureByteString errResponse = new FutureByteString("test-correlation-id");
    errResponse.setResult(TpStateGetResponse.newBuilder().setStatus(TpStateGetResponse.Status.AUTHORIZATION_ERROR)
        .build().toByteString());

    try {
      when(stream.send(any(), any())).thenReturn(okResponse);
      Map<String, ByteString> resultMap = ctx.getState(getMap.keySet());

      if (!resultMap.equals(getMap)) {
        fail("getState returned a different result than we asked for");
      }
    } catch (InternalError exc) {
      exc.printStackTrace();
      fail("Happy path should not generate an InternalError");
    } catch (InvalidTransactionException exc) {
      exc.printStackTrace();
      fail("Happy path should not generate an InvalidTransactionException");
    }

    try {
      when(stream.send(any(), any())).thenReturn(emptyResponse);
      Map<String, ByteString> resultMap = ctx.getState(getMap.keySet());
      assertTrue("resultMap should be empty", resultMap.isEmpty());
    } catch (InternalError exc) {
      fail("Returning an empty map of entries should not result in an InternalError");
    } catch (InvalidTransactionException exc) {
      exc.printStackTrace();
      fail("Empty path should not generate an InvalidTransactionException");
    }

    try {
      when(stream.send(any(), any())).thenReturn(errResponse);
      Map<String, ByteString> resultMap = ctx.getState(getMap.keySet());
      fail("An error should have been thrown since we responded with something wrong!");
    } catch (InternalError ie) {
      ie.printStackTrace();
      fail(" should not generate an InternalError");
    } catch (InvalidTransactionException exc) {
      // Expected
    }
  }

  @Test
  public void testSetState() throws UnsupportedEncodingException {
    Stream stream = mock(Stream.class);

    Context ctx = new StreamContext(stream, "test-context-id");

    Map<String, ByteString> getMap = new HashMap<>();
    List<TpStateEntry> entryList = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      String key = RandomString.make(70);
      ByteString value = ByteString.copyFrom(RandomString.make(10), "UTF-8");
      getMap.put(key, value);
      TpStateEntry entry = TpStateEntry.newBuilder().setAddress(key).setData(value).build();
      entryList.add(entry);
    }

    FutureByteString okResponse = new FutureByteString("test-correlation-id");
    TpStateSetResponse validReponse = TpStateSetResponse.newBuilder().setStatus(TpStateSetResponse.Status.OK)
        .addAllAddresses(getMap.keySet()).build();
    okResponse.setResult(validReponse.toByteString());

    FutureByteString errResponse = new FutureByteString("test-correlation-id");
    errResponse.setResult(TpStateSetResponse.newBuilder().setStatus(TpStateSetResponse.Status.AUTHORIZATION_ERROR)
        .build().toByteString());

    try {
      when(stream.send(any(), any())).thenReturn(okResponse);
      Collection<String> result = ctx.setState(getMap.entrySet());

      Set<String> outSet = new HashSet<>(result);
      if (!getMap.keySet().equals(outSet)) {
        fail("Addresses requested to be set do not match those which were actually set");
      }

    } catch (InternalError exc) {
      exc.printStackTrace();
      fail("Happy path should not generate an InternalError");
    } catch (InvalidTransactionException exc) {
      exc.printStackTrace();
      fail("Happy path should not generate an InvalidTransactionException");
    }

    try {
      when(stream.send(any(), any())).thenReturn(errResponse);
      Collection<String> result = ctx.setState(getMap.entrySet());
      fail("An error should have been thrown since we responded with something wrong!");
    } catch (InternalError ie) {
      ie.printStackTrace();
      fail(" should not generate an InternalError");
    } catch (InvalidTransactionException exc) {
      // Expected
    }
  }

}
