package oogasalad.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

  private Socket socket;
  private int playerId;
  private BufferedReader in;
  private PrintWriter out;
  private GameServer server;

  public ClientHandler(Socket socket, int playerId, GameServer server) throws IOException {
    this.socket = socket;
    this.playerId = playerId;
    this.server = server;
    this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.out = new PrintWriter(socket.getOutputStream(), true);
  }

  public void run() {
    try {
      out.println("WELCOME " + playerId);
      String message;
      while ((message = in.readLine()) != null) {
        System.out.println("From Player " + playerId + ": " + message);
        server.broadcast("PLAYER " + playerId + ": " + message);
      }
    } catch (IOException e) {
      System.out.println("Player " + playerId + " disconnected.");
    } finally {
      try {
        socket.close();
        server.removeClient(playerId);
      } catch (IOException ignored) {}
    }
  }

  public void send(String message) {
    out.println(message);
  }
}
