package pl.poznan.put.omw;

public class GameVariation {
    String sanMoveRepresentation;
    boolean wasPlayed;
    boolean isBestMove;
    int index;
    int centipawns;

    public GameVariation(String sanMoveRepresentation, boolean wasPlayed, boolean isBestMove, int index, int centipawns) {
        this.sanMoveRepresentation = sanMoveRepresentation;
        this.wasPlayed = wasPlayed;
        this.isBestMove = isBestMove;
        this.index = index;
        this.centipawns = centipawns;
    }

    @Override
    public String toString() {
        String leftCommentBracket = isBestMove ? "" : "(";
        String rightCommentBracket = isBestMove ? "" : ")";
        return leftCommentBracket
                + index + ". " + sanMoveRepresentation
                + " {" + getCentipawnsWithSign() + "}"
                + getWasPlayedString()
                + rightCommentBracket;
    }

    private String getCentipawnsWithSign() {
        return centipawns >= 0 ? "+" + centipawns : String.valueOf(centipawns);
    }

    private String getWasPlayedString(){
        return wasPlayed ? "{G}" : "";
    }
}
