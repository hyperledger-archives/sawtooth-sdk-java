package io.bitwise.sawtooth_xo.models

class Game {
    var name: String? = null
    var board: String? = null
    var gameState: String? = null
    var playerKey1: String? = null
    var playerKey2: String? = null

    constructor(state: String) {
        val split = state.split(',')
        name = split[0]
        board = split[1]
        gameState = split[2]
        playerKey1 = split[3]
        playerKey2 = split[4]
    }

    override fun toString(): String {
        return "Game(name=$name, board=$board, gameState=$gameState, playerKey1=$playerKey1, playerKey2=$playerKey2)"
    }
}
