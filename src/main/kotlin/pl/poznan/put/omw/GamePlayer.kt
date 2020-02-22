package pl.poznan.put.omw

import chess.parser.ChessGameUtils
import chess.parser.ObservableChessGame
import chess.parser.PossibleMovesProviderImpl
import chess.parser.SANMoveMaker
import chess.parser.pgn.PGNGame
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.MoveConversionException
import com.github.bhlangonijr.chesslib.move.MoveList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KLogging
import kotlin.coroutines.suspendCoroutine

class GamePlayer(private val game: PGNGame, private val gameConnection: GameConnection) {
    private companion object : KLogging()

    // How many moves from pgn should be checked
    // some of game.entities are not moves, so this the max number of moves
    private val movesToCheck = game.entities.size

    fun play(maxDepth: Int): List<EngineResult> {
        val chess = ObservableChessGame()
        var moveCounter = 0
        val result = ArrayList<EngineResult>()
        var isWhitePlayerMove = true // assuming that WHITE starts
        var lastBestmoveIndex = -1

        chess.addObserver { o, arg ->
            if (arg != ObservableChessGame.ACTION_NEW_MOVE) return@addObserver
            if (moveCounter >= movesToCheck) return@addObserver

            val pan = ChessGameUtils.getChessGameMovesAsPAN((o as ObservableChessGame).chessGame) ?: return@addObserver
            val nextFen = getFenFromMoves(pan) ?: return@addObserver
            val movePlayedInGame = pan.split(" ").last { it.isNotEmpty() }

            runBlocking {
                suspendCoroutine<Boolean> { continuation ->
                    var counter = 0
                    launch {
                        logger.debug("sending move $pan in FEN: $nextFen")
                        lateinit var cancellation: Cancellation
                        cancellation = gameConnection.nextPosition(nextFen) {
                            val responseType = EngineResult.getReponseType(it)
                            val responseDepth = EngineResult.getResponseDepth(it)

                            when (responseType) {
                                ResultType.Move -> {
                                    if (responseDepth == maxDepth)
                                        result.add(EngineResult(nextFen, movePlayedInGame, it, moveCounter, isWhitePlayerMove))
                                }
                                ResultType.BestMove -> {
                                    result.add(EngineResult(nextFen, movePlayedInGame, it, moveCounter, isWhitePlayerMove,  isBestMove = true))
                                    assignCPAndSecondBestmoveToBestmove(result, lastBestmoveIndex, isWhitePlayerMove)
                                    lastBestmoveIndex = result.size - 1
                                    // player changes
                                    isWhitePlayerMove = !isWhitePlayerMove
                                }
                            }

                            if (responseType == ResultType.BestMove || responseType == ResultType.Move) {
                                logger.debug("received response message for move $moveCounter: $it")
                            }


                            //when the current response
                            // is bestmove then close the connection
                            if (responseType == ResultType.BestMove) {
                                logger.debug("closing move response for move $moveCounter: $nextFen")
                                cancellation()
                                continuation.resumeWith(Result.success(true))
                            }
                            ++counter
                        }
                    }
                }
            }
            moveCounter++
        }
        val possibleMovesProvider = PossibleMovesProviderImpl(chess)
        val sanMoveMaker = SANMoveMaker(chess, possibleMovesProvider)
        sanMoveMaker.processMoves(game.entities)
        println("EXITING!")
        return result
    }

    /**
     * Create FEN based on moves in pan.
     */
    private fun getFenFromMoves(pan: String) =
            Board().let {
                val moves = MoveList()
                try {
                    moves.loadFromSan(pan)
                } catch (e: MoveConversionException) {
                    return null
                }

                moves.forEach { move ->
                    it.doMove(move)
                }
                // list all moves that created the fen
                logger.debug("Moves: ${moves.toSan()}")

                it.fen
            }

    /**
     * Assuming that bestmove comes as the last one so we have all moves with the same ID (comming from the same FEN)
     * already in the result list. By keeping lastBest value we are iterating only within results with the same id.
     */
    private fun assignCPAndSecondBestmoveToBestmove(result: List<EngineResult>, lastBest: Int, isWhitePlayerMove: Boolean) {
        // assign cp to bestmoves - engine doesn't return cp to bestmove
        val bestmoveIndex = result.size - 1 // bestmove position
        val values = arrayListOf<Pair<Int, Int>>() // keeps cp of all results and their indices
        // result with bestmove as the first move should be before bestmove in the result list
        for (j in result.size - 2 downTo lastBest + 1) {
            if (result[j].getMove() == result[bestmoveIndex].getMove() && result[bestmoveIndex].depth < 0) {
                // result with the same move as bestmove is found
                // assign cp to bestmove
                result[bestmoveIndex].centipaws = result[j].centipaws
                result[bestmoveIndex].depth = result[j].depth
            } else {
                // add only those results that are not the best move
                values.add(Pair(result[j].centipaws, j))
            }

        }

        if(values.size == 0)
        {
            // there was only one response from the server
            result[bestmoveIndex].secondBestIndex = -2
        }
        else
        {
            if(isWhitePlayerMove)
            {
                // sort by cp - the highest cp the best
                values.sortByDescending { it.first }
            }
            else
            {
                // black's move - the lowest cp the best
                values.sortBy { it.first }
            }

            // assign id of the second best to the bestmove
            result[bestmoveIndex].secondBestIndex = values[0].second
        }

    }
}
