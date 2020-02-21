package pl.poznan.put.omw

import pl.poznan.put.omw.filters.MoveFilter

class GameFilter(private val engineResult: List<EngineResult>,
                 private val filters: List<MoveFilter>) {
    init {
        //the best move (the biggest number of CP) at the beginning
        engineResult.sortedByDescending {
            it.centipaws
        }
    }

    fun filterInterestingMoves(): List<EngineResult> {
        val interestingMoves = arrayListOf<EngineResult>()

        engineResult.forEach { result ->
            run {
                // TODO implement filtering interesting moves
                interestingMoves.add(result)
//                var interestingMove = true
//                for (filter in filters) {
//                    val board = Board()
//                    board.loadFromFen(result.fen)
//
//                    for (move in result.moves) {
//                        if (!filter.match(result.fen, move)) {
//                            interestingMove = false
//                            break
//                        }
//                    }
//                    if(!interestingMove)
//                        break
//                }
//                if (interestingMove)
//                    interestingMoves.add(result)
            }
        }

        return interestingMoves
    }

    /**
     * Returns at most numberOfBestResults engine's results.
     */
    fun getNBestResults(numberOfBestResults: Int) =
            engineResult
                    .filter { !it.isBestMove }
                    .take(numberOfBestResults)

    /**
     * Returns at most numberOfBestResults best moves in UCI format returned by engine.
     */
    fun getNBestMoves(numberOfBestMoves: Int) =
            getNBestResults(numberOfBestMoves)
                    .map { it.getMove() }
}
