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
 * FutureError throws a ValidatorConnectionError from all of its methods. Used
 * to resolve a future with an error response.
 */
public class FutureError implements Future {

  /**
   * Constructor.
   */
  public FutureError() {
  }

  /**
   * Always raises ValidatorConnectionError.
   * @throws ValidatorConnectionError Always throws this Exception.
   * @return Does not return.
   */
  public final ByteString getResult() throws ValidatorConnectionError {
    throw new ValidatorConnectionError();
  }

  /**
   * Always raises ValidatorConnectionError.
   * @param time The timeout.
   * @throws ValidatorConnectionError Always throws this Exception.
   * @throws TimeoutException         Does not throw this exception.
   * @return Does not return.
   */
  public final ByteString getResult(final long time) throws TimeoutException, ValidatorConnectionError {
    throw new ValidatorConnectionError();
  }

  /**
   * Always raises ValidatorConnectionError.
   * @param byteString The bytes.
   * @throws ValidatorConnectionError Always throws this exception.
   */
  public final void setResult(final ByteString byteString) throws ValidatorConnectionError {
    throw new ValidatorConnectionError();
  }

  /**
   * Always raises ValidatorConnectionError.
   * @return Does not return.
   * @throws ValidatorConnectionError Always throws this exception.
   */
  public final boolean isDone() throws ValidatorConnectionError {
    throw new ValidatorConnectionError();
  }

}
