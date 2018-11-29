package io.bitwise.sawtooth_xo.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import io.bitwise.sawtooth_xo.models.Game
import io.bitwise.sawtooth_xo.state.XoStateRepository

class GameViewModel : ViewModel(){
    private val stateRepository: XoStateRepository = XoStateRepository()
    var games: LiveData<List<Game>> = Transformations.switchMap(stateRepository.games) { input ->
        var m = MutableLiveData<List<Game>>()
        m.value = input
        m
    }

    init {
        stateRepository.getState(true)
    }

    fun loadGames(update: Boolean) {
      stateRepository.getState(update)
    }
}
