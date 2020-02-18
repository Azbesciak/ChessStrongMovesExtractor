package pl.poznan.put.omw.filters;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import pl.poznan.put.omw.ChessLibUtils;

/**
 * The best move is not just a capture by a minor piece, leading to material advantage.
 */
public class NotMinorCaptureMoveFilter extends BasicMoveFilter {

    public NotMinorCaptureMoveFilter(int cpDifference) {
        super(cpDifference);
    }

    @Override
    public boolean match(String FEN, String move) throws MoveConversionException {
        Board board = new Board();
        board.loadFromFen(FEN);
        boolean isCapture = ChessLibUtils.isMoveACapture(board, move);
        if (isCapture) {
            int materialAdvantage = ChessLibUtils.getOpponentMaterialDifferenceAfterMove(board, move);
            if (cpDifference > materialAdvantage * 3) { // TODO what exactly MINOR means?
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
