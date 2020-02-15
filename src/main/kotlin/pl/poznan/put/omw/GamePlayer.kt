package pl.poznan.put.omw

import chess.parser.ChessGameUtils
import chess.parser.ObservableChessGame
import chess.parser.PossibleMovesProviderImpl
import chess.parser.SANMoveMaker
import chess.parser.pgn.PGNGame

class GamePlayer(private val game: PGNGame, private val gameConnection: GameConnection) {
    fun play() {
        val chess = ObservableChessGame()
        chess.addObserver { o, arg ->
            if (arg != ObservableChessGame.ACTION_NEW_MOVE) return@addObserver
            val pan = ChessGameUtils.getChessGameMovesAsPAN((o as ObservableChessGame).chessGame)
            gameConnection.nextPosition("fen position") {
                // here comes engine response
                // should be only for this position BUT need to wait for the end
                // engine is sequential, without id or something like this - new request cancels this one
                println(it)
            }
        }
        val possibleMovesProvider = PossibleMovesProviderImpl(chess)
        val sanMoveMaker = SANMoveMaker(chess, possibleMovesProvider)
        sanMoveMaker.processMoves(game.entities)
    }
}
