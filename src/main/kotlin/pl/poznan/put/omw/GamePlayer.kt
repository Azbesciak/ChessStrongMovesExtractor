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

    fun play() {
        val chess = ObservableChessGame()
        var moveCounter = 0
        val exampleFenMoves = arrayOf(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
                "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2",
                "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2"
        )
        chess.addObserver { o, arg ->
            if (arg != ObservableChessGame.ACTION_NEW_MOVE) return@addObserver
            println("STILL PROCESSING...")
            if (moveCounter >= exampleFenMoves.size) return@addObserver
            val pan = ChessGameUtils.getChessGameMovesAsPAN((o as ObservableChessGame).chessGame)
            // translate from pan to FEN
            val board = Board()
            val moves = MoveList()
            moves.loadFromSan(pan)
            for (move in moves) {
                board.doMove(move)
            }
            val fen = board.fen
            println("FEN: " + fen)

            runBlocking {
                val result = suspendCoroutine<Boolean> { continuation ->
                    var counter = 0
                    launch {
                        val move = fen //exampleFenMoves[moveCounter]
                        logger.debug("sending move in FEN: $move")
                        lateinit var cancellation: Cancellation
                        cancellation = gameConnection.nextPosition(move) {
                            // here comes engine response
                            // should be only for this position BUT need to wait for the end
                            // engine is sequential, without id or something like this - new request cancels this one
                            logger.info("received response message for move $moveCounter: $it")
                            if (counter > 15) { // TODO adjust how many massages to consume in order to get all best moves and their centipawns
                                logger.debug("closing move response for move $moveCounter: $move")
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
    }
}
