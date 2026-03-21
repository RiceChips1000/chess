package ui;

import chess.*;

import static ui.EscapeSequences.*;

public class BoardRenderer {

    private static final String LIGHT_SQUARE = SET_BG_COLOR_WHITE;
    private static final String DARK_SQUARE = SET_BG_COLOR_DARK_GREEN;
    private static final String BORDER_BG = SET_BG_COLOR_LIGHT_GREY;

    private static final String BORDER_TEXT = SET_TEXT_COLOR_BLACK;
    private static final String WHITE_PIECE_COLOR = SET_TEXT_COLOR_RED;
    private static final String BLACK_PIECE_COLOR = SET_TEXT_COLOR_BLUE;
    private static final String RESET = RESET_BG_COLOR + RESET_TEXT_COLOR;

    public static void drawWhiteBoard() {
        ChessGame game = new ChessGame();
        ChessBoard board = game.getBoard();

        drawBoard(board, true);
    }

    public static void drawBlackBoard()        {
        ChessGame game = new ChessGame();

        ChessBoard board = game.getBoard();
        drawBoard(board, false);
    }

    public static void drawBothBoards() {

        //eziest peaziest thing ever
        ChessGame game = new ChessGame();

        ChessBoard board = game.getBoard();

        System.out.println("White perspective:");
        drawBoard(board, true);

        System.out.println("Black perspective:");
        drawBoard(board, false);

    }


    private static void drawBoard(ChessBoard board, boolean whitePerspective) {


        System.out.println();
        drawColumnHeaders(whitePerspective);

        int startRow = whitePerspective ? 8 : 1;
        int endRow = whitePerspective ? 1 : 8;
        int step = whitePerspective ? -1 : 1;

        for (int row = startRow; whitePerspective ? row >= endRow : row <= endRow; row += step) {
            drawRow(board, row, whitePerspective);

        }

        drawColumnHeaders(whitePerspective);

        System.out.println();
    }

    private static void drawColumnHeaders(boolean whitePerspective) {
        System.out.print(BORDER_BG + BORDER_TEXT);
        System.out.print(EMPTY);

        if (whitePerspective) {
            for (char c = 'a'; c <= 'h'; c++) {

                System.out.print(" " + c + " ");
            }
        } else {

            for (char c = 'h'; c >= 'a'; c--) {
                System.out.print(" " + c + " ");
            }
        }

        System.out.print(EMPTY);

        System.out.println(RESET);
    }

    private static void drawRow(ChessBoard board, int row, boolean whitePerspective) {
        System.out.print(BORDER_BG + BORDER_TEXT + " " + row + " ");

        int startCol = whitePerspective ? 1 : 8;

        int endCol = whitePerspective ? 8 : 1;
        int step = whitePerspective ? 1 : -1;

        for (int col = startCol; whitePerspective ? col <= endCol : col >= endCol; col += step) {
            boolean isLightSquare = (row + col) % 2 != 0;
            String bgColor = isLightSquare ? LIGHT_SQUARE : DARK_SQUARE;
            System.out.print(bgColor);

            ChessPosition pos = new ChessPosition(row, col);

            ChessPiece piece = board.getPiece(pos);

            if (piece == null) {
                System.out.print(EMPTY);
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

        return textColor + switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;

            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;

            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;

            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;

            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;

            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
        };
    }
}
