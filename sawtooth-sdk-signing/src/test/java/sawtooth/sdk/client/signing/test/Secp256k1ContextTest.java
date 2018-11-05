/* Copyright 2018 Bitwise IO
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

package sawtooth.sdk.client.signing.test;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;
import org.junit.Assert;
import org.junit.Test;
import sawtooth.sdk.client.signing.PrivateKey;
import sawtooth.sdk.client.signing.PublicKey;
import sawtooth.sdk.client.signing.Secp256k1Context;
import sawtooth.sdk.client.signing.Secp256k1PrivateKey;
import sawtooth.sdk.client.signing.Secp256k1PublicKey;
import sawtooth.sdk.client.signing.Signer;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;


public class Secp256k1ContextTest {

  @Test
  public void testPublicPrivateKey() {
    Secp256k1Context context = new Secp256k1Context();

    PrivateKey privateKey = Secp256k1PrivateKey.fromHex(Utils.HEX.encode(BigInteger.TEN.toByteArray()));

    ECKey ecKey = ECKey.fromPrivate(privateKey.getBytes(), true);
    PublicKey publicKeyFromECKey = new Secp256k1PublicKey(ecKey.getPubKey());

    PublicKey publicKey = context.getPublicKey(privateKey);

    Assert.assertEquals(publicKey.hex(), publicKeyFromECKey.hex());

  }

  @Test
  public void testPublicPrivatePythonGeneratedKeys() {
    Secp256k1Context context = new Secp256k1Context();

    PrivateKey privateKey = Secp256k1PrivateKey.fromHex("80378f103c7f1ea5856d50f2dcdf38b97da5986e9b32297be2de3c8444c38c08");

    PublicKey publicKey = Secp256k1PublicKey.fromHex("0279b0fbdf73d8656c86ef6fe12c5de883ebb5a07126aa2ab655e6f8321cb4beed");

    Assert.assertEquals("Calculating the public key from the private key with the context gets the same result as Python signer",
        context.getPublicKey(privateKey).hex(), publicKey.hex());
  }

  @Test
  public void testSignAndVerify() {
    Secp256k1Context context = new Secp256k1Context();

    PrivateKey privateKey = Secp256k1PrivateKey.fromHex("80378f103c7f1ea5856d50f2dcdf38b97da5986e9b32297be2de3c8444c38c08");

    Signer signer = new Signer(context, privateKey);

    PublicKey publicKey = Secp256k1PublicKey.fromHex("0279b0fbdf73d8656c86ef6fe12c5de883ebb5a07126aa2ab655e6f8321cb4beed");

    byte[] message = "Hello, Alice, this is Bob.".getBytes();

    String signature = signer.sign(message);

    Assert.assertTrue("A context can sign and verify its own signatures.",
        context.verify(signature, message, publicKey));
  }

  @Test
  public void testSignAndVerifyPythonCompatibility() throws UnsupportedEncodingException {
    Secp256k1Context context = new Secp256k1Context();

    PrivateKey privateKey = Secp256k1PrivateKey.fromHex("80378f103c7f1ea5856d50f2dcdf38b97da5986e9b32297be2de3c8444c38c08");

    Signer signer = new Signer(context, privateKey);

    PublicKey publicKey = Secp256k1PublicKey.fromHex("0279b0fbdf73d8656c86ef6fe12c5de883ebb5a07126aa2ab655e6f8321cb4beed");

    byte[] message = "Hello, Alice, this is Bob.".getBytes("UTF-8");

    String signature = signer.sign(message);

    // This Signature was created with the Python sawtooth_signing library.

    Assert.assertEquals(
        "The Java and Python signing are equivalent",
        signature,
        "b7eec6dc1e4c3b64f0d5bae3f0e6be3978120c69ea1c8b5987921a869f36cb262a4200527f9a06585a4d461281e008b929f7c4ec24880d2baf2a774cfc61969a");
  }

}
