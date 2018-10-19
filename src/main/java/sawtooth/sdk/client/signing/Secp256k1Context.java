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

package sawtooth.sdk.client.signing;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;

import java.security.SecureRandom;
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

  private static byte[] generateCompactSig(ECKey privateKey, byte[] data) {
    Sha256Hash hash = Sha256Hash.of(data);
    ECKey.ECDSASignature sig = privateKey.sign(hash);

    byte[] csig = new byte[64];

    System.arraycopy(Utils.bigIntegerToBytes(sig.r, 32), 0, csig, 0, 32);
    System.arraycopy(Utils.bigIntegerToBytes(sig.s, 32), 0, csig, 32, 32);
    return csig;
  }

  @Override
  public final String sign(final byte[] data, final PrivateKey privateKey) {
    throw new RuntimeException("Not Implemented");
  }

  @Override
  public final boolean verify(final String signature, final byte[] data, final PublicKey publicKey) {

    throw new RuntimeException("Not Implemented");
  }

  @Override
  public final PrivateKey newRandomPrivateKey() {
    ECKey eckey = new ECKey(new SecureRandom());
    return new Secp256k1PrivateKey(eckey.getPrivKeyBytes());
  }

}
