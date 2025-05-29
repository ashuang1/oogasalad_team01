package oogasalad.networking;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import oogasalad.engine.utility.constants.Directions.Direction;
import oogasalad.networking.util.JsonUtils;
import oogasalad.player.model.strategies.control.RemoteControlStrategy;

/**
 * Handles the client-side networking logic for a multiplayer game.
 * Connects to the game server, sends player input as JSON-encoded messages,
 * and listens for server responses to update local game state.
 */
public class GameClient {

  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;
  private final String serverIP;
  private final int serverPort;
  private int playerId = -1;
  private boolean isReady = false;
  private Map<Integer, RemoteControlStrategy> playerIdToRemoteControlStrategy = new HashMap<>();
  private Set<Integer> activePlayerIds = new HashSet<>();
  private final ObjectMapper mapper = JsonUtils.getMapper();

  /**
   * Creates a new {@code GameClient} and attempts to connect to the server
   * at the specified IP address and port.
   * <p>
   * Upon successful connection, it sends a {@code HELLO} message to the server
   * and starts a background thread to listen for server messages.
   *
   * @param serverIP   the IP address of the server to connect to
   * @param serverPort the port number the server is listening on
   */
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

    if (message.type() == MessageType.MOVE) {
      RemoteControlStrategy strategy = playerIdToRemoteControlStrategy.get(playerId);
      if (strategy != null) {
        strategy.setDirectionFromNetwork((Direction) message.payload().get("direction"));
      }
    }

    if (message.type() == MessageType.START) {
      Object raw = message.payload().get("playerIds");
      List<Integer> ids = mapper.convertValue(raw, new TypeReference<>() {});
      this.activePlayerIds = new HashSet<>(ids);
    }
  }

  /**
   * Sends a {@link GameMessage} to the server after serializing it as a JSON string.
   *
   * @param message the {@code GameMessage} to send to the server
   */
  public void sendMessage(GameMessage message) {
    // {"type": "MOVE", "playerId": 1, "payload": {"direction": "R"}}
    // {"type": "READY", "playerId": 1, "payload": {"ready": true}}
    try {
      // serialize
      String json = mapper.writeValueAsString(message);
      out.println(json);
    } catch (IOException e) {
      System.err.println("Failed to serialize GameMessage: " + e.getMessage());
    }
  }

  /**
   * Sets ready status for client and sends ready status message to server.
   *
   * @param ready ready status of client.
   */
  public void setReadyStatus(boolean ready) {
    isReady = ready;
    sendMessage(new GameMessage(MessageType.READY, playerId, Map.of("ready", ready)));
  }

  /**
   * Closes the client's connection to the server.
   *
   * @throws IOException if an I/O error occurs while closing the socket
   */
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
