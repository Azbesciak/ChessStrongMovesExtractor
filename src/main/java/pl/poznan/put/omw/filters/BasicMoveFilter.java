package pl.poznan.put.omw.filters;

public abstract class BasicMoveFilter implements MoveFilter {
    public BasicMoveFilter(int cpDifference)
    {
        this.cpDifference = cpDifference;
    }

    protected int cpDifference;
}
