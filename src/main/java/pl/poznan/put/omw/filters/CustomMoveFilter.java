package pl.poznan.put.omw.filters;

import com.github.bhlangonijr.chesslib.move.MoveConversionException;

/**
 * Our brilliant idea.
 */
public class CustomMoveFilter extends BasicMoveFilter {

    /**
     * @param cpDifference difference between best and second best in centipawns
     */
    public CustomMoveFilter(int cpDifference) {
        super(cpDifference);
    }

    @Override
    public boolean match(String FEN, String uciMove) throws MoveConversionException {
        return false;
    }
}
