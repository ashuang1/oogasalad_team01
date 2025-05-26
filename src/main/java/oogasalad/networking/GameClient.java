package oogasalad.networking;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import oogasalad.networking.util.JsonUtils;

public class GameClient {

  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;
  private String serverIP;
  private int serverPort;
  private int playerId = -1;
  private ObjectMapper mapper = JsonUtils.getMapper();

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
      GameMessage hello = new GameMessage(MessageType.HELLO, playerId, null);
      sendMessage(hello);

      new Thread(this::listenToServer).start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void listenToServer() {
    try {
      String jsonLine;
      while ((jsonLine = in.readLine()) != null) {
        // deserialize
        GameMessage message = mapper.readValue(jsonLine, GameMessage.class);
        handleMessage(message);
      }
    } catch (IOException e) {
      System.out.println("Disconnected from server: " + e.getMessage());
    }
  }

  private void handleMessage(GameMessage message) {
    System.out.println("Server: " + message);
    // TODO: parse and forward to RemoteControlStrategy or game view
    if (message.type() == MessageType.WELCOME && playerId == -1) {
      playerId = message.playerId();
      System.out.println("Received playerId: " + playerId);
    }
  }

  public void sendMessage(GameMessage message) {
    // { "type": "MOVE", "playerId": 1, "direction": "LEFT" }
    try {
      // serialize
      String json = mapper.writeValueAsString(message);
      out.println(json);
    } catch (IOException e) {
      System.err.println("Failed to serialize GameMessage: " + e.getMessage());
    }
  }

  public void close() throws IOException {
    socket.close();
  }

  public static void main(String[] args) throws IOException {
    Scanner scanner = new Scanner(System.in);
    ObjectMapper myMapper = JsonUtils.getMapper();
    GameClient client = new GameClient("127.0.0.1", 8000);
    while (true) {
      String json = scanner.nextLine();
      GameMessage message = myMapper.readValue(json, GameMessage.class);
      client.sendMessage(message);
    }
  }
}
