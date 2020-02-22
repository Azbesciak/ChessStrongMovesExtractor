package pl.poznan.put.omw

import pl.poznan.put.omw.filters.MoveFilter
import kotlin.math.abs

class GameFilter(private val engineResult: List<EngineResult>,
                 private val filters: List<MoveFilter>) {
    init {
        //the best move (the biggest number of CP) at the beginning
        engineResult.sortedByDescending {
            it.centipaws
        }
    }

    fun filterInterestingMoves(depth: Int, minCPBetweenBest: Int): List<EngineResult> {
        val interestingMoves = arrayListOf<EngineResult>()

        engineResult
                .filter { it.isBestMove && depth == it.depth }
                .forEach {bestMove ->
                    run {
                        if(bestMove.secondBestIndex == -2 ||
                                abs(bestMove.centipaws) - abs(engineResult[bestMove.secondBestIndex].centipaws) > minCPBetweenBest)
                        {
                            // difference between bestmove and second bestmove is greater then minCPBetweenBest
                            // or there was only one result from the engine
                            // so we can apply filters
                            var matchesAllFilters = true
                            for (filter in filters) {
                                matchesAllFilters = matchesAllFilters && filter.match(bestMove.fen, bestMove.getMove())
                            }
                            if(matchesAllFilters)
                                interestingMoves.add(bestMove)
                        }
                    }
                }


        return interestingMoves
    }

//    private fun getSecondBest(bestIndex: Int): EngineResult {
//        for (i in bestIndex downTo 0)
//        {
//            if (engineResult[i].isBestMove)
//            {
//                // result with bestmove as the first move should be before bestmove in the result list
//                for (j in i-1 downTo lastBest+1)
//                {
//                    if(results[j].getMove() == results[i].getMove())
//                    {
//                        // result with the same move as bestmove is found
//                        // assign cp to bestmove
//                        results[i].centipaws = results[j].centipaws
//                        results[i].depth = results[j].depth
//                        break
//                    }
//                }
//                lastBest = i
//            }
//        }
//    }

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
