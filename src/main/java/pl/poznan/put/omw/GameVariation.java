package pl.poznan.put.omw;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;

import java.util.ArrayList;

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

    //TODO skąd wziąć SAN???
    public GameVariation(String uciMove, int id, int centipawns, String fen, ArrayList<String> sanList) throws MoveConversionException {
        Board board = new Board();
        board.loadFromFen(fen);
        this.sanMoveRepresentation = ChessLibUtils.getSANFromUCI(fen, uciMove);
        this.index = id;
        this.centipawns = centipawns;
        if (sanList.get(id) == this.sanMoveRepresentation) {
            this.wasPlayed = true;
        } else {
            this.wasPlayed = false;
        }
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

    private String getWasPlayedString() {
        return wasPlayed ? "{G}" : "";
    }
}
