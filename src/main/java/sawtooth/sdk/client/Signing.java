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

package sawtooth.sdk.client;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;

import java.security.SecureRandom;

/** Signing has methods to manipulate bytes, Strings,
 *  and ECKeys to sign bytes and produce hex encoded Strings.
 *
 */
public final class Signing {

  /**
   * This class only has static methods, so the default constructor should be private.
   */
  private Signing() { };

  /**
   * The number of bytes of the signature produced.
   */
  private static final int NUM_SIGNATURE_BYTES = 64;

  /**
   * Half of the number of bytes of the signature produced.
   */
  private static final int HALF_NUM_SIGNATURE_BYTES = 32;

  /**
   * Parameters necessary for reading a wif encoded String.
   */
  private static final NetworkParameters MAINNET = org.bitcoinj.params.MainNetParams.get();

  /** Read in a wif encoded String and produce a ECKey private key.
   *
   * @param wif String encoded in the wif format.
   * @return ECkey private key
   */
  public static ECKey readWif(final String wif) {
    return DumpedPrivateKey.fromBase58(MAINNET, wif).getKey();
  }

  /** Generate an ECKey private key from an entropy enhancing random number generator.
   *
   * @param random random number generator
   * @return ECKey private key
   */
  public static ECKey generatePrivateKey(final SecureRandom random) {
    return new ECKey(random);
  }

  /** Static method to return a public key from a private key.
   *
   * @param privateKey the private key
   * @return String public key
   */
  public static String getPublicKey(final ECKey privateKey) {
    return ECKey.fromPrivate(privateKey.getPrivKey(), true).getPublicKeyAsHex();
  }

  /**
   * Returns a bitcoin-style 64-byte compact signature.
   * @param privateKey the private key with which to sign
   * @param data the data to sign
   * @return String the signature
   */
  public static String sign(final ECKey privateKey, final byte[] data) {
    Sha256Hash hash = Sha256Hash.of(data);
    ECKey.ECDSASignature sig = privateKey.sign(hash);

    byte[] csig = new byte[NUM_SIGNATURE_BYTES];

    System.arraycopy(Utils.bigIntegerToBytes(sig.r, HALF_NUM_SIGNATURE_BYTES), 0, csig, 0, HALF_NUM_SIGNATURE_BYTES);
    System.arraycopy(Utils.bigIntegerToBytes(sig.s, HALF_NUM_SIGNATURE_BYTES), 0, csig,
        HALF_NUM_SIGNATURE_BYTES, HALF_NUM_SIGNATURE_BYTES);

    return Utils.HEX.encode(csig);
  }
}
