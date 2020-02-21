package pl.poznan.put.omw.filters;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import pl.poznan.put.omw.ChessLibUtils;

/**
 * The best move is not just a capture by a minor piece, leading to material advantage.
 * <p>
 * Interpretation: Ruch to nie jest bicie piona, które prowadzi do przewagi materiału
 */
public class NotMinorCaptureMoveFilter extends BasicMoveFilter {
    @Override
    public boolean match(String FEN, String uciMove) throws MoveConversionException {
        Board board = new Board();
        board.loadFromFen(FEN);
        Move move = ChessLibUtils.getMoveFromUCI(board, uciMove);
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
