package client;

import chess.*;
import com.google.gson.Gson;
import ui.BoardRenderer;
import websocket.commands.MakeMoveCommand;

import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.Collection;
import java.util.Scanner;

public class GameplayUI implements WebSocketCommunicator.ServerMessageObserver       {

    private final WebSocketCommunicator ws;

    private final String authToken;
    private final int gameID;

    private final ChessGame.TeamColor playerColor;

    private ChessGame currentGame;
    private boolean inGame = true;

    public GameplayUI(String serverUrl, String authToken, int gameID, ChessGame.TeamColor playerColor) throws Exception      {
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;

        ws = new WebSocketCommunicator(serverUrl, this);

        UserGameCommand connectCmd = new UserGameCommand(

                UserGameCommand.CommandType.CONNECT, authToken, gameID);
        ws.sendCommand(connectCmd);
    }

    @Override
    public void notify(ServerMessage message) {

        switch (message.getServerMessageType()) {

            case LOAD_GAME -> {
                currentGame = message.getGame();
                drawBoard();
                printPrompt();
            }
            case ERROR -> {

                System.out.println("\n" + message.getErrorMessage());
                printPrompt();
            }
            case NOTIFICATION -> {

                System.out.println("\n" + message.getMessage());
                printPrompt();
            }

        }
    }

    private void drawBoard() {
        System.out.println();
        if (playerColor == ChessGame.TeamColor.BLACK) {

            BoardRenderer.drawBoard(currentGame.getBoard(), false, null);
        } else {

            BoardRenderer.drawBoard(currentGame.getBoard(), true, null);
        }
    }

    public void run() {

        Scanner scanner = new Scanner(System.in);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        while (inGame)     {
            printPrompt();

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            try {

                processCommand(line);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void processCommand(String input) throws Exception {

        String[] tokens = input.toLowerCase().split(" ");
        String cmd = tokens[0];

        switch (cmd) {
            case "help" -> printHelp();
            case "redraw" -> drawBoard();

            case "leave" -> leaveGame();
            case "move" -> makeMove(input);
            case "resign" -> resign();

            case "highlight" -> highlightMoves(tokens);
            default -> System.out.println("Unknown command. Type 'help' for options.");
        }
    }

    private void printHelp() {
        System.out.println("""
                help - show this help text
                redraw - redraw the chess board
                leave - leave the game
                move <FROM> <TO> [PROMOTION] - make a  move (e.g. move e2 e4, or move e7 e8 queen)
                resign - forfeit the game 
                highlight <POSITION> - show legal moves for a piece (e.g. highlight e2)""");
    }

    private void leaveGame() throws Exception {
        UserGameCommand leaveCmd = new UserGameCommand(

                UserGameCommand.CommandType.LEAVE, authToken, gameID);
        ws.sendCommand(leaveCmd);
        ws.close();
        inGame = false;
    }

    private void resign() throws Exception       {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        Scanner scanner = new Scanner(System.in);

        String answer = scanner.nextLine().trim().toLowerCase();
        if (answer.equals("yes")) {

            UserGameCommand resignCmd = new UserGameCommand(
                    UserGameCommand.CommandType.RESIGN, authToken, gameID);
            ws.sendCommand(resignCmd);

        } else {
            System.out.println("Resign cancelled.");
        }
    }

    private void makeMove(String input) throws Exception {
        String[] parts = input.split(" ");
        if (parts.length < 3) {
            System.out.println("Usage: move <FROM> <TO> [PROMOTION]");
            System.out.println("Example: move e2 e4");
            return;
        }

        ChessPosition from = parsePosition(parts[1]);

        ChessPosition to = parsePosition(parts[2]);
        if (from == null || to == null) {

            System.out.println("Invalid position. Use format like 'e2' (column letter + row number).");
            return;
        }

        ChessPiece.PieceType promotion = null;
        if (parts.length >= 4) {

            promotion = parsePromotion(parts[3]);
            if (promotion == null) {

                System.out.println("Invalid promotion piece. Use: queen, rook, bishop, or knight.");
                return;
            }
        }

        ChessMove move = new ChessMove(from, to, promotion);
        MakeMoveCommand moveCmd = new MakeMoveCommand(authToken, gameID, move);
        ws.sendCommand(moveCmd);
    }

    private void highlightMoves(String[] tokens) {
        if (tokens.length < 2) {

            System.out.println("Usage: highlight <POSITION> (e.g. highlight e2)");
            return;
        }
        if (currentGame == null) {

            System.out.println("No game loaded yet.");
            return;
        }

        ChessPosition pos = parsePosition(tokens[1]);
        if (pos == null) {
            System.out.println("Invalid position. Use format like 'e2'.");
            return;
        }

        ChessPiece piece = currentGame.getBoard().getPiece(pos);
        if (piece == null) {

            System.out.println("No piece at that position.");
            return;
        }

        Collection<ChessMove> validMoves = currentGame.validMoves(pos);
        System.out.println();
        if (playerColor == ChessGame.TeamColor.BLACK) {

            BoardRenderer.drawBoard(currentGame.getBoard(), false, validMoves);
        } else {


            BoardRenderer.drawBoard(currentGame.getBoard(), true, validMoves);
        }
    }

    private ChessPosition parsePosition(String posStr) {
        if (posStr == null || posStr.length() != 2) {

            return null;
        }
        char colChar = posStr.charAt(0);

        char rowChar = posStr.charAt(1);
        int col = colChar - 'a' + 1;

        int row = rowChar - '0';
        if (col < 1 || col > 8 || row < 1 || row > 8) {
            return null;
        }
        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType parsePromotion(String str)     {
        return switch (str.toLowerCase())    {
            case "queen" -> ChessPiece.PieceType.QUEEN;
            case "rook" -> ChessPiece.PieceType.ROOK;

            case "bishop" -> ChessPiece.PieceType.BISHOP;
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            default -> null;
        };
    }

    private void printPrompt() {

        if (currentGame != null && currentGame.getTeamTurn() != null) {

            System.out.print("[GAME - " + currentGame.getTeamTurn() + "'s turn] >>> ");
        } else    {
            System.out.print("[GAME] >>> ");
        }
    }
}
