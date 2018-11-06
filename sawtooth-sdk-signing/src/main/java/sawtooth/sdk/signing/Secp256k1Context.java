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

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * A Context for signing and verifying Secp256k1 signatures.
 */
public class Secp256k1Context implements Context {

  /**
   * The algorithm name associated with this type of Context.
   */
  private static final String SECP2561K_ALGORITHM_NAME = "secp256k1";

  /**
   * Logger.
   */
  private static final Logger LOGGER = Logger.getLogger(Context.class.getName());

  /**
   * The number of bytes in the signature.
   */
  private static final int NUM_SIGNATURE_BYTES = 64;

  /**
   * Half of the number of bytes in the signature.
   */
  private static final int HALF_NUM_SIGNATURE_BYTES = 32;

  @Override
  public final String getAlgorithmName() {
    return SECP2561K_ALGORITHM_NAME;
  }

  @Override
  public final PublicKey getPublicKey(final PrivateKey privateKey) {
    ECKey privKey = ECKey.fromPrivate(privateKey.getBytes());
    byte[] publicKey = privKey.getPubKey();
    return new Secp256k1PublicKey(publicKey);
  }

  /**
   * Generate a bitcoin-style compact signature.
   *
   * @param privateKey ECKey private key
   * @param data the raw message bytes
   * @return the raw signature bytes
   */
  private static byte[] generateCompactSig(final ECKey privateKey, final byte[] data) {
    Sha256Hash hash = Sha256Hash.of(data);
    ECKey.ECDSASignature sig = privateKey.sign(hash);

    byte[] csig = new byte[NUM_SIGNATURE_BYTES];

    System.arraycopy(Utils.bigIntegerToBytes(sig.r, HALF_NUM_SIGNATURE_BYTES), 0,
                                             csig, 0, HALF_NUM_SIGNATURE_BYTES);
    System.arraycopy(Utils.bigIntegerToBytes(sig.s, HALF_NUM_SIGNATURE_BYTES), 0,
                                             csig, HALF_NUM_SIGNATURE_BYTES, HALF_NUM_SIGNATURE_BYTES);

    return csig;
  }

  @Override
  public final String sign(final byte[] data, final PrivateKey privateKey) {
    ECKey privKey = ECKey.fromPrivate(privateKey.getBytes());

    return Utils.HEX.encode(generateCompactSig(privKey, data));
  }

  @Override
  public final boolean verify(final String signature, final byte[] data, final PublicKey publicKey) {

    byte[] signatureBytes = Utils.HEX.decode(signature);

    byte[] rbytes = Arrays.copyOfRange(signatureBytes, 0, HALF_NUM_SIGNATURE_BYTES);
    byte[] sbytes = Arrays.copyOfRange(signatureBytes, HALF_NUM_SIGNATURE_BYTES, NUM_SIGNATURE_BYTES);

    BigInteger rSig = new BigInteger(1, rbytes);
    BigInteger sSig = new BigInteger(1, sbytes);

    ECKey.ECDSASignature ecdsaSignature = new ECKey.ECDSASignature(rSig, sSig);

    byte[] hash = Sha256Hash.of(data).getBytes();

    return ECKey.verify(hash, ecdsaSignature, publicKey.getBytes());
  }

  @Override
  public final PrivateKey newRandomPrivateKey() {
    ECKey eckey = new ECKey(new SecureRandom());
    return new Secp256k1PrivateKey(eckey.getPrivKeyBytes());
  }

}
