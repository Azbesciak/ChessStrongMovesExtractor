import chess.parser.GameBegin
import chess.parser.Move
import chess.parser.VariantBegin
import chess.parser.VariantEnd
import chess.parser.pgn.Meta
import chess.parser.pgn.PGNGame
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import pl.poznan.put.omw.VariantMovesFilter

class VariantMovesFilterTest {

    @Test
    fun filter() {
        val initial = PGNGame(
                listOf(Meta("a", "1"), Meta("b", "2")),
                listOf(
                        start,
                        move(1),
                        move(2),
                        begin,
                        move(3),
                        begin,
                        move(4),
                        move(5),
                        end,
                        end,
                        move(6),
                        move(7),
                        begin,
                        end,
                        move(8)
                )
        )
        val expectedMoviesIndices = listOf(1, 2, 6, 7, 8)
        val result = VariantMovesFilter.filter(listOf(initial))
        assertAll({
            assertEquals(1, result.size) { "expected only one entry as initial" }
        }, {
            assertTrue(result.first().entities.none { it is VariantEnd || it is VariantBegin }) {
                "should not find any variant entities"
            }
        }, {
            val movesOnly = result.first().entities.filterIsInstance(Move::class.java).map { it.no }
            assertEquals(expectedMoviesIndices, movesOnly) { "different moves in result" }
        }, {
            assertEquals(initial.meta, result.first().meta) { "meta was changed" }
        })
    }

    private fun move(id: Int) = Move().apply { no = id }
    private val begin get() = VariantBegin()
    private val start get() = GameBegin()
    private val end get() = VariantEnd()
}
