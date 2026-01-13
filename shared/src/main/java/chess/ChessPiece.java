package chess;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

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
     * returns a boolean whether move is in bounds. true if it is false if not
     */
    public boolean isNextMoveInChessBounds(ChessPosition myPosition, int rowMoveDistance, int colMoveDistance ) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        int newRow = row + rowMoveDistance;
        int newCol = col + colMoveDistance;
        return (newRow < 9 && newCol < 9) && (newRow > 0 && newCol > 0);
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        int[][] pawnMovement = {
                {1,0}, /* forward movement DOUBLE FOWARD IS TAKEN CARE OF WITH if statments */
                {1,1}, /* CAPTURE PIECE AS WHITE up left */
                {1,-1} /* CAPTURE PIECE AS WHITE up right */
                     /* TO CAPTURE PIECES AS BLACK probably just have a if condition for black and multiply [] by -1  */
        };

        int[][] knightMovement = {
                { 2,1},
                { 2,-1},
                {-2,1},
                {-2,-1},
                {1,2},
                {1, -2},
                {-1,2},
                {-1,-2}


        };

        int[][] verticalAndHorizontalMovement = {
                {1,0},
                {-1,0},
                {0,1},
                {0,-1}
        };

        int[][] diagonalMovement = {
                {1,1},
                {1,-1},
                {-1,1},
                {-1,-1}
        };

        ChessPiece piece = board.getPiece(myPosition);

        List<ChessMove> moves = new ArrayList<>();

        /* BISHOP */

        if(piece.getPieceType() == PieceType.BISHOP) {
            int col = myPosition.getColumn();
            int row = myPosition.getRow();

            for(int[] direction : diagonalMovement) {
                int i = 1;
                while (isNextMoveInChessBounds(myPosition, direction[0] * i, direction[1] * i)) {
                    moves.add(new ChessMove(
                            new ChessPosition(row,col),
                            new ChessPosition(row + (i * direction[0]), col + (i * direction[1])),
                            null));
                    i++;
            }
            }
                return moves;
        }


        /* QUEEN */

        if(piece.getPieceType() == PieceType.QUEEN) {
            int col = myPosition.getColumn();
            int row = myPosition.getRow();

            for(int[] direction : diagonalMovement) {
                int i = 1;
                while (isNextMoveInChessBounds(myPosition, direction[0] * i, direction[1] * i)) {
                    moves.add(new ChessMove(
                            new ChessPosition(row,col),
                            new ChessPosition(row + (i * direction[0]), col + (i * direction[1])),
                            null));
                    i++;
                }
            }

            for(int[] direction : verticalAndHorizontalMovement) {
                int i = 1;
                while (isNextMoveInChessBounds(myPosition, direction[0] * i, direction[1] * i)) {
                    moves.add(new ChessMove(
                            new ChessPosition(row,col),
                            new ChessPosition(row + (i * direction[0]), col + (i * direction[1])),
                            null));
                    i++;
                }
            }

            return moves;
        }

        /* KING  */

        if(piece.getPieceType() == PieceType.KING) {
            int col = myPosition.getColumn();
            int row = myPosition.getRow();

            for(int[] direction : diagonalMovement) {
                if (isNextMoveInChessBounds(myPosition, direction[0], direction[1])) {
                    moves.add(new ChessMove(
                            new ChessPosition(row,col),
                            new ChessPosition(row + (direction[0]), col + (direction[1])),
                            null));
                }
            }

            for(int[] direction : verticalAndHorizontalMovement) {
                if (isNextMoveInChessBounds(myPosition, direction[0], direction[1])) {
                    moves.add(new ChessMove(
                            new ChessPosition(row,col),
                            new ChessPosition(row + (direction[0]), col + (direction[1])),
                            null));
                }
            }

            return moves;
        }

        /* ROOK */

        if(piece.getPieceType() == PieceType.ROOK) {
            int col = myPosition.getColumn();
            int row = myPosition.getRow();

            for(int[] direction : verticalAndHorizontalMovement) {
                int i = 1;
                while (isNextMoveInChessBounds(myPosition, direction[0] * i, direction[1] * i)) {
                    moves.add(new ChessMove(
                            new ChessPosition(row,col),
                            new ChessPosition(row + (i * direction[0]), col + (i * direction[1])),
                            null));
                    i++;
                }
            }

            return moves;
        }

        /* KNIGHT */

        if(piece.getPieceType() == PieceType.KNIGHT) {
            int col = myPosition.getColumn();
            int row = myPosition.getRow();

            for(int[] direction : knightMovement) {
                if(isNextMoveInChessBounds(myPosition, direction[0], direction[1])) {
                    moves.add(new ChessMove(
                            new ChessPosition(row,col),
                            new ChessPosition(row + (direction[0]), col + (direction[1])),
                            null));
                }
            }
            return moves;
        }

        if(piece.getPieceType() == PieceType.PAWN) {
            int col = myPosition.getColumn();
            int row = myPosition.getRow();

            if(piece.pieceColor == ChessGame.TeamColor.WHITE && col == 2) {
                moves.add(new ChessMove(myPosition, new ChessPosition(row + 2, col), null));
            }
            if(piece.pieceColor == ChessGame.TeamColor.BLACK && col == 6) {
                moves.add(new ChessMove(myPosition, new ChessPosition(row - 2, col), null));
            }
            for(int[] direction : pawnMovement) {
                moves.add(new ChessMove(myPosition, new ChessPosition(direction[0], direction[1]), null));

            }
            return moves;
        }

        return List.of();
    }
};