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

    fun filterInterestingMoves(depth: Int): List<EngineResult> {
        val interestingMoves = arrayListOf<EngineResult>()

        engineResult
                .filter { it.depth == depth }
                .forEach {bestMove ->
                    run {
                        var matchesAllFilters = true
                        for (filter in filters) {
                            matchesAllFilters = matchesAllFilters && filter.match(bestMove.fen, bestMove.getMove())
                        }
                        if(matchesAllFilters)
                            interestingMoves.add(bestMove)
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
