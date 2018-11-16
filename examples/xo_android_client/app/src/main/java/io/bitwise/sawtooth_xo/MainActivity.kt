package io.bitwise.sawtooth_xo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.bitwise.sawtooth_xo.state.XoState
import java.util.UUID


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val state =  XoState()
        state.createGame("game-" + UUID.randomUUID().toString())
        setContentView(R.layout.activity_main)
    }
}
