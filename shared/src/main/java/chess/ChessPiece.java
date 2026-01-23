package chess;

import java.lang.annotation.ElementType;
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
    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    int[][] pawnMovement = {
            {1, 0} /* forward movement DOUBLE FORWARD IS TAKEN CARE OF WITH if statments */
    };

    int[][] pawnCapture = {
            {1, 1},
            {1, -1}/* CAPTURE PIECE AS WHITE up left */
            /* CAPTURE PIECE AS WHITE up right */
            /* TO CAPTURE PIECES AS BLACK probably just have an if condition for black and multiply [] by -1  */
    };

    int[][] knightMovement = {
            {2, 1},
            {2, -1},
            {-2, 1},
            {-2, -1},
            {1, 2},
            {1, -2},
            {-1, 2},
            {-1, -2}
    };

    int[][] verticalAndHorizontalMovement = {
            {1, 0},
            {-1, 0},
            {0, 1},
            {0, -1}
    };

    int[][] diagonalMovement = {
            {1, 1},
            {1, -1},
            {-1, 1},
            {-1, -1}
    };

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
    public boolean isNextMoveInChessBounds(ChessPosition myPosition, int rowMoveDistance, int colMoveDistance) {
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
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece piece = board.getPiece(myPosition);

        /* BISHOP */
        if (piece.getPieceType() == PieceType.BISHOP) {
            return bishopMovesCalculator(board, myPosition,moves, row, col);
        }

        /* QUEEN */
        if (piece.getPieceType() == PieceType.QUEEN) {
            return queenMovesCalculator(board, myPosition,moves, row, col);
        }

        /* KING  */
        if (piece.getPieceType() == PieceType.KING) {
            return kingMovesCalculator(board, myPosition, moves, row, col);
        }

        /* ROOK */
        if (piece.getPieceType() == PieceType.ROOK) {
            return rookMovesCalculator(board, myPosition, moves, row, col);
        }

        /* KNIGHT */
        if (piece.getPieceType() == PieceType.KNIGHT) {
            return knightMovesCalculator(board, myPosition, moves, row, col);
        }

        /* PAWN */
        if (piece.getPieceType() == PieceType.PAWN) {
            return pawnMovesCalculator(board, myPosition, moves, piece, row, col);
        }
        return moves;
    }

    public Collection<ChessMove> slidingMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves, int[][] movementPattern, int row, int col){
        for(int[] direction : movementPattern) {
            int i = 1;
            while (isNextMoveInChessBounds(myPosition, direction[0] * i, direction[1] * i)) {
                ChessPosition otherPosition = new ChessPosition(row + direction[0] * i, col + direction[1] * i);

                ChessPiece nextMove = board.getPiece(otherPosition);

                if (nextMove == null) {
                    moves.add(new ChessMove(
                            new ChessPosition(row, col),
                            new ChessPosition(row + (i * direction[0]), col + (i * direction[1])),
                            null));
                } else {
                    if (!WhatTeamColor.isSameColor(board, this, otherPosition)) {
                        moves.add(new ChessMove(myPosition, otherPosition, null)); // capture
                    }
                    break;
                }
                i++;
            }
        }
        return moves;
    }

    public Collection<ChessMove> diagonalMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves, int row, int col){
        return slidingMoves( board, myPosition, moves, diagonalMovement, row, col);
    }

    public Collection<ChessMove> verticalAndHorizontalMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves, int row, int col){
        return slidingMoves( board, myPosition, moves, verticalAndHorizontalMovement, row, col);
    }


    public Collection<ChessMove> bishopMovesCalculator(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves, int row, int col) {
        return diagonalMoves(board, myPosition, moves, row, col);
    }


    public Collection<ChessMove> queenMovesCalculator(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves, int row, int col) {
        diagonalMoves(board, myPosition, moves, row, col);
        return verticalAndHorizontalMoves(board, myPosition, moves, row, col);
    }


    public Collection<ChessMove> kingMovesCalculator(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves, int row, int col) {

        for (int[] direction : diagonalMovement) {
            if (isNextMoveInChessBounds(myPosition, direction[0], direction[1])) {
                ChessPosition otherPosition = new ChessPosition(
                        row + direction[0],
                        col + direction[1]
                );

                ChessPiece nextMove = board.getPiece(otherPosition);

                if (nextMove == null) {
                    moves.add(new ChessMove(myPosition, otherPosition, null));
                } else {
                    if (!WhatTeamColor.isSameColor(board, this, otherPosition)) {
                        moves.add(new ChessMove(myPosition, otherPosition, null));
                    }
                }
            }
        }

        for (int[] direction : verticalAndHorizontalMovement) {
            if (isNextMoveInChessBounds(myPosition, direction[0], direction[1])) {
                ChessPosition otherPosition = new ChessPosition(
                        row + direction[0],
                        col + direction[1]
                );

                ChessPiece nextMove = board.getPiece(otherPosition);

                if (nextMove == null) {
                    moves.add(new ChessMove(myPosition, otherPosition, null));
                } else {
                    if (!WhatTeamColor.isSameColor(board, this, otherPosition)) {
                        moves.add(new ChessMove(myPosition, otherPosition, null));
                    }
                }
            }
        }

        return moves;
    }

    public Collection<ChessMove> rookMovesCalculator(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves, int row, int col) {
        return verticalAndHorizontalMoves(board, myPosition, moves, row, col);
    }

    public Collection<ChessMove> knightMovesCalculator(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves, int row, int col) {
        for (int[] direction : knightMovement) {
            if (isNextMoveInChessBounds(myPosition, direction[0], direction[1])) {
                ChessPosition otherPosition = new ChessPosition(
                        row + direction[0],
                        col + direction[1]
                );

                ChessPiece nextMove = board.getPiece(otherPosition);

                if (nextMove == null) {
                    moves.add(new ChessMove(myPosition, otherPosition, null));
                } else {
                    if (!WhatTeamColor.isSameColor(board, this, otherPosition)) {
                        moves.add(new ChessMove(myPosition, otherPosition, null));
                    }
                }
            }
        }

        return moves;
    }

    public Collection<ChessMove> pawnMovesCalculator(ChessBoard board, ChessPosition myPosition,
                                                     List<ChessMove> moves,ChessPiece piece, int row, int col) {
        if (piece.pieceColor == ChessGame.TeamColor.WHITE && row == 2) {
            ChessPosition oneInFrontWhite = new ChessPosition(row + 1, col);
            ChessPosition twoInFrontWhite = new ChessPosition(row + 2, col);

            if (board.getPiece(oneInFrontWhite) == null &&
                    board.getPiece(twoInFrontWhite) == null) {
                moves.add(new ChessMove(myPosition, twoInFrontWhite, null));
            }
        }

        if (piece.pieceColor == ChessGame.TeamColor.BLACK && row == 7) {
            ChessPosition oneInFrontBlack = new ChessPosition(row - 1, col);
            ChessPosition twoInFrontBlack = new ChessPosition(row - 2, col);

            if (board.getPiece(oneInFrontBlack) == null &&
                    board.getPiece(twoInFrontBlack) == null) {
                moves.add(new ChessMove(myPosition, twoInFrontBlack, null));
            }
        }

        for (int[] direction : pawnMovement) {

            if (piece.pieceColor == ChessGame.TeamColor.WHITE &&
                    isNextMoveInChessBounds(myPosition, direction[0], direction[1])) {

                ChessPosition oneInFrontWhite = new ChessPosition(row + direction[0], col);

                if (board.getPiece(oneInFrontWhite) == null) {
                    if (row + direction[0] == 8) {
                        moves.add(new ChessMove(myPosition, oneInFrontWhite, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, oneInFrontWhite, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, oneInFrontWhite, PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, oneInFrontWhite, PieceType.KNIGHT));
                    } else {
                        moves.add(new ChessMove(myPosition, oneInFrontWhite, null));
                    }
                }
            }

            if (piece.pieceColor == ChessGame.TeamColor.BLACK && isNextMoveInChessBounds(myPosition, -direction[0], 0)) {
                ChessPosition oneInFrontBlack = new ChessPosition(row - direction[0], col);
                if (board.getPiece(oneInFrontBlack) == null) {
                    if (row - direction[0] == 1) {
                        moves.add(new ChessMove(myPosition, oneInFrontBlack, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, oneInFrontBlack, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, oneInFrontBlack, PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, oneInFrontBlack, PieceType.KNIGHT));
                    } else {
                        moves.add(new ChessMove(myPosition, oneInFrontBlack, null));
                    }
                }
            }
        }
        for (int[] direction : pawnCapture) {
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE && isNextMoveInChessBounds(myPosition, direction[0], direction[1])) {
                ChessPosition frontLeftThenRight = new ChessPosition(
                        row + direction[0],
                        col + direction[1]);
                ChessPiece target = board.getPiece(frontLeftThenRight);
                if (target != null && !WhatTeamColor.isSameColor(board, this, frontLeftThenRight)) {
                    if (row + direction[0] == 8) {
                        moves.add(new ChessMove(myPosition, frontLeftThenRight, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, frontLeftThenRight, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, frontLeftThenRight, PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, frontLeftThenRight, PieceType.KNIGHT));
                    } else {
                        moves.add(new ChessMove(myPosition, frontLeftThenRight, null));
                    }
                }
            }

            if (piece.getTeamColor() == ChessGame.TeamColor.BLACK && isNextMoveInChessBounds(myPosition, -direction[0], -direction[1])) {

                ChessPosition frontLeftThenRight = new ChessPosition(row - direction[0], col - direction[1]);
                ChessPiece target = board.getPiece(frontLeftThenRight);
                if (target != null && !WhatTeamColor.isSameColor(board, this, frontLeftThenRight)) {
                    if (row - direction[0] == 1) {
                        moves.add(new ChessMove(myPosition, frontLeftThenRight, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, frontLeftThenRight, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, frontLeftThenRight, PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, frontLeftThenRight, PieceType.KNIGHT));
                    } else {
                        moves.add(new ChessMove(myPosition, frontLeftThenRight, null));
                    }
                }
            }
        }
        return moves;
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

