package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        if(type == PieceType.PAWN) {
            int direction;
            int startRow;
            int promotionRow;

            if(pieceColor == ChessGame.TeamColor.WHITE){
                direction = 1;
                startRow = 2;
                promotionRow = 8;
            } else {
                direction = -1;
                startRow = 7;
                promotionRow = 1;
            }
            // just normal pawn one foward
            int nextRow = row + direction;
            if(isValidPosition(nextRow, col)) {
                ChessPosition forwardOne = new ChessPosition(nextRow, col);
                if(board.getPiece(forwardOne) == null) {
                    if(nextRow == promotionRow) {
                        addPromotionMoves(moves, myPosition, forwardOne);
                    } else {
                        moves.add(new ChessMove(myPosition, forwardOne, null));
                    }
                    // this is just for the 2 forward one
                    if(row == startRow){
                        int nextRowTwo = row + 2 * direction;
                        ChessPosition forwardTwo = new ChessPosition(nextRowTwo, col);
                        if(board.getPiece(forwardTwo) == null) {
                            moves.add(new ChessMove(myPosition, forwardTwo, null));
                        }
                    }
                }
            }
            // last part of za pawn ze captures muahahah
            int[] diagonalCapture = {-1, 1};
            for(int i = 0; i < diagonalCapture.length; i++) {
                int newCapturePosCol = col + diagonalCapture[i];
                int newCapturePosRow = row + direction;
                if(isValidPosition(newCapturePosRow, newCapturePosCol)) {
                    ChessPosition targetPos = new ChessPosition(newCapturePosRow, newCapturePosCol);
                    ChessPiece target = board.getPiece(targetPos);
                    if(target != null && target.getTeamColor() != pieceColor){
                        if(newCapturePosRow == promotionRow) {
                            addPromotionMoves(moves, myPosition, targetPos);
                        } else {
                            moves.add(new ChessMove(myPosition, targetPos, null));
                        }
                    }
                }
            }
        }

        //knight

        if(type == PieceType.KNIGHT) {
            int[][] jumps = {
                    {2,1},{2,-1},{-2,1},{-2,-1},
                    {1,2},{1,-2},{-1,2},{-1,-2}
            };

            for(int i = 0; i < jumps.length; i++) {
                int newRow = row + jumps[i][0];
                int newCol = col + jumps[i][1];
                addMoveIfValid(board, myPosition, newRow, newCol, moves);
            }
        }
        if(type == PieceType.ROOK) {
            int[][] directions = {{1,0},{-1,0},{0,1},{0,-1}};
            addSlidingMoves(board, myPosition, directions, moves);
        }

        if(type == PieceType.BISHOP) {
            int[][] directions = {{1,1},{1,-1},{-1,1},{-1,-1}};
            addSlidingMoves(board, myPosition, directions, moves);
        }

        if(type == PieceType.QUEEN) {
            int[][] directions = {{1,0},{-1,0},{0,1},{0,-1},
                    {1,1},{1,-1},{-1,1},{-1,-1}};
            addSlidingMoves(board, myPosition, directions, moves);
        }

        // the king(me fr)
        if(type == PieceType.KING){
            for(int dirRow = -1; dirRow <= 1; dirRow++){
                for(int dirColKing = -1; dirColKing <= 1; dirColKing++) {
                    if(dirRow == 0 && dirColKing == 0) {continue;}
                    int newRow = row + dirRow;
                    int newCol = col + dirColKing;
                    addMoveIfValid(board, myPosition, newRow, newCol, moves);
                }
            }
        }

        return moves;


        // Sliding moves calc
        // promotion moves
        //Will have a array of array type ish of thing to run through so that sliding moves and other moves can be easily made for each peice

    }

    private boolean isValidPosition(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private void addMoveIfValid(ChessBoard board, ChessPosition myPosition, int newRow, int newCol, List<ChessMove> moves) {
        if(isValidPosition(newRow, newCol)) {
            ChessPosition pos = new ChessPosition(newRow, newCol);
            ChessPiece target = board.getPiece(pos);
            if(target == null || target.getTeamColor() != pieceColor) {
                moves.add(new ChessMove(myPosition, pos, null));
            }
        }
    }

    private void addSlidingMoves(ChessBoard board, ChessPosition start, int[][] directions, List<ChessMove> moves){
        int row = start.getRow();
        int col = start.getColumn();

        for(int i = 0; i < directions.length; i++) {
            int directionRow = directions[i][0];
            int directionCol = directions[i][1];

            int newRow = row + directionRow;
            int newCol = col + directionCol;
            while(isValidPosition(newRow, newCol)) {
                ChessPosition pos = new ChessPosition(newRow, newCol);
                ChessPiece target = board.getPiece(pos);
                if (target == null) {
                    moves.add(new ChessMove(start, pos, null));
                } else {
                    if(target.getTeamColor() != pieceColor) {
                        moves.add(new ChessMove(start, pos, null));
                    }
                    break;
                }
                newRow = newRow + directionRow;
                newCol = newCol + directionCol;
            }
        }
    }

    private void addPromotionMoves(List<ChessMove> moves, ChessPosition start, ChessPosition end) {
        moves.add(new ChessMove(start, end, PieceType.QUEEN));
        moves.add(new ChessMove(start, end, PieceType.ROOK));
        moves.add(new ChessMove(start, end, PieceType.KNIGHT));
        moves.add(new ChessMove(start, end, PieceType.BISHOP));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}