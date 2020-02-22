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

        chess.addObserver { o, arg ->
            if (arg != ObservableChessGame.ACTION_NEW_MOVE) return@addObserver
            if (moveCounter >= movesToCheck) return@addObserver

            val pan = ChessGameUtils.getChessGameMovesAsPAN((o as ObservableChessGame).chessGame) ?: return@addObserver
            val nextFen = getFenFromMoves(pan) ?: return@addObserver
            val movePlayedInGame = pan.split(" ").last { it.isNotEmpty() }

            runBlocking {
                suspendCoroutine<Boolean> { continuation ->
                    var counter = 0
                    var lastBestmoveIndex = -1
                    launch {
                        logger.debug("sending move $pan in FEN: $nextFen")
                        lateinit var cancellation: Cancellation
                        cancellation = gameConnection.nextPosition(nextFen) {
                            val responseType = EngineResult.getReponseType(it)
                            val responseDepth = EngineResult.getResponseDepth(it)

                            when (responseType) {
                                ResultType.Move -> {
                                    if (responseDepth == maxDepth)
                                        result.add(EngineResult(nextFen, movePlayedInGame, it, moveCounter))
                                }
                                ResultType.BestMove -> {
                                    result.add(EngineResult(nextFen, movePlayedInGame, it, moveCounter, isBestMove = true))
                                    assignCPToBestmove(result, lastBestmoveIndex)
                                    lastBestmoveIndex = result.size - 1
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

    private fun assignCPToBestmove(result: List<EngineResult>, lastBest: Int) {
        // assign cp to bestmoves - engine doesn't return cp to bestmove
        val i = result.size - 1 // bestmove position
        // result with bestmove as the first move should be before bestmove in the result list
        for (j in result.size - 2 downTo lastBest + 1) {
            if (result[j].getMove() == result[i].getMove()) {
                // result with the same move as bestmove is found
                // assign cp to bestmove
                result[i].centipaws = result[j].centipaws
                result[i].depth = result[j].depth
                break
            }
        }
    }
}
