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

import sawtooth.sdk.protobuf.Message;

/**
 * Inner class for passing messages.
 */
public final class MessageWrapper {
  /**
   * The protobuf Message.
   */
  private Message message;

  /** Constructor.
   *
   * @param msg The protobuf Message.
   */
  MessageWrapper(final Message msg) {
    this.message = msg;
  }

  /** Return the Message associated with this MessageWrapper.
   *
   * @return Message the message.
   */
  public Message getMessage() {
    return message;
  }
}
