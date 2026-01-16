package chess;

public class WhatTeamColor {
    public static boolean isSameColor(ChessBoard board, ChessPiece piece, ChessPosition otherPosition) {

        ChessPiece other = board.getPiece(otherPosition);
        return other != null && other.getTeamColor() == piece.getTeamColor();

    }
}
