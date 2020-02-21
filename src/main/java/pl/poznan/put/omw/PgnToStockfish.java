package pl.poznan.put.omw;

import chess.parser.Entity;
import chess.parser.Move;
import chess.parser.pgn.PGNGame;

import java.util.ArrayList;

public class PgnToStockfish {


    public static ArrayList<String> getStockfishFormat(PGNGame game) {
        ArrayList<String> moveList = new ArrayList<>();
        for (Entity entity : game.getEntities()) {
            if (entity instanceof Move) {

                //format ASCII
                String startX = Character.toString((char) (((Move) entity).getFromX() + 97));
                String startY = String.valueOf(((Move) entity).getFromY() + 1);

                String endX = Character.toString((char) (((Move) entity).getToX() + 97));
                String endY = String.valueOf(((Move) entity).getToY() + 1);

                moveList.add(startX + startY + endX + endY);
            }

        }

        return moveList;
    }

    public static ArrayList<String> getSanList(PGNGame game) {
        ArrayList<String> moveList = new ArrayList<>();
        for (Entity entity : game.getEntities()) {
            if (entity instanceof Move) {
                moveList.add(((Move) entity).getSan());
            }
        }
        return moveList;
    }

}
