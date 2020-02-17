package pl.poznan.put.omw.filters;

import com.github.bhlangonijr.chesslib.move.MoveConversionException;

/**
 * The best move is not a simple (re-)capture of a piece with value missing for material equality.
 */
public class NotRecaptureMoveFilter extends BasicMoveFilter {

    public NotRecaptureMoveFilter(int cpDifference) {
        super(cpDifference);
    }

    @Override
    public boolean match(String FEN, String move) throws MoveConversionException {
        return false;
    }
}
