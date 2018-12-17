package bitwiseio.sawtooth.xo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.google.gson.Gson
import bitwiseio.sawtooth.xo.state.api.XORequestHandler
import sawtooth.sdk.signing.Secp256k1PrivateKey

class CreateGameActivity : AppCompatActivity() {
    private var requestHandler: XORequestHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_game)
        setSupportActionBar(findViewById(R.id.action_menu))

        requestHandler = XORequestHandler(getRestApiUrl(this,
            getString(R.string.rest_api_settings_key),
            getString(R.string.default_rest_api_address)),
            getPrivateKey(intent.getStringExtra("privateKey")))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.send_transaction -> {
            val editText = findViewById<EditText>(R.id.gameName)
            val message = editText.text.toString()
            if (message.isBlank()) {
                Snackbar.make(findViewById(R.id.create_game_layout), "Please enter a name for the game.", Snackbar.LENGTH_LONG).show()
            } else {
                requestHandler?.createGame(message, findViewById(R.id.create_game_layout),
                    getRestApiUrl(this,
                        getString(R.string.rest_api_settings_key),
                        getString(R.string.default_rest_api_address))
                ) {it ->
                    if (it) {
                        Handler().postDelayed({
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }, 1500)
                    }

                }
            }
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun getPrivateKey(privateKey: String): Secp256k1PrivateKey {
        val gson = Gson()
        return gson.fromJson(privateKey, Secp256k1PrivateKey::class.java)
    }
}
