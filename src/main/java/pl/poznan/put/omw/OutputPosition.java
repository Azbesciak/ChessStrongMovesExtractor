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
    GameVariation bestmove;

    public OutputPosition(String FEN, List<GameVariation> gameVariations) {
        this.FEN = FEN;
        this.gameVariations = gameVariations;
        this.bestmove = gameVariations.get(0);
    }

    @Override
    public String toString() {
        return "[FEN \"" + FEN + "\"]" + "\n" +
                gameVariations.stream().map(gameVariation -> gameVariation.toString()).collect(Collectors.joining(" "));
    }

    public static List<OutputPosition> createOutputPositions(Map<Integer, List<GameVariation>> grouppedGameVariations) {
        return grouppedGameVariations
                .values()
                .stream()
                .filter(x -> x.size() > 0)
                .map(x -> new OutputPosition(x.get(0).fen, x))
                .collect(Collectors.toList());
    }
}
