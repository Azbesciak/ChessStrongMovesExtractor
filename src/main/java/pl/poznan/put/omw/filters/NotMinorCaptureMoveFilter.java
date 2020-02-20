package pl.poznan.put.omw.filters;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import pl.poznan.put.omw.ChessLibUtils;

/**
 * The best move is not just a capture by a minor piece, leading to material advantage.
 * <p>
 * First interpretation: Ruch to nie jest bicie, które daje przewagę wynikającą z tego bicia
 * (nie jest biciem albo jest biciem, które daje lepszą ocenę niż wynika z materialu utraconego przez przeciwnika w trakcie bicia)
 */
public class NotMinorCaptureMoveFilter extends BasicMoveFilter {

    public NotMinorCaptureMoveFilter(int cpDifference) {
        super(cpDifference);
    }

    @Override
    public boolean match(String FEN, String uciMove) throws MoveConversionException {
        Board board = new Board();
        board.loadFromFen(FEN);
        Move move = ChessLibUtils.getMoveFromUCI(board, uciMove);
        boolean isCapture = ChessLibUtils.isMoveACapture(board, move);
        if (isCapture) {
            int opponentMaterialDifference = ChessLibUtils.getOpponentMaterialDifferenceAfterMove(board, move);
            if (cpDifference > opponentMaterialDifference * 2) { // TODO what exactly MINOR means?
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
