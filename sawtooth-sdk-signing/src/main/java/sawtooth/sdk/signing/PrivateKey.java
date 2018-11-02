/* Copyright 2018 Bitwise IO, Inc.
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

package sawtooth.sdk.signing;

/**
 * A PrivateKey for any asymmetric key algorithm.
 */
public interface PrivateKey {

  /**
   * Return the algorithm name for this key.
   *
   * @return String algorithm name.
   */
  String getAlgorithmName();

  /**
   * Return the PrivateKey, hex encoded.
   *
   * @return String Hex encoded private key
   */
  String hex();

  /**
   * Return the bytes underlying the PrivateKey.
   *
   * @return byte[]
   */
  byte[] getBytes();
}
