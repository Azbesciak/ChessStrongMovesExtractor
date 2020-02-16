package pl.poznan.put.omw.filters;

public interface MoveFilter {
    boolean match(String FEN, String move);
}
