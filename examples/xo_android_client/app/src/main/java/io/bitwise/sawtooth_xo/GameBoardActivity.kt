package io.bitwise.sawtooth_xo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import io.bitwise.sawtooth_xo.state.XoState

class GameBoardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_board)
        setSupportActionBar(findViewById(R.id.action_menu))

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.send_transaction -> {
            val state =  XoState()
            val editText = findViewById<EditText>(R.id.gameName)
            val message = editText.text.toString()
            if(message.isBlank()){
                Toast.makeText(applicationContext, "Please, enter a name for the game.", Toast.LENGTH_LONG).show()
            }
            else {
                state.createGame(message, applicationContext)
            }
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}




