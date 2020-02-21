package pl.poznan.put.omw.filters;

import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

public interface MoveFilter {
    /**
     * @param FEN FEN game notation
     * @param uciMove move in UCI format [from-to]
     * @return true if move matches criteria
     */
    boolean match(String FEN, String uciMove) throws MoveConversionException, MoveGeneratorException;
}
