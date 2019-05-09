/* Copyright 2016,  2017 Intel Corporation
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

import java.util.concurrent.TimeoutException;

import com.google.protobuf.ByteString;

import sawtooth.sdk.protobuf.Message;

/**
 * The client networking class.
 */
public interface Stream extends AutoCloseable {

  /**
   * Send a message and return a Future that will later have the Bytestring.
   * @param destination one of the Message.MessageType enum values defined in
   *                    validator.proto
   * @param contents    the ByteString that has been serialized from a Protobuf
   *                    class
   * @return future a future that will have ByteString that can be deserialized
   *         into a, for example, GetResponse
   */
  Future send(Message.MessageType destination, ByteString contents);

  /**
   * Send a message without getting a future back. Useful for sending a response
   * message to, for example, a transaction
   * @param destination   Message.MessageType defined in validator.proto
   * @param correlationId a random string generated on the server for the client
   *                      to send back
   * @param contents      ByteString serialized contents that the server is
   *                      expecting
   */
  void sendBack(Message.MessageType destination, String correlationId, ByteString contents);

  /**
   * Get a message that has been received.
   * @return result, a protobuf Message
   */
  Message receive();

  /**
   * Get a message that has been received. If the timeout is expired, throws
   * TimeoutException.
   * @param timeout time to wait for a message.
   * @return result, a protobuf Message
   * @throws TimeoutException The Message is not received before timeout.
   */
  Message receive(long timeout) throws TimeoutException;

}
