package pl.poznan.put.omw.filters;

import com.github.bhlangonijr.chesslib.move.MoveConversionException;

public interface MoveFilter {
    /**
     *
     * @param FEN
     * @param move
     * @return
     */
    boolean match(String FEN, String move) throws MoveConversionException;
}
