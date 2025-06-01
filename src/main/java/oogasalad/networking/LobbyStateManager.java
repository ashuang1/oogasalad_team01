package oogasalad.networking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyStateManager {

  private final Map<Integer, Boolean> readyStatus = new ConcurrentHashMap<>();

  public void updateReady(int playerID, boolean isReady) {
    readyStatus.put(playerID, isReady);
  }

  public Map<Integer, Boolean> getAllStatuses() {
    return readyStatus;
  }

  public boolean allReady() {
    return !readyStatus.isEmpty() && readyStatus.values().stream().allMatch(Boolean::booleanValue);
  }

  public void removePlayer(int playerID) {
    readyStatus.remove(playerID);
  }
}
