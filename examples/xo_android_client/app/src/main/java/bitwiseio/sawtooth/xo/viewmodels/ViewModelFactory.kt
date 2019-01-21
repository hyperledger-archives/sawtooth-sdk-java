package bitwiseio.sawtooth.xo.viewmodels

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class ViewModelFactory(private val restApiUrl: String) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            GameViewModel::class.java -> GameViewModel(restApiUrl) as T
            GameBoardViewModel::class.java -> GameBoardViewModel(restApiUrl) as T
            else -> null as T
        }
    }
}