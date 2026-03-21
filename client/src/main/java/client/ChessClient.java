package client;

import model.*;
import ui.BoardRenderer;

import java.util.Arrays;
import java.util.Scanner;

public class ChessClient {

    private final ServerFacade server;
    private String authToken = null;

    private String username = null;
    private boolean loggedIn = false;

    private GameData[] lastGameList = null;

    public ChessClient(String serverUrl) {

        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println(" Welcome to 240 Chess. Type help to get started.");

        Scanner scanner = new Scanner(System.in);
        var result = "";


        while (!result.equals("quit"))   {
            printPrompt();

            String line = scanner.nextLine().trim();

            try {
                result = eval(line);
                System.out.println(result);

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println();
    }

    private String eval(String input) {
        var tokens = input.toLowerCase().split(" ");

        if (tokens.length == 0 || tokens[0].isEmpty()) {
            return help();
        }
        var cmd = tokens[0];

        var params = Arrays.copyOfRange(input.split(" "), 1, input.split(" ").length);

        if (loggedIn) {

            return evalPostlogin(cmd, params);
        } else {

             return evalPrelogin(cmd, params);
        }
    }

    private String evalPrelogin(String cmd, String[] params) {
        try {

            return switch (cmd) {
                case "help" -> help();
                 case "quit" -> "quit";
                case "login" -> login(params);

                case "register" -> register(params);
                default -> "Unknown command. Type help for available commands.";
            };
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String login(String[] params) throws Exception {
        if (params.length < 2) {
            return "Usage: login <username> <password>";
        }
        AuthData auth = server.login(params[0], params[1]);
        authToken = auth.authToken();

        username = auth.username();
        loggedIn = true;
        return "Logged in as " + username + ".";

    }

    private String register(String[] params) throws Exception {
        if (params.length < 3) {
            return "Usage: register <username> <password> <email>";
        }
        AuthData auth = server.register(params[0], params[1], params[2]);

        authToken = auth.authToken();
        username = auth.username();
        loggedIn = true;

        return "Registered and logged in as " + username + ".";
    }

    private String help() {
        if (loggedIn) {

            return """
                    create <NAME> - create a game
                    list - list games
                    join <ID> [WHITE|BLACK] - play a game
                    observe <ID> - observe a game
                    logout - log out
                    quit - exit the program
                    help - display available commands""";

        }
        return """
                register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                login <USERNAME> <PASSWORD> - to play chess
                quit - exit the program
                help - display available commands""";


    }

    private void printPrompt() {
        if (loggedIn) {

            System.out.print("[" + username + "] >>> ");
        } else {

            System.out.print("[LOGGED_OUT] >>> ");
        }
    }
}
