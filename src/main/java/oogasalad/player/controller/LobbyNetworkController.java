package oogasalad.player.controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
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
   * Finds if server with ip and port passed in is reachable.
   *
   * @param ip address of server trying to be reached
   * @param port of server trying to be reached
   * @return true if server is reachable
   */
  public boolean isServerReachable(String ip, int port) {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(ip, port), 1000);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * @return the local IP address of computer
   */
  public String getLocalIpAddress() {
    try {
      var interfaces = java.net.NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        var iface = interfaces.nextElement();
        var addresses = iface.getInetAddresses();
        while (addresses.hasMoreElements()) {
          var addr = addresses.nextElement();
          if (!addr.isLoopbackAddress() && addr instanceof java.net.Inet4Address) {
            return addr.getHostAddress();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "Local IP Unavailable";
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
