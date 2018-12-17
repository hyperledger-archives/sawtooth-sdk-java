package bitwiseio.sawtooth.xo

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import bitwiseio.sawtooth.xo.models.Game
import com.google.gson.Gson
import bitwiseio.sawtooth.xo.state.api.XORequestHandler
import bitwiseio.sawtooth.xo.viewmodels.GameBoardViewModel
import sawtooth.sdk.signing.Secp256k1PrivateKey

class GameBoardActivity : AppCompatActivity(), View.OnClickListener {

    var game: Game? = null
    private lateinit var model: GameBoardViewModel
    private var requestHandler: XORequestHandler? = null
    private val gson = Gson()

    private var gameBoard: MutableList<Button> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = this.intent
        val displayGame = getGameObject(intent.getStringExtra("selectedGame"))
        this.game = displayGame

        requestHandler = XORequestHandler(getRestApiUrl(this,
                        getString(R.string.rest_api_settings_key),
                        getString(R.string.default_rest_api_address)),
                        getPrivateKey(intent.getStringExtra("privateKey")))

        setContentView(R.layout.activity_game_board)
        setSupportActionBar(findViewById(R.id.game_board_menu))

        collectButtons()

        model = ViewModelProviders.of(this).get(GameBoardViewModel::class.java)
        model.game.observe(this, Observer<Game> { fetchedGame ->
            game = fetchedGame
            updateBoard()
        })
        model.loadGame(game?.name!!)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.game_board_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.refresh_board -> {
            model.loadGame(game?.name!!)
            true
        }
        R.id.game_board_information -> {
            showAlertDialog(game)
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun updateGameInformation() {
        val boardName: TextView = this.findViewById(R.id.game_board_name)
        boardName.text = game?.name
        val gameState: TextView = this.findViewById(R.id.game_board_state)
        gameState.text = game?.gameState
    }

    private fun getGameObject(game: String): Game {
        return gson.fromJson<Game>(game, Game::class.java)
    }

    private fun getPrivateKey(privateKey: String): Secp256k1PrivateKey {
        return gson.fromJson(privateKey, Secp256k1PrivateKey::class.java)
    }

    private fun showAlertDialog(item: Game?) {
        val buildDialog = AlertDialog.Builder(this)

        if (item == null) {
            buildDialog.setMessage("No valid game")
            buildDialog.setPositiveButton("Ok", null)
        } else {
            createDialog(buildDialog, item)
        }

        buildDialog.show()
    }

    private fun createDialog(builder: AlertDialog.Builder, item: Game) {
        builder.setTitle(item.name)
        val message = getString(R.string.player_pub_keys, item.playerKey1, item.playerKey2)
        builder.setMessage(message)
    }

    private fun collectButtons() {
        val layout = findViewById<TableLayout>(R.id.game_table)
        for (i in 0..layout.childCount step 2) {
            val tableRow: TableRow = findViewById(layout.getChildAt(i).id)
            for (j in 0..tableRow.childCount step 2) {
                val button: Button = findViewById(tableRow.getChildAt(j).id)
                button.setOnClickListener(this)
                gameBoard.add(button)
            }
        }
    }

    private fun updateBoard() {
        updateGameInformation()
        gameBoard.forEachIndexed { index, button ->
            when (game?.board?.get(index)) {
                'X', 'O' -> {
                    button.text = game?.board?.get(index).toString()
                    button.isClickable = false
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.design_default_color_primary_dark))
                } else -> {
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.design_default_color_primary))
            }
            }
        }
    }

    override fun onClick(v: View) {
        val itemId = v.id
        val intSpace = (gameBoard.indexOfFirst { it.id == itemId }) + 1
        requestHandler?.takeSpace(
            game?.name!!,
            intSpace.toString(),
            findViewById(R.id.game_board_layout),
            getRestApiUrl(this,
                getString(R.string.rest_api_settings_key),
                getString(R.string.default_rest_api_address)))
        v.setBackgroundColor(ContextCompat.getColor(this, R.color.selected_button))
    }
}
