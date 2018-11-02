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
 * Convenience class that wraps the PrivateKey and the Context.
 */
public class Signer {

  /**
   * The `sawtooth.sdk.signing.Context` implementation.
   */
  private Context mContext;
  /**
   * The `sawtooth.sdk.signing.PrivateKey` implementation.
   */
  private PrivateKey mPrivateKey;

  /**
   * Constructor.
   *
   * @param aContext Context
   * @param aPrivateKey PrivateKey
   */
  public Signer(final Context aContext, final PrivateKey aPrivateKey) {
    this.mContext = aContext;
    this.mPrivateKey = aPrivateKey;
  }

  /**
   * Produce a Hex encoded signature from the data and the PrivateKey.
   *
   * @param data byte[]
   * @return String signature
   */
  public final String sign(final byte[] data) {
    return this.mContext.sign(data, this.mPrivateKey);
  }

  /**
   * Get the public key associated with the private key.
   *
   * @return PublicKey
   */
  public final PublicKey getPublicKey() {
    return this.mContext.getPublicKey(this.mPrivateKey);
  }
}
