package pl.poznan.put.omw;

import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import kotlin.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameVariation {
    String sanMoveRepresentation;
    String uclMoveRepresentation;
    boolean wasPlayed;

    public void setBestMove(boolean bestMove) {
        isBestMove = bestMove;
    }

    boolean isBestMove;
    String fen;

    public int getIndex() {
        return index;
    }

    int index;
    int centipawns;

    public GameVariation(EngineResult result)
    {
            this(result.getSANMove(),
                    result.wasPlayedInGame(),
                    result.isBestMove(),
                    result.getMoveID(),
                    result.getCentipaws(),
                    result.getFen());
    }

    public GameVariation(String sanMoveRepresentation, boolean wasPlayed, boolean isBestMove, int index, int centipawns, String fen) {
        this.sanMoveRepresentation = sanMoveRepresentation;

        this.wasPlayed = wasPlayed;
        this.isBestMove = isBestMove;
        this.index = index;
        this.centipawns = centipawns;
        this.fen = fen;
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

    public static Map<Integer, List<GameVariation>> createGameVariationList(List<Pair<EngineResult, List<EngineResult>>> results) throws MoveConversionException {
        HashMap<Integer, List<GameVariation>> grouppedMap = new HashMap<>();
        for (Pair<EngineResult, List<EngineResult>> bestResultWithVariants : results) {
            ArrayList<GameVariation> variations = new ArrayList<>();
            // add bestmove as the first variation
            variations.add(new GameVariation(bestResultWithVariants.getFirst()));
            for (EngineResult eResult : bestResultWithVariants.getSecond()) {
                variations.add(new GameVariation(eResult));
            }
            grouppedMap.put(bestResultWithVariants.getFirst().getMoveID(), variations);
        }
        return grouppedMap;
    }
}
