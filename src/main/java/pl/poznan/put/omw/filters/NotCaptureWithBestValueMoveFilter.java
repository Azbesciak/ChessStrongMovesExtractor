package pl.poznan.put.omw.filters;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.*;
import pl.poznan.put.omw.ChessLibUtils;

/**
 * The best move is not a capture with greater material gain than any other move (strict inequality).
 */
public class NotCaptureWithBestValueMoveFilter extends BasicMoveFilter {

    public NotCaptureWithBestValueMoveFilter(int cpDifference) {
        super(cpDifference);
    }

    @Override
    public boolean match(String FEN, String move) throws MoveConversionException, MoveGeneratorException {
        Board board = new Board();
        board.loadFromFen(FEN);
        MoveList legalMoves = MoveGenerator.generateLegalMoves(board);

        MoveList moves = new MoveList();
        moves.loadFromSan(move);
        Move playedMove = moves.get(0);
        // TODO finish this
        return true;

//        for(Move legalMove : legalMoves) {
//            ChessLibUtils.getOpponentMaterialDifferenceAfterMove(FEN, move);
//        }

    }
}
