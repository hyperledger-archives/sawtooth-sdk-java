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

import java.util.Collection;
import java.util.Map;

import com.google.protobuf.ByteString;

import sawtooth.sdk.processor.exceptions.InternalError;
import sawtooth.sdk.processor.exceptions.InvalidTransactionException;

/**
 * Interface for interaction with the context manager.
 */
public interface Context {

  /**
   * Make a Get request on a specific context specified by contextId.
   *
   * @param addresses a collection of address Strings
   * @return Map where the keys are addresses, values Bytestring
   * @throws InternalError               something went wrong processing
   *                                     transaction
   * @throws InvalidTransactionException an invalid transaction was encountered
   */
  Map<String, ByteString> getState(Collection<String> addresses) throws InternalError, InvalidTransactionException;

  /**
   * Make a Set request on a specific context specified by contextId.
   *
   * @param addressValuePairs A collection of Map.Entry's
   * @return addressesThatWereSet, A collection of address Strings that were set
   * @throws InternalError               something went wrong processing
   *                                     transaction
   * @throws InvalidTransactionException an invalid transaction was encountered
   */
  Collection<String> setState(Collection<java.util.Map.Entry<String, ByteString>> addressValuePairs)
      throws InternalError, InvalidTransactionException;

}

