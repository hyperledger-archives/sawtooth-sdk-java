package io.bitwise.sawtooth_xo.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import io.bitwise.sawtooth_xo.models.Game
import io.bitwise.sawtooth_xo.state.XoState

class GameViewModel : ViewModel(){
    private val state: XoState = XoState()
    var games: LiveData<List<Game>> = Transformations.switchMap(state.games) {input ->
        var m = MutableLiveData<List<Game>>()
        m.value = input
        m
    }

    init {
        state.getState(true)
    }

    fun loadGames(update: Boolean) {
      state.getState(update)
    }
}

