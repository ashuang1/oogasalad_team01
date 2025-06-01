package oogasalad.networking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the ready status map for all players and checks if all players are ready.
 *
 * @author Austin Huang
 */
public class LobbyStateManager {

  private final Map<Integer, Boolean> readyStatus = new ConcurrentHashMap<>();

  /**
   * Updates the ready status of a player.
   *
   * @param playerID player whose ready status has changed
   * @param isReady updated ready status
   */
  public void updateReady(int playerID, boolean isReady) {
    readyStatus.put(playerID, isReady);
  }

  /**
   * @return readyStatus map of all player ready statuses.
   */
  public Map<Integer, Boolean> getAllStatuses() {
    return readyStatus;
  }

  /**
   * @return if all players are ready.
   */
  public boolean allReady() {
    return !readyStatus.isEmpty() && readyStatus.values().stream().allMatch(Boolean::booleanValue);
  }

  /**
   * Removes player from ready status, typically after disconnection from server.
   *
   * @param playerID player to be removed
   */
  public void removePlayer(int playerID) {
    readyStatus.remove(playerID);
  }
}
