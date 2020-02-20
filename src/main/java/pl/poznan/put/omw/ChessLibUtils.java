package pl.poznan.put.omw;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import com.github.bhlangonijr.chesslib.move.MoveList;

import java.util.Arrays;
import java.util.List;

public class ChessLibUtils {

    public static void main(String args[]) throws MoveConversionException {
        // Creates a new chessboard in the standard initial position
        Board board = new Board();
        // String fen = "8/p5Q1/2ppq2p/3n1ppk/3B4/2P2P1P/P5P1/6K1 w - - 3 46";

        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        board.loadFromFen(fen);


        // działa, format SAN/PAN standardowa notacja szachowa
        String san = "Nc3"; // "g5"
        Move moveFromSAN = getMoveFromSAN(san);
        System.out.println(getMoveToSAN(moveFromSAN));
        System.out.println(getMoveToUCI(moveFromSAN));


        // dziala, format skad-dokad, format od stockfisha/serwera uci
        String uci = "d2d4";
        Move moveFromUCI = getMoveFromUCI(board, uci);
        System.out.println(getMoveToSAN(moveFromUCI));
        System.out.println(getMoveToUCI(moveFromUCI));


        System.out.println(isMoveACapture(board, moveFromSAN));
        System.out.println(isMoveACapture(board, moveFromSAN));
        System.out.println(ChessLibUtils.getOpponentMaterialDifferenceAfterMove(board, moveFromSAN));


        System.out.println(getWhiteMaterialSum(board));
        System.out.println(getBlackMaterialSum(board));
        System.out.println(board.getFen());

        List<GameVariation> variations = Arrays.asList(
                new GameVariation("g4", false, true, 46, 29900),
                new GameVariation("Qxa7", true, false, 46, 0),
                new GameVariation("c4", false, false, 46, -51)
        );
        OutputPosition outputPosition = new OutputPosition("8/p5Q1/2ppq2p/3n1ppk/3B4/2P2P1P/P5P1/6K1 w - - 3 46", variations);
        System.out.println(outputPosition.toString());
    }

    public static boolean isMaterialAdvantageAfterMove(Board board, Move move) throws MoveConversionException {
        return getMyMaterial(board) > getOpponentMaterialAfterMove(board, move);
    }

    public static int getMyMaterial(Board board) {
        Side side = board.getSideToMove();
        if (side == Side.WHITE) {
            return getWhiteMaterialSum(board);
        } else {
            return getBlackMaterialSum(board);
        }
    }

    /**
     * Gets material sum of the opponent.
     *
     * @param board board before move
     * @return material sum of the opponent
     */
    public static int getOpponentMaterialBeforeMove(Board board) {
        Side side = board.getSideToMove();
        if (side == Side.BLACK) {
            return getWhiteMaterialSum(board);
        } else {
            return getBlackMaterialSum(board);
        }
    }

    /**
     * Gets material sum of the opponent after move.
     *
     * @param board board before move
     * @return material sum aterial sum of the opponent after move
     */
    public static int getOpponentMaterialAfterMove(Board board, Move move) throws MoveConversionException {
        Side side = board.getSideToMove();
        board.doMove(move);
        int afterMoveOpponentMaterial;
        if (side == Side.BLACK) {
            afterMoveOpponentMaterial = getWhiteMaterialSum(board);
        } else {
            afterMoveOpponentMaterial = getBlackMaterialSum(board);
        }
        board.undoMove(); // undo move so state of the board is not changed
        return afterMoveOpponentMaterial;
    }

    public static int getOpponentMaterialDifferenceAfterMove(Board board, Move move) throws MoveConversionException {
        return getOpponentMaterialBeforeMove(board) - getOpponentMaterialAfterMove(board, move);
    }

    public static int getWhiteMaterialSum(Board board) {
        Piece[] whitePieces = {Piece.WHITE_PAWN, Piece.WHITE_KNIGHT, Piece.WHITE_BISHOP, Piece.WHITE_ROOK, Piece.WHITE_QUEEN, Piece.WHITE_KING};
        return getMaterialSum(whitePieces, board);
    }

    public static int getBlackMaterialSum(Board board) {
        Piece[] blackPieces = {Piece.BLACK_PAWN, Piece.BLACK_KNIGHT, Piece.BLACK_BISHOP, Piece.BLACK_ROOK, Piece.BLACK_QUEEN, Piece.BLACK_KING};
        return getMaterialSum(blackPieces, board);
    }

    public static int getMaterialSum(Piece[] pieces, Board board) {
        return Arrays.stream(pieces).map(piece ->
                board.getPieceLocation(piece).size() * getValueFromPiece(piece.getPieceType())
        ).mapToInt(Integer::intValue).sum();
    }

    public static boolean isMoveACapture(Board board, Move move) throws MoveConversionException {
        return getOpponentMaterialDifferenceAfterMove(board, move) > 0;
    }

    public static int getValueFromPiece(PieceType piece) {
        switch (piece) {
            case PAWN:
                return 100;
            case KNIGHT:
            case BISHOP:
                return 300;
            case KING:
                return 400;
            case ROOK:
                return 500;
            case QUEEN:
                return 900;
            default:
                return 0;
        }
    }

    public static Move getMoveFromSAN(String sanMove) throws MoveConversionException {
        MoveList moves = new MoveList();
        moves.loadFromSan(sanMove);
        return moves.get(0);
    }

    public static Move getMoveFromUCI(Board board, String uciMove) throws MoveConversionException {
        return new Move(uciMove, board.getSideToMove());
    }

    public static String getMoveToSAN(Move move) throws MoveConversionException {
        MoveList sanList = new MoveList();
        sanList.add(move);
        String[] sanRepresentation = sanList.toSanArray();
        return sanRepresentation[0];
    }

    public static String getMoveToUCI(Move move) {
        return move.toString();
    }

    public static String getSANFromUCI(String FEN, String uciMove) throws MoveConversionException {
        Board board = new Board();
        board.loadFromFen(FEN);
        Move move = getMoveFromUCI(board, uciMove);
        return getMoveToSAN(move);
    }
}
