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
 * A factory class for making Contexts.
 */
public final class CryptoFactory {

  /**
   * Private constructor for Factory class.
   */
  private CryptoFactory() { }

  /**
   * Create a Context of the specific type.
   *
   * @param algorithmName The name of the algorithm.
   * @return A Context.
   */
  public static Context createContext(final String algorithmName) {

    Context context = null;

    if (algorithmName.equals("secp256k1")) {
      context = new Secp256k1Context();
    } else {
      throw new RuntimeException("During call to createContext, Algorithm is not implemented");
    }

    return context;
  }

}
