package pl.poznan.put.omw

import chess.parser.ChessGameUtils
import chess.parser.ObservableChessGame
import chess.parser.PossibleMovesProviderImpl
import chess.parser.SANMoveMaker
import chess.parser.pgn.PGNGame
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.MoveList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KLogging
import kotlin.coroutines.suspendCoroutine

class GamePlayer(private val game: PGNGame, private val gameConnection: GameConnection) {
    private companion object : KLogging()

    // How many moves from pgn should be checked
    private val movesToCheck = 10

    fun play(maxDepth: Int): List<EngineResult> {
        val chess = ObservableChessGame()
        var moveCounter = 0
        val result = ArrayList<EngineResult>()

        chess.addObserver { o, arg ->
            if (arg != ObservableChessGame.ACTION_NEW_MOVE) return@addObserver
            if (moveCounter >= movesToCheck) return@addObserver

            val pan = ChessGameUtils.getChessGameMovesAsPAN((o as ObservableChessGame).chessGame)
            val nextFen = getFenFromMoves(pan)

            runBlocking {
                suspendCoroutine<Boolean> { continuation ->
                    var counter = 0
                    launch {
                        logger.debug("sending move in FEN: $nextFen")
                        lateinit var cancellation: Cancellation
                        cancellation = gameConnection.nextPosition(nextFen) {
                            // here comes engine response
                            // should be only for this position BUT need to wait for the end
                            // engine is sequential, without id or something like this - new request cancels this one

                            val responseType = EngineResult.getReponseType(it)
                            val responseDepth = EngineResult.getResponseDepth(it)

                            when (responseType) {
                                ResultType.Move ->
                                {
                                    if(responseDepth == maxDepth)
                                        result.add(EngineResult(nextFen, it))
                                }
                                ResultType.BestMove -> result.add(EngineResult(nextFen, it, isBestMove = true))
                            }

                            if(responseType == ResultType.BestMove || responseType == ResultType.Move)
                            {
                                logger.info("received response message for move $moveCounter: $it")
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

    // translate from pan to FEN
    private fun getFenFromMoves(pan: String) =
            Board().let {
                Board().clear()
                val moves = MoveList()
                moves.loadFromSan(pan)
                moves.forEach { move ->
                    it.doMove(move)
                }
                // list all moves that created the fen
                logger.debug("Moves: ${moves.toSan()}")

                it.fen
            }
}
