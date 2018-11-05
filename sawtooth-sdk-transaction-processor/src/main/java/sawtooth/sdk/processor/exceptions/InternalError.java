/* Copyright 2016 Intel Corporation
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

package sawtooth.sdk.processor.exceptions;

/**
 * An internal error.
 */
public class InternalError extends SawtoothException {
  /**
   * The simple constructor for this class.
   * @param message the internal error message
   */
  public InternalError(final String message) {
    super(message);
  }

  /**
   * The extended constructor for this class.
   * @param message the internal error message
   * @param extendedData extended error data
   */
  public InternalError(final String message, final byte[] extendedData) {
    super(message, extendedData);
  }
}
