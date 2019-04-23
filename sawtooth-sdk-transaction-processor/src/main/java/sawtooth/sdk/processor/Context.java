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
   * @param addresses a collection of address Strings
   * @return Map where the keys are addresses, values Bytestring
   * @throws InternalError               something went wrong processing
   *                                     transaction
   * @throws InvalidTransactionException an invalid transaction was encountered
   */
  Map<String, ByteString> getState(Collection<String> addresses) throws InternalError, InvalidTransactionException;

  /**
   * Make a Set request on a specific context specified by contextId.
   * @param addressValuePairs A collection of Map.Entry's
   * @return addressesThatWereSet, A collection of address Strings that were set
   * @throws InternalError               something went wrong processing
   *                                     transaction
   * @throws InvalidTransactionException an invalid transaction was encountered
   */
  Collection<String> setState(Collection<java.util.Map.Entry<String, ByteString>> addressValuePairs)
      throws InternalError, InvalidTransactionException;

  /**
   * Make a Delete request on a specific context specified by contextId.
   * @param addresses a collection of address Strings
   * @return addressesThatWereDeleted, A collection of address Strings that were
   *         deleted
   * @throws InternalError               something went wrong processing
   *                                     transaction
   * @throws InvalidTransactionException an invalid transaction was encountered
   */
  Collection<String> deleteState(Collection<String> addresses) throws InternalError, InvalidTransactionException;

  /**
   * Add a blob to the execution result for this transaction.
   * @param data The data to add
   * @throws InternalError something went wrong processing transaction
   */
  void addReceiptData(ByteString data) throws InternalError;

  /**
   * Adds a new event to the execution result for this transaction.
   * @param eventType  This is used to subscribe to events. It should be globally
   *                   unique and describe what, in general, has occurred.
   * @param attributes Additional information about the event that is transparent
   *                   to the validator. Attributes can be used by subscribers to
   *                   filter the type of events they receive.
   * @param data       Additional information about the event that is opaque to
   *                   the validator, or null
   * @throws InternalError something went wrong processing transaction
   */
  void addEvent(String eventType, Collection<Map.Entry<String, String>> attributes, ByteString data)
      throws InternalError;
}
