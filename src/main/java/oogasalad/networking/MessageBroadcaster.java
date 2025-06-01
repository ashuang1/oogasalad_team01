package oogasalad.networking;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import oogasalad.networking.util.JsonUtils;

/**
 * Handles broadcasting messages to all clients and serialization of messages to JSON.
 *
 * @author Austin Huang
 */
public class MessageBroadcaster {

  private final ObjectMapper mapper = JsonUtils.getMapper();
  private final Collection<ClientHandler> clients;

  /**
   * Creates a {@link MessageBroadcaster} with a collection of client handlers.
   *
   * @param clients collection of {@link ClientHandler} that can send messages to clients
   */
  public MessageBroadcaster(Collection<ClientHandler> clients) {
    this.clients = clients;
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
      for (ClientHandler client : clients) {
        client.send(json);
      }
    } catch (Exception e) {
      System.err.println("Failed to serialize GameMessage: " + e.getMessage());
    }
  }
}
