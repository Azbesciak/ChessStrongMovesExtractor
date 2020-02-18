package pl.poznan.put.omw

import pl.poznan.put.omw.filters.MoveFilter

class GameFilter(private val engineResult: List<EngineResult>,
                 private val filters: List<MoveFilter>) {
    fun filterInterestingMoves(): List<EngineResult> {
        val interestingMoves = arrayListOf<EngineResult>()

        engineResult.forEach { result ->
            run {
                var interestingMove = true
                for (filter in filters) {
                    for (move in result.moves) {
                        if (!filter.match(result.fen, move)) {
                            interestingMove = false
                            break
                        }
                    }
                    if(!interestingMove)
                        break
                }
                if (interestingMove)
                    interestingMoves.add(result)
            }
        }

        return interestingMoves
    }
}