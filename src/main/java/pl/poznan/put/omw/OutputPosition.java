package pl.poznan.put.omw;

import com.github.bhlangonijr.chesslib.move.MoveConversionException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OutputPosition {

    String FEN;
    List<GameVariation> gameVariations;

    public OutputPosition(String FEN, List<GameVariation> gameVariations) {
        this.FEN = FEN;
        this.gameVariations = gameVariations;
    }

    @Override
    public String toString() {
        GameVariation bestMove = gameVariations.get(0);
        return "[FEN \"" + FEN + "\"]" + "\n" +
                gameVariations.stream().map(gameVariation -> gameVariation.toString()).collect(Collectors.joining(" "));
    }

    public static ArrayList<GameVariation> createGameVariationList(ArrayList<EngineResult> results, ArrayList<String> sanList) throws MoveConversionException {
        ArrayList<GameVariation> variations = new ArrayList<>();
        for (EngineResult eResult : results) {
            GameVariation v = new GameVariation(eResult.getMoves().get(0), eResult.getMoveID(), eResult.getCentipaws(),
                    eResult.getFen(), sanList);
            variations.add(v);
        }
        return variations;
    }
}
