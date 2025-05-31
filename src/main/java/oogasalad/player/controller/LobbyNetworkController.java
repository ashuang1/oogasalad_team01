package oogasalad.player.controller;

import java.io.IOException;
import javafx.application.Platform;
import oogasalad.networking.GameClient;
import oogasalad.networking.GameServer;

public class LobbyNetworkController {

  private GameServer server;
  private GameClient client;

  /**
   * Starts a new game server and connects to it as a client.
   *
   * @param port the port to host the server on
   * @throws IOException if server creation or client connection fails
   */
  public void startServer(int port) throws IOException {
    server = new GameServer(port);
    new Thread(() -> {
      try {
        server.start();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();

    client = new GameClient("localhost", port);
  }

  /**
   * Connects to an existing server as a client.
   *
   * @param ip   the IP address of the server
   * @param port the port of the server
   */
  public void joinServer(String ip, int port) {
    client = new GameClient(ip, port);
  }

  /**
   * Disconnects the client and stops the server if this client was the host.
   */
  public void disconnect() {
    if (client != null) {
      client.disconnect();
      client = null;
    }
    if (server != null) {
      Platform.runLater(() -> {
        server.stop();
      });
    }
  }

  /**
   * @return the running GameServer instance, or null if not hosting
   */
  public GameServer getServer() {
    return server;
  }

  /**
   * @return the connected GameClient instance, or null if not connected
   */
  public GameClient getClient() {
    return client;
  }

  /**
   * @return true if currently connected to a server or hosting one
   */
  public boolean isConnected() {
    return client != null;
  }
}
