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


import org.bitcoinj.core.Utils;

/**
 * A Public Key for the Secp256k1 algorithm.
 */
public final class Secp256k1PublicKey implements PublicKey {

  /**
   * The algorithm name associated with this type of PublicKey.
   */
  private static final String SECP256K1_ALGORITHM_NAME = "secp256k1";

  /**
   * The public key bytes.
   */
  private byte[] mData;

  @Override
  public String getAlgorithmName() {
    return SECP256K1_ALGORITHM_NAME;
  }

  /**
   * Constructor.
   *
   * @param data the public key byte[].
   */
  public Secp256k1PublicKey(final byte[] data) {
    this.mData = data;
  }

  /**
   * Create a Public Key from Hex.
   *
   * @param publicKey A String representing the public key.
   * @return Secp256k1PublicKey the public key.
   */
  public static Secp256k1PublicKey fromHex(final String publicKey) {
    return new Secp256k1PublicKey(Utils.HEX.decode(publicKey));
  }

  @Override
  public String hex() {
    return Utils.HEX.encode(this.mData).toLowerCase();
  }

  @Override
  public byte[] getBytes() {
    return this.mData;
  }
}
