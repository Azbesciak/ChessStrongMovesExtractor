package pl.poznan.put.omw.filters;

/**
 * The best move is not just a capture by a minor piece, leading to material advantage.
 */
public class NotMinorCaptureMoveFilter implements MoveFilter {

    @Override
    public boolean match(String FEN, String move) {
        // TODO
        return false;
    }
}
