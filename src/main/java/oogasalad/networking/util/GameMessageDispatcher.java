package oogasalad.networking.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import oogasalad.networking.GameMessage;
import oogasalad.networking.MessageType;

/**
 * Routes {@link GameMessage} objects to registered handlers based on {@link MessageType}.
 * Used to separate message handling logic from message reception.
 *
 * @author Austin Huang
 */
public class GameMessageDispatcher {

  private final Map<MessageType, Consumer<GameMessage>> handlers = new HashMap<>();

  /**
   * Registers a handler function for a specific {@link MessageType}.
   *
   * @param type    the type of message to associate with the handler
   * @param handler the logic to execute when a message of the given type is received
   */
  public void registerHandler(MessageType type, Consumer<GameMessage> handler) {
    handlers.put(type, handler);
  }

  /**
   * Dispatches a {@link GameMessage} to its registered handler, if one exists.
   *
   * @param message the message to dispatch
   */
  public void dispatch(GameMessage message) {
    System.out.println("Server: " + message);
    Consumer<GameMessage> handler = handlers.get(message.type());
    if (handler != null) {
      handler.accept(message);
    } else {
      System.out.println("Unhandled message type: " + message.type());
    }
  }
}
