package oogasalad.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class GameClient {

  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;
  private String serverIP;
  private int serverPort;

  public GameClient(String serverIP, int serverPort) {
    this.serverIP = serverIP;
    this.serverPort = serverPort;
    connect();
  }

  private void connect() {
    try {
      socket = new Socket(serverIP, serverPort);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);
      out.println("Hello from client");

      new Thread(this::listenToServer).start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void listenToServer() {
    try {
      String line;
      while ((line = in.readLine()) != null) {
        handleMessage(line);
      }
    } catch (IOException e) {
      System.out.println("Disconnected from server.");
    }
  }

  private void handleMessage(String message) {
    System.out.println("Server: " + message);
    // TODO: parse and forward to RemoteControlStrategy or game view
  }

  public void sendMessage(String message) {
    out.println(message);
  }

  public void close() throws IOException {
    socket.close();
  }

  public static void main(String[] args) throws IOException {
    Scanner scanner = new Scanner(System.in);
    GameClient client = new GameClient("127.0.0.1", 8000);
    while (true) {
      String message = scanner.nextLine();
      client.sendMessage(message);
    }
  }
}
