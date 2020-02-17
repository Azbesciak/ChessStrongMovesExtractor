package pl.poznan.put.omw.filters;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import com.github.bhlangonijr.chesslib.move.MoveList;

/**
 * The best move is not just a capture by a minor piece, leading to material advantage.
 */
public class NotMinorCaptureMoveFilter extends BasicMoveFilter {

    public NotMinorCaptureMoveFilter(int cpDifference) {
        super(cpDifference);
    }

    @Override
    public boolean match(String FEN, String move) throws MoveConversionException {
        // TODO
        boolean isCapture = true;
        if (isCapture) {
            Board board = new Board();
            board.loadFromFen(FEN);
            MoveList moves = new MoveList();

            moves.loadFromSan(move);
            board.doMove(moves.get(0));

            int materialAdvantage = 10; //TODO
            if (cpDifference > materialAdvantage * 3) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
