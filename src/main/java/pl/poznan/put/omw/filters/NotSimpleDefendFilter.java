package pl.poznan.put.omw.filters;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import pl.poznan.put.omw.ChessLibUtils;

/**
 * The best move is not a simple escape from mate.
 */
public class NotSimpleDefendFilter extends BasicMoveFilter {

    public NotSimpleDefendFilter(int cpDifference) {
        super(cpDifference);
    }

    @Override
    public boolean match(String FEN, String uciMove) throws MoveConversionException {
        Board board = new Board();
        board.loadFromFen(FEN);
        Move move = ChessLibUtils.getMoveFromUCI(board, uciMove);
        if (board.isKingAttacked() && ChessLibUtils.didKingMove(board, move)) {
            return false;
        } else {
            return true;
        }

    }
}
