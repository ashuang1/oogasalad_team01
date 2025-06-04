package oogasalad.engine.records;

import java.util.Map;
import java.util.Set;
import oogasalad.player.model.strategies.control.RemoteControlStrategy;

/**
 * A record that encapsulates the context for a multiplayer game.
 *
 * @param localPlayerId   playerId of local client
 * @param activePlayerIds playerIds of active players
 * @param remoteMap       maps playerId to RemoteControlStrategy
 */
public record MultiplayerContextRecord(int localPlayerId, Set<Integer> activePlayerIds,
                                       Map<Integer, RemoteControlStrategy> remoteMap) {

}
