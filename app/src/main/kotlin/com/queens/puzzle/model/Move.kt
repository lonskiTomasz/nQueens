package com.queens.puzzle.model

sealed interface Move {

    val position: Position

    data class Place(override val position: Position) : Move

    data class Remove(override val position: Position) : Move
}
