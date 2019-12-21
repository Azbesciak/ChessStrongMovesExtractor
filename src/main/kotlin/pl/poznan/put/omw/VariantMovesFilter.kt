package pl.poznan.put.omw

import chess.parser.Entity
import chess.parser.VariantBegin
import chess.parser.VariantEnd
import chess.parser.pgn.PGNGame

object VariantMovesFilter {
    fun filter(games: List<PGNGame>) = games.map {
        PGNGame(it.meta, it.entities.filterVariantMoves()).apply {
            id = it.id
        }
    }

    private fun List<Entity>.filterVariantMoves(): List<Entity> {
        var variantNestingCounter = 0
        return filter {
            when (it) {
                is VariantBegin -> {
                    variantNestingCounter++
                    false
                }
                is VariantEnd -> {
                    variantNestingCounter--
                    false
                }
                else -> variantNestingCounter == 0
            }
        }
    }
}
