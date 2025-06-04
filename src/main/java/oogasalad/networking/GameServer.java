package oogasalad.networking;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 *
 * @author Austin Huang
 */
public class GameServer {

  public static final int MAX_PLAYERS = 2;

  private final ServerSocket serverSocket;
  private final ExecutorService threadPool;
  private boolean isRunning = true;
  private final Map<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
  private final MessageBroadcaster broadcaster = new MessageBroadcaster(clients.values());
  private int nextPlayerId = 1;
  private final LobbyStateManager lobbyStateManager = new LobbyStateManager();

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
      while (isRunning) {
        Socket clientSocket = serverSocket.accept();
        int playerId = nextPlayerId++;
        ClientHandler handler = new ClientHandler(clientSocket, playerId, this);
        clients.put(playerId, handler);
        threadPool.submit(handler);
        System.out.println("Player " + playerId + " connected.");
      }
    } catch (IOException e) {
      if (isRunning) {
        e.printStackTrace(); // only print if server wasn't stopped intentionally
      }
    }
  }

  /**
   * Broadcasts a {@link GameMessage} to all connected clients by serializing it to JSON using a
   * {@link MessageBroadcaster}.
   *
   * @param message the {@code GameMessage} to broadcast to all clients
   */
  public void broadcast(GameMessage message) {
    broadcaster.broadcast(message);
  }

  /**
   * Removes a disconnected client from the server's active client map.
   *
   * @param playerId the ID of the player to remove
   */
  public void removeClient(int playerId) {
    clients.remove(playerId);
    lobbyStateManager.removePlayer(playerId);
    broadcastUpdatedPlayerStatuses();
  }

  /**
   * Puts player ready status in map and broadcasts updated player statuses.
   * Sends START message to start game if all players ready upon receiving a READY message.
   *
   * @param playerId the ID of the player sending READY message
   * @param isReady ready status of player
   */
  public void handleReadyMessage(int playerId, boolean isReady) {
    lobbyStateManager.updateReady(playerId, isReady);
    broadcastUpdatedPlayerStatuses();

    if (lobbyStateManager.allReady()) {
      Map<String, Object> payload = new HashMap<>();
      payload.put("playerIds", new ArrayList<>(lobbyStateManager.getAllStatuses().keySet()));
      GameMessage startMessage = new GameMessage(MessageType.START, -1, payload);
      broadcast(startMessage);
    }
  }

  private void broadcastUpdatedPlayerStatuses() {
    Map<String, Object> statusPayload = new HashMap<>();
    statusPayload.put("playerStatuses", new HashMap<>(lobbyStateManager.getAllStatuses()));
    GameMessage statusMessage = new GameMessage(MessageType.PLAYER_STATUS, -1, statusPayload);
    broadcast(statusMessage);
  }

  /**
   * Gracefully shuts down the game server by closing the server socket, disconnecting all connected
   * clients, and terminating the thread pool.
   */
  public void stop() {
    isRunning = false;
    try {
      serverSocket.close();
    } catch (IOException e) {
      System.err.println("Error closing server socket: " + e.getMessage());
    }

    for (ClientHandler client : clients.values()) {
      client.close();
    }

    threadPool.shutdownNow();
    System.out.println("Server shut down.");
  }

  public static void main(String[] args) throws IOException {
    GameServer gameServer = new GameServer(8000);
    gameServer.start();
  }
}
