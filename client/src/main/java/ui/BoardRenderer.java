package ui;

import chess.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static ui.EscapeSequences.*;

public class BoardRenderer {

    private static final String LIGHT_SQUARE = SET_BG_COLOR_WHITE;
    private static final String DARK_SQUARE = SET_BG_COLOR_DARK_GREEN;
    private static final String HIGHLIGHT_SQUARE = SET_BG_COLOR_YELLOW;
    private static final String HIGHLIGHT_SOURCE = SET_BG_COLOR_GREEN;
    private static final String BORDER_BG = SET_BG_COLOR_LIGHT_GREY;
    private static final String BORDER_TEXT = SET_TEXT_COLOR_BLACK;
    private static final String WHITE_PIECE_COLOR = SET_TEXT_COLOR_RED;
    private static final String BLACK_PIECE_COLOR = SET_TEXT_COLOR_BLUE;
    private static final String RESET = RESET_BG_COLOR + RESET_TEXT_COLOR;

    public static void drawWhiteBoard() {
        ChessGame game = new ChessGame();

        drawBoard(game.getBoard(), true, null);
    }

    public static void drawBlackBoard() {
        ChessGame game = new ChessGame();
        drawBoard(game.getBoard(), false, null);
    }

    public static void drawBoard(ChessBoard board, boolean whitePerspective, Collection<ChessMove> highlights) {
        Set<ChessPosition> highlightedSquares = new HashSet<>();
        ChessPosition sourceSquare = null;
        if (highlights != null && !highlights.isEmpty()) {
            for (ChessMove move : highlights)   {

                highlightedSquares.add(move.getEndPosition());
                sourceSquare = move.getStartPosition();
            }
        }

        System.out.println();
        drawColumnHeaders(whitePerspective);

        int startRow = whitePerspective ? 8 : 1;
        int endRow = whitePerspective ? 1 : 8;
        int step = whitePerspective ? -1 : 1;

        for (int row = startRow; whitePerspective ? row >= endRow : row <= endRow; row += step) {
            drawRow(board, row, whitePerspective, highlightedSquares, sourceSquare);
        }

        drawColumnHeaders(whitePerspective);
        System.out.println();
    }

    private static void drawColumnHeaders(boolean whitePerspective) {
        System.out.print(BORDER_BG + BORDER_TEXT);
        System.out.print("   ");

        if (whitePerspective) {
            for (char c = 'a'; c <= 'h'; c++) {
                System.out.print(" " + c + " ");
            }
        } else {
            for (char c = 'h'; c >= 'a'; c--) {
                System.out.print(" " + c + " ");
            }
        }

        System.out.print("   ");
        System.out.println(RESET);
    }

    private static void drawRow(ChessBoard board, int row, boolean whitePerspective,
                                Set<ChessPosition> highlights, ChessPosition source) {
        System.out.print(BORDER_BG + BORDER_TEXT + " " + row + " ");

        int startCol = whitePerspective ? 1 : 8;
        int endCol = whitePerspective ? 8 : 1;
        int step = whitePerspective ? 1 : -1;

        for (int col = startCol; whitePerspective ? col <= endCol : col >= endCol; col += step) {
            ChessPosition pos = new ChessPosition(row, col);

            String bgColor;
            if (source != null && pos.equals(source)) {
                bgColor = HIGHLIGHT_SOURCE;

            } else if (highlights.contains(pos)) {
                bgColor = HIGHLIGHT_SQUARE;
            } else {
                boolean isLightSquare = (row + col) % 2 != 0;
                bgColor = isLightSquare ? LIGHT_SQUARE : DARK_SQUARE;
            }
            System.out.print(bgColor);

            ChessPiece piece = board.getPiece(pos);
            if (piece == null) {
                System.out.print("   ");
            } else {
                System.out.print(getPieceString(piece));
            }
        }

        System.out.print(BORDER_BG + BORDER_TEXT + " " + row + " ");
        System.out.println(RESET);
    }

    private static String getPieceString(ChessPiece piece) {
        String textColor = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                ? WHITE_PIECE_COLOR : BLACK_PIECE_COLOR;

        String letter = switch (piece.getPieceType()) {
            case KING -> "K";
            case QUEEN -> "Q";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case ROOK -> "R";
            case PAWN -> "P";
        };
        return textColor + " " + letter + " ";
    }
}
