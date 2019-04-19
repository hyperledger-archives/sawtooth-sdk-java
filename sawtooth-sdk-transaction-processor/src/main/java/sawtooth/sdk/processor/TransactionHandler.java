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

package sawtooth.sdk.processor;

import sawtooth.sdk.processor.exceptions.InternalError;
import sawtooth.sdk.processor.exceptions.InvalidTransactionException;
import sawtooth.sdk.protobuf.TpProcessRequest;

import java.util.Collection;

/**
 * Interface for creating a transaction handler.
 */
public interface TransactionHandler {

  /**
   * Returns the transaction family's name.
   * @return the transaction family's name
   */
  String transactionFamilyName();

  /**
   * Returns the transaction family's version.
   * @return the transaction family's version
   */
  String getVersion();

  /**
   * Returns the namespaces for this transaction handler.
   * @return the namespaces for this transaction handler
   */
  Collection<String> getNameSpaces();

  /**
   * Applies the given transaction request.
   * @param transactionRequest the transaction request to apply
   * @param state the on-chain state for this transaction
   * @throws InvalidTransactionException an invalid transaction was encountered
   * @throws InternalError something went wrong processing transaction
   */
  void apply(TpProcessRequest transactionRequest,
                    Context state) throws InvalidTransactionException, InternalError;


}
