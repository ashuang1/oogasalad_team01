package oogasalad.networking;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import oogasalad.networking.util.JsonUtils;

/**
 * Handles the server-side networking logic for a multiplayer game.
 * Listens for incoming client connections, assigns each player a unique ID,
 * and launches a {@link ClientHandler} for each connected client.
 * <p>
 * This class also manages message broadcasting and client disconnection.
 */
public class GameServer {

  public static final int MAX_PLAYERS = 2;

  private ServerSocket serverSocket;
  private ExecutorService threadPool;
  private Map<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
  private int nextPlayerId = 1;
  private ObjectMapper mapper = JsonUtils.getMapper();

  /**
   * Creates a {@code GameServer} that listens on the specified port.
   * Initializes the thread pool and prepares to accept client connections.
   *
   * @param port the TCP port number to bind the server socket to
   * @throws IOException if the server socket cannot be created or bound
   */
  public GameServer(int port) throws IOException {
    serverSocket = new ServerSocket(port);
    threadPool = Executors.newCachedThreadPool();
    System.out.println("Server started on port " + port);
  }

  /**
   * Starts the server loop and begins accepting incoming client connections.
   * For each connected client, a unique player ID is assigned and a new
   * {@link ClientHandler} is created and submitted to the thread pool.
   *
   * @throws IOException if an I/O error occurs while accepting a connection
   */
  public void start() throws IOException {
    try {
      while (true) {
        Socket clientSocket = serverSocket.accept();
        int playerId = nextPlayerId++;
        ClientHandler handler = new ClientHandler(clientSocket, playerId, this);
        clients.put(playerId, handler);
        threadPool.submit(handler);
        System.out.println("Player " + playerId + " connected.");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Broadcasts a {@link GameMessage} to all connected clients by serializing it to JSON.
   *
   * @param message the {@code GameMessage} to broadcast to all clients
   */
  public void broadcast(GameMessage message) {
    try {
      // serialize
      String json = mapper.writeValueAsString(message);
      for (ClientHandler client : clients.values()) {
        client.send(json);
      }
    } catch (Exception e) {
      System.err.println("Failed to serialize GameMessage: " + e.getMessage());
    }
  }

  /**
   * Removes a disconnected client from the server's active client map.
   *
   * @param playerId the ID of the player to remove
   */
  public void removeClient(int playerId) {
    clients.remove(playerId);
  }

  public static void main(String[] args) throws IOException {
    GameServer gameServer = new GameServer(8000);
    gameServer.start();
  }
}
