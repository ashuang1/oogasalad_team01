package oogasalad.networking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import oogasalad.networking.util.JsonUtils;

/**
 * Handles communication between the game server and a single connected client.
 * <p>
 * Each instance of {@code ClientHandler} runs in its own thread and manages
 * message receipt, deserialization, and dispatching to the appropriate server-side
 * logic based on message type. It also supports sending messages back to the client.
 * </p>
 *
 * @author Austin Huang
 */
public class ClientHandler implements Runnable {

  private final Socket socket;
  private final int playerId;
  private final BufferedReader in;
  private final PrintWriter out;
  private final GameServer server;
  private final ObjectMapper mapper = JsonUtils.getMapper();

  /**
   * Constructs a {@code ClientHandler} for the specified client socket and player ID.
   *
   * @param socket   the socket connected to the client
   * @param playerId the unique player ID assigned to this client
   * @param server   a reference to the {@link GameServer} managing the game
   * @throws IOException if an I/O error occurs while initializing input/output streams
   */
  public ClientHandler(Socket socket, int playerId, GameServer server) throws IOException {
    this.socket = socket;
    this.playerId = playerId;
    this.server = server;
    this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.out = new PrintWriter(socket.getOutputStream(), true);
  }

  /**
   * Continuously listens for incoming messages from the client, parses them,
   * and forwards them to the server for processing. Handles HELLO, READY,
   * DISCONNECT, and other broadcast messages appropriately.
   * <p>
   * Upon client disconnection or error, the player is removed from the server
   * and the socket is closed.
   * </p>
   */
  @Override
  public void run() {
    try {
      String jsonLine;
      while ((jsonLine = in.readLine()) != null) {
        // deserialize
        GameMessage message = mapper.readValue(jsonLine, GameMessage.class);
        System.out.println("Player " + playerId + ": " + message);

        if (message.type() == MessageType.HELLO && message.playerId() == -1) {
          assignPlayerIdToClient();
          continue;
        }

        if (message.type() == MessageType.READY) {
          Object readyObject = message.payload().get("ready");
          if (readyObject instanceof Boolean ready) {
            server.handleReadyMessage(playerId, ready);
          }
          continue;
        }

        if (message.type() == MessageType.DISCONNECT) {
          System.out.println("Player " + playerId + " disconnected.");
          server.removeClient(playerId);
          break;
        }

        server.broadcast(message);
      }
    } catch (IOException e) {
      System.out.println("Player " + playerId + " disconnected: " + e.getMessage());
    } finally {
      try {
        server.removeClient(playerId);
        socket.close();
      } catch (IOException ignored) {}
    }
  }

  private void assignPlayerIdToClient() throws JsonProcessingException {
    GameMessage welcome = new GameMessage(MessageType.WELCOME, playerId, null);
    String json = mapper.writeValueAsString(welcome);
    send(json);
  }

  /**
   * Sends a raw JSON-formatted message string to the client over the socket.
   *
   * @param message the JSON string to send
   */
  public void send(String message) {
    out.println(message);
  }


  /**
   * Closes the client socket and associated resources.
   */
  public void close() {
    try {
      socket.close();
    } catch (IOException e) {
      System.err.println("Error closing socket: " + e.getMessage());
    }
  }
}
