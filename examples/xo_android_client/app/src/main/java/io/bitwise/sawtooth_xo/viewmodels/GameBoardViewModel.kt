package io.bitwise.sawtooth_xo.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import io.bitwise.sawtooth_xo.models.Game
import io.bitwise.sawtooth_xo.state.XoStateRepository


class GameBoardViewModel : ViewModel() {
    private val stateRepository: XoStateRepository = XoStateRepository()
    var game: LiveData<Game> = Transformations.switchMap(stateRepository.gameFocus) { input ->
        var m = MutableLiveData<Game>()
        m.value = input
        m
    }

    init {  }

    fun loadGame(name: String) {
        stateRepository.getGameState(name)
        game = stateRepository.gameFocus
    }
}
