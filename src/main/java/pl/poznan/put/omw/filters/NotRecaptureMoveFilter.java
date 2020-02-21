package pl.poznan.put.omw.filters;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import pl.poznan.put.omw.ChessLibUtils;

/**
 * The best move is not a simple (re-)capture of a piece with value missing for material equality.
 */
public class NotRecaptureMoveFilter extends BasicMoveFilter {

    @Override
    public boolean match(String FEN, String uciMove) throws MoveConversionException {
        Board board = new Board();
        board.loadFromFen(FEN);
        Move move = ChessLibUtils.getMoveFromUCI(board, uciMove);
        int myMaterial = ChessLibUtils.getMyMaterial(board);
        if (myMaterial < ChessLibUtils.getOpponentMaterialBeforeMove(board)
                && myMaterial >= ChessLibUtils.getOpponentMaterialAfterMove(board, move)) {
            return false;
        } else {
            return true;
        }
    }
}
