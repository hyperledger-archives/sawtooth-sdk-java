/* Copyright 2017 Intel Corporation
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

import com.google.protobuf.ByteString;

import sawtooth.sdk.processor.exceptions.ValidatorConnectionError;

import java.util.concurrent.TimeoutException;

/**
 * An object that will have a value at some point.
 */
public interface Future {
  /**
   * Block until the result is ready.
   * @return the result ByteString
   * @throws InterruptedException     An interrupt happened during the method
   *                                  call.
   * @throws ValidatorConnectionError The validator disconnected.
   */
  ByteString getResult() throws InterruptedException, ValidatorConnectionError;

  /**
   * Block until timeout, then throw TimeoutException if result is not available.
   * @param timeout The amount of time to wait.
   * @return result ByteString
   * @throws InterruptedException     An interrupt happened.
   * @throws TimeoutException         The time to wait happened.
   * @throws ValidatorConnectionError The validator disconnected.
   */
  ByteString getResult(long timeout) throws InterruptedException, TimeoutException, ValidatorConnectionError;

  /**
   * Set the result of the Future.
   * @param byteString the result.
   * @throws ValidatorConnectionError The validator disconnected.
   */
  void setResult(ByteString byteString) throws ValidatorConnectionError;

  /**
   * The future is available.
   * @return boolean True if the future is resolved, false otherwise.
   * @throws ValidatorConnectionError The validator disconnected.
   */
  boolean isDone() throws ValidatorConnectionError;

}
