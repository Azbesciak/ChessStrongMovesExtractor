package pl.poznan.put.omw;

import com.github.bhlangonijr.chesslib.move.MoveConversionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

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

    public static Map<Integer, List<GameVariation>> createGameVariationList(ArrayList<EngineResult> results, ArrayList<String> sanList) throws MoveConversionException {
        ArrayList<GameVariation> variations = new ArrayList<>();
        for (EngineResult eResult : results) {
            GameVariation v = new GameVariation(eResult.getMoves().get(0), eResult.getMoveID(), eResult.getCentipaws(),
                    eResult.getFen(), sanList);
            variations.add(v);
        }
        Map<Integer, List<GameVariation>> groupedVariations = variations.stream()
                .collect(groupingBy(GameVariation::getIndex));
        return groupedVariations;
    }

    public static ArrayList<GameVariation> setBestAndFlatten(Map<Integer, List<GameVariation>> results) {
        ArrayList<GameVariation> output = new ArrayList<>();
        for (List<GameVariation> nRes : results.values()) {
            nRes.get(0).setBestMove(true);
            for (GameVariation g : nRes) {
                output.add(g);
            }
        }
        return output;
    }
}
