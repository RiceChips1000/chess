package client;


public class ClientMain {
    public static void main(String[] args) {
        ChessClient client = new ChessClient("http://localhost:8080");
        client.run();
    }
}
