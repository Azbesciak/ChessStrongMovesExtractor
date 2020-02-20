package pl.poznan.put.omw;

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
}
