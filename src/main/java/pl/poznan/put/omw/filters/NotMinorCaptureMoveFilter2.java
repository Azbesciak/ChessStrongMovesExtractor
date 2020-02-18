package pl.poznan.put.omw.filters;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import pl.poznan.put.omw.ChessLibUtils;

/**
 * The best move is not just a capture by a minor piece, leading to material advantage.
 * <p>
 * Second interpretation: Ruch to nie jest bicie piona, które prowadzi do przewagi materiału
 */
public class NotMinorCaptureMoveFilter2 extends BasicMoveFilter {

    public NotMinorCaptureMoveFilter2(int cpDifference) {
        super(cpDifference);
    }

    @Override
    public boolean match(String FEN, String move) throws MoveConversionException {
        Board board = new Board();
        board.loadFromFen(FEN);
        boolean isCapture = ChessLibUtils.isMoveACapture(board, move);
        if (isCapture) {
            int opponentMaterialDifference = ChessLibUtils.getOpponentMaterialDifferenceAfterMove(board, move);
            if (opponentMaterialDifference == 100 // bicie piona
                    && ChessLibUtils.isMaterialAdvantageAfterMove(board, move)) { // jest przewaga materialu po biciu
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
