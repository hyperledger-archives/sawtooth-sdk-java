package io.bitwise.sawtooth_xo

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.bitwise.sawtooth_xo.adapters.GameListRecyclerViewAdapter
import io.bitwise.sawtooth_xo.models.Game
import io.bitwise.sawtooth_xo.viewmodels.GameViewModel


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [GameListFragment.OnListFragmentInteractionListener] interface.
 */
class GameListFragment : Fragment() {

    private var listener: OnListFragmentInteractionListener? = null
    private var model: GameViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = activity?.run {
            ViewModelProviders.of(this).get(GameViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

                layoutManager = LinearLayoutManager(context)
                adapter = GameListRecyclerViewAdapter(arrayListOf(), listener)
            }
            model?.games?.observe(this, Observer<List<Game>>{ games ->
                if(games != null) {
                    val adapter = view.adapter as GameListRecyclerViewAdapter
                    adapter.updateData(filterGameList(games))
                }
            })
        }
        return view
    }

    private fun filterGameList(games: List<Game>) : List<Game> {
        val filter = arguments?.getString("listFilter")
        val publicKey = arguments?.getString("publicKey")
        when {
            filter.equals(getString(R.string.PlayTab)) -> {
                return games.filter { game -> (userCanJoinGame(game, publicKey) ||
                        userIsInGame(game, publicKey)) && !gameIsOver(game.gameState) }
            }
            filter.equals(getString(R.string.WatchTab)) -> {
                return games.filter { game -> !userIsInGame(game, publicKey) && !userCanJoinGame(game, publicKey)
                        && !gameIsOver(game.gameState)}
            }
            filter.equals(getString(R.string.HistoryTab)) -> {
                return games.filter { game -> gameIsOver(game.gameState) }
            }
        }

        return arrayListOf()

    }

    private fun gameIsOver(gameState: String) : Boolean{
        return gameState == "P1-WIN" ||  gameState == "P2-WIN" ||  gameState == "TIE"
    }

    private fun userIsInGame(game: Game, publicKey: String?) : Boolean {
        return game.playerKey1 == publicKey || game.playerKey2 == publicKey
    }

    private fun userCanJoinGame(game: Game, publicKey: String?) : Boolean {
        return  game.playerKey1.isBlank() || (game.playerKey2.isBlank() && game.playerKey1 != publicKey)
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: Game?)
    }
}
