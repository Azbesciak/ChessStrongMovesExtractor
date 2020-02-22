package pl.poznan.put.omw;

import com.github.bhlangonijr.chesslib.*;
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
        String san = "e4"; // "g5"
        Move moveFromSAN = getMoveFromSAN(fen, san);
        System.out.println(getMoveToSAN(fen, moveFromSAN));
        System.out.println(getMoveToUCI(moveFromSAN));


        // dziala, format skad-dokad, format od stockfisha/serwera uci
        String uci = "d2d4";
        Move moveFromUCI = getMoveFromUCI(board, uci);
        System.out.println(getMoveToSAN(fen, moveFromUCI));
        System.out.println(getMoveToUCI(moveFromUCI));


        // UCI -> SAN for move from black perspective
        String fen2 = "rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 1";
        board.loadFromFen(fen2);
        String uci2 = "e7e6";
        Move moveFromUCI2 = getMoveFromUCI(board, uci2);
        // System.out.println(getMoveToSAN(moveFromUCI2)) will throw an exception,
        // because toSanArray() in getMoveToSAN uses default
        // board, so it assumes that uci2 is the first move, it doesn't take into account fen2
        System.out.println(getMoveToSAN(fen2, moveFromUCI2));
//        System.out.println(getMoveToUCI(moveFromUCI2));
        // proper conversion
        System.out.println(getMoveToSAN(fen2, uci2));
        System.out.println(getMoveToUCI(moveFromUCI2));

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

    /**
     *
     * @param board board object
     * @param move move object
     * @return true if my material is bigger than opponent's material after move
     * @throws MoveConversionException
     */
    public static boolean isMaterialAdvantageAfterMove(Board board, Move move) throws MoveConversionException {
        return getMyMaterial(board) > getOpponentMaterialAfterMove(board, move);
    }

    /**
     *
     * @param board board object
     * @return value of my material (all my figure values sum in centipawns)
     */
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
     * @return material sum of the opponent after move
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

    /**
     *
     * @param board board object
     * @param move move object
     * @return difference in centipawns in opponent's material after my move
     * @throws MoveConversionException
     */
    public static int getOpponentMaterialDifferenceAfterMove(Board board, Move move) throws MoveConversionException {
        return getOpponentMaterialBeforeMove(board) - getOpponentMaterialAfterMove(board, move);
    }

    /**
     *
     * @param board board object
     * @return white side material sum
     */
    public static int getWhiteMaterialSum(Board board) {
        Piece[] whitePieces = {Piece.WHITE_PAWN, Piece.WHITE_KNIGHT, Piece.WHITE_BISHOP, Piece.WHITE_ROOK, Piece.WHITE_QUEEN, Piece.WHITE_KING};
        return getMaterialSum(whitePieces, board);
    }

    /**
     *
     * @param board board object
     * @return black material sum
     */
    public static int getBlackMaterialSum(Board board) {
        Piece[] blackPieces = {Piece.BLACK_PAWN, Piece.BLACK_KNIGHT, Piece.BLACK_BISHOP, Piece.BLACK_ROOK, Piece.BLACK_QUEEN, Piece.BLACK_KING};
        return getMaterialSum(blackPieces, board);
    }

    /**
     *
     * @param pieces all pieces to take into account
     * @param board board object
     * @return material sum got from all the pieces
     */
    private static int getMaterialSum(Piece[] pieces, Board board) {
        return Arrays.stream(pieces).map(piece ->
                board.getPieceLocation(piece).size() * getValueFromPiece(piece.getPieceType())
        ).mapToInt(Integer::intValue).sum();
    }

    /**
     *
     * @param  board board object
     * @param move move object
     * @return true if move was a capture
     * @throws MoveConversionException
     */
    public static boolean isMoveACapture(Board board, Move move) throws MoveConversionException {
        return getOpponentMaterialDifferenceAfterMove(board, move) > 0;
    }

    public static boolean didKingMove(Board board, Move move) {
        Side side = board.getSideToMove();
        Square kingPosBefore = board.getKingSquare(side);
        board.doMove(move);
        Square kingPosAfter = board.getKingSquare(side);
        board.undoMove();
        if (kingPosAfter != kingPosBefore) {
            return true;
        } else {
            return false;
        }

    }
//    public static int getKingNeighbours(Board board) {
//    }

    /**
     *
     * @param piece piece type like king or knight
     * @return piece value in centipawns
     */
    private static int getValueFromPiece(PieceType piece) {
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

    /**
     *
     * @param sanMove move in san notation like in example Nf1
     * @return move object
     * @throws MoveConversionException
     */
    public static Move getMoveFromSAN(String fen, String sanMove) throws MoveConversionException {
        MoveList moves = new MoveList(fen);
        moves.loadFromSan(sanMove);
        return moves.get(0);
    }

    /**
     *
     * @param board board object
     * @param uciMove move in UCI notation like in example c1c2
     * @return move object
     * @throws MoveConversionException
     */
    public static Move getMoveFromUCI(Board board, String uciMove) throws MoveConversionException {
        return new Move(uciMove, board.getSideToMove());
    }

    /**
     *
     * @param move move object
     * @return move in san notation like in example Nf1
     * @throws MoveConversionException
     */
    public static String getMoveToSAN(String fen, Move move) throws MoveConversionException {
        MoveList sanList = new MoveList(fen);
        sanList.add(move);
        String[] sanRepresentation = sanList.toSanArray();
        return sanRepresentation[0];
    }

    /**
     *
     * @param move move object
     * @return move in UCI notation like in example c1c2
     */
    public static String getMoveToUCI(Move move) {
        return move.toString();
    }

    /**
     * Overloaded version of getMoveToSAN allowing passing uci move as a String instead of a Move.
     * Converts move in UCI notation to SAN notation.
     * @param fen FEN board notation
     * @param uciMove move in UCI notation for the specified fen
     * @return move in san notation
     * @throws MoveConversionException
     */
    public static String getMoveToSAN(String fen, String uciMove) throws MoveConversionException {
        Board b = new Board();
        b.loadFromFen(fen);
        Move a = getMoveFromUCI(b, uciMove);
        return getMoveToSAN(fen, a);
    }
}
