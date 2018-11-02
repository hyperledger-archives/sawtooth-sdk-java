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
 * Wraps the Private Key.
 */
public final class Secp256k1PrivateKey implements PrivateKey {

  /**
   * The algorithm name associated with this type of PrivateKey.
   */
  private static final String SECP256K1_ALGORITHM_NAME = "secp256k1";

  /**
   * The private key bytes.
   */
  private byte[] mPrivKey;

  @Override
  public String getAlgorithmName() {
    return SECP256K1_ALGORITHM_NAME;
  }

  /**
   * Constructor.
   *
   * @param data private key byte[]
   */
  public Secp256k1PrivateKey(final byte[] data) {
    this.mPrivKey = data;
  }

  /**
   * Create a private key from hex.
   *
   * @param aPrivateKey hex String.
   * @return Secp256k1PrivateKey The private key.
   */
  public static Secp256k1PrivateKey fromHex(final String aPrivateKey) {
    return new Secp256k1PrivateKey(Utils.HEX.decode(aPrivateKey));
  }

  @Override
  public String hex() {
    return Utils.HEX.encode(this.mPrivKey).toLowerCase();
  }

  @Override
  public byte[] getBytes() {
    return this.mPrivKey;
  }
}
