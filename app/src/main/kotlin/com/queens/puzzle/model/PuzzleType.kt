package com.queens.puzzle.model

import kotlinx.serialization.Serializable

/** Which piece is in play, and therefore which [com.queens.puzzle.domain.rules.PieceRules] apply. */
@Serializable
enum class PuzzleType {
    Queens,
    Knights,
}
