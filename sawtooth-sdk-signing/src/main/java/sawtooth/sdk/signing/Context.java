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
 * Interface to be implemented by different signing backends.
 *
 */
public interface Context {

  /**
   * Return the Algorithm name.
   *
   * @return String Algorithm name.
   */
  String getAlgorithmName();

  /**
   * Sign bytes returning a signature, hex encoded.
   *
   * @param data byte[]
   * @param privateKey PrivateKey
   * @return String, hex encoded signature.
   */
  String sign(byte[] data, PrivateKey privateKey);

  /**
   * Verify that the private key associated with the public key, produced the signature
   * by signing the bytes.
   *
   * @param signature String
   * @param data byte[]
   * @param publicKey PublicKey
   *
   * @return boolean
   */
  boolean verify(String signature, byte[] data, PublicKey publicKey);

  /**
   * Get the public key from the private key.
   *
   * @param privateKey PrivateKey
   * @return PublicKey
   */
  PublicKey getPublicKey(PrivateKey privateKey);

  /**
   * Generate a random PrivateKey.
   *
   * @return PrivateKey
   */
  PrivateKey newRandomPrivateKey();

}
