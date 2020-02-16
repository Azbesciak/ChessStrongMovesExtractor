package pl.poznan.put.omw;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import com.github.bhlangonijr.chesslib.move.MoveList;

public class ChessLibPlayground {

    public static void main(String args[]) throws MoveConversionException {
        // Creates a new chessboard in the standard initial position
        Board board = new Board();
        String fen = "8/p5Q1/2ppq2p/3n1ppk/3B4/2P2P1P/P5P1/6K1 w - - 3 46";

        board.loadFromFen(fen);

        String san = "g4";

        MoveList moves = new MoveList();
        moves.loadFromSan(san);
        for (Move move : moves) {
            board.doMove(move);
        }
        System.out.println(board.getFen());

    }
}
