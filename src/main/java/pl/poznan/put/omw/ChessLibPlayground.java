package pl.poznan.put.omw;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import com.github.bhlangonijr.chesslib.move.MoveList;

import java.util.Arrays;

public class ChessLibPlayground {

    public static void main(String args[]) throws MoveConversionException {
        // Creates a new chessboard in the standard initial position
        Board board = new Board();
        String fen = "8/p5Q1/2ppq2p/3n1ppk/3B4/2P2P1P/P5P1/6K1 w - - 3 46";

        board.loadFromFen(fen);

        String san = "g4";

        MoveList moves = new MoveList();
        moves.loadFromSan(san);
        board.doMove(moves.get(0));


        System.out.println(getWhiteMaterialSum(board));
        System.out.println(getBlackMaterialSum(board));
        System.out.println(board.getFen());

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

    public static int getValueFromPiece(PieceType piece) {
        switch (piece) {
            case PAWN:
                return 100;
            case KNIGHT:
                return 300;
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
}
