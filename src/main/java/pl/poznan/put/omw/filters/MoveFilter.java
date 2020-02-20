package pl.poznan.put.omw.filters;

import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

public interface MoveFilter {
    /**
     * @param FEN
     * @param uciMove
     * @return
     */
    boolean match(String FEN, String uciMove) throws MoveConversionException, MoveGeneratorException;
}
