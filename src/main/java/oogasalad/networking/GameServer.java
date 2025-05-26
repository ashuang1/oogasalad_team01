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

public class GameServer {

  public static final int MAX_PLAYERS = 2;

  private ServerSocket serverSocket;
  private ExecutorService threadPool;
  private Map<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
  private int nextPlayerId = 1;
  private ObjectMapper mapper = JsonUtils.getMapper();

  public GameServer(int port) throws IOException {
    serverSocket = new ServerSocket(port);
    threadPool = Executors.newCachedThreadPool();
    System.out.println("Server started on port " + port);
  }

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

  public void removeClient(int playerId) {
    clients.remove(playerId);
  }

  public static void main(String[] args) throws IOException {
    GameServer gameServer = new GameServer(8000);
    gameServer.start();
  }
}
