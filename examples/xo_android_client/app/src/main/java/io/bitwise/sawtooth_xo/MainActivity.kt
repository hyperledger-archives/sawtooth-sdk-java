package io.bitwise.sawtooth_xo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab: FloatingActionButton = findViewById(R.id.newGameFloatingButton)
        fab.setOnClickListener { view ->
            val intent = Intent(this, GameBoardActivity::class.java)
            startActivity(intent)
        }

    }
}
