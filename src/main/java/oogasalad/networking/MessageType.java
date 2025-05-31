package oogasalad.networking;

/**
 * Enum representing standard message types exchanged between client and server.
 */
public enum MessageType {
  HELLO,              // payload: null
  WELCOME,            // payload: null
  READY,              // payload: "ready", boolean
  DISCONNECT,         // payload: null
  PLAYER_STATUS,      // payload: "playerStatuses", Map<Integer, Boolean>
  START,              // payload: "playerIds", List<Integer>
  MOVE,               // paylaod: "direction", Direction
  WIN,
  LOSS
}
