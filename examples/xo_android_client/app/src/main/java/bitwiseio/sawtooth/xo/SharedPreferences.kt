package bitwiseio.sawtooth.xo

import android.app.Activity
import android.content.Context
import android.support.v7.preference.PreferenceManager
import sawtooth.sdk.signing.PrivateKey
import sawtooth.sdk.signing.Secp256k1Context
import sawtooth.sdk.signing.Secp256k1PrivateKey

const val sharedPreferencePrivateKey = "private_key"
const val sharedPreferencePublicKey = "public_key"

fun getPrivateKey(activity: Activity): Secp256k1PrivateKey {
    var sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
    var privateKey = sharedPref.getString(sharedPreferencePrivateKey, "")
    if (privateKey == "") {
        privateKey = generatePrivateKey()
        with(sharedPref.edit()) {
            putString(sharedPreferencePrivateKey, privateKey)
            apply()
        }
    }
    return Secp256k1PrivateKey.fromHex(privateKey)
}

fun getPublicKey(activity: Activity, privateKey: PrivateKey): String {
    var sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
    var publicKey = sharedPref.getString(sharedPreferencePublicKey, "")
    if (publicKey == "" || publicKey == null) {
        publicKey = generatePublicKey(privateKey)
        with(sharedPref.edit()) {
            putString(sharedPreferencePublicKey, publicKey)
            apply()
        }
    }
    return publicKey
}

private fun generatePrivateKey(): String {
    val context = Secp256k1Context()
    return context.newRandomPrivateKey().hex()
}

private fun generatePublicKey(privateKey: PrivateKey): String {
    val context = Secp256k1Context()
    return context.getPublicKey(privateKey).hex()
}

fun getRestApiUrl(activity: Activity, settingsKey: String, urlDefaultValue: String): String {
    var sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
    return sharedPref.getString(settingsKey, urlDefaultValue) ?: urlDefaultValue
}
