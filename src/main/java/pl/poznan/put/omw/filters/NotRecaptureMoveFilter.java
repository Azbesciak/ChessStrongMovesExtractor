package pl.poznan.put.omw.filters;

/**
 * The best move is not a simple (re-)capture of a piece with value missing for material equality.
 */
public class NotRecaptureMoveFilter implements MoveFilter {

    @Override
    public boolean match(String FEN, String move) {
        // TODO
        return false;
    }
}
