package pl.poznan.put.omw.filters;

import com.github.bhlangonijr.chesslib.move.MoveConversionException;

public interface MoveFilter {
    /**
     *
     * @param FEN
     * @param move
     * @param cpDifference difference between best and second best in centipawns
     * @return
     */
    boolean match(String FEN, String move, int cpDifference) throws MoveConversionException;
}
