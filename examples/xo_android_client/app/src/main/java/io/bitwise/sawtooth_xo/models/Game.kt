package io.bitwise.sawtooth_xo.models


class Game( var name: String,
            var board: String,
            var gameState: String,
            var playerKey1: String,
            var playerKey2: String) {

    override fun toString(): String {
        return "Game(name=$name, board=$board, gameState=$gameState, playerKey1=$playerKey1, playerKey2=$playerKey2)"
    }
}
