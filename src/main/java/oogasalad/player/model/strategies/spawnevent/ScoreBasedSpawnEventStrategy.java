package oogasalad.player.model.strategies.spawnevent;

import oogasalad.engine.records.GameContextRecord;
import oogasalad.engine.records.config.model.SpawnEventRecord;
import oogasalad.engine.utility.LoggingManager;

/**
 * A spawn event strategy that handles score based spawning and despawn.
 *
 * @author Owen Jennings
 */
public class ScoreBasedSpawnEventStrategy implements SpawnEventStrategyInterface {

  @Override
  public boolean shouldSpawn(SpawnEventRecord spawnEvent, GameContextRecord gameContextRecord) {
    Object amountObj = spawnEvent.spawnCondition().parameters().get("amount");
    if (amountObj == null) {
      LoggingManager.LOGGER.warn(
          "ScoreBasedSpawnEventStrategy spawnCondition requires amount parameter, but it was not provided in the config, defaulting to never spawning entity.");
      return false;
    }
    try {
      int amount = Integer.parseInt(amountObj.toString());
      return gameContextRecord.gameState().getScore() >= amount;
    } catch (NumberFormatException e) {
      LoggingManager.LOGGER.warn(
          "ScoreBasedSpawnEventStrategy spawnCondition parameter 'amount' must be an integer, but received: {}",
          amountObj);
      return false;
    }
  }

  @Override
  public boolean shouldDespawn(SpawnEventRecord spawnEvent, GameContextRecord gameContextRecord) {
    Object amountObj = spawnEvent.despawnCondition().parameters().get("amount");
    if (amountObj == null) {
      LoggingManager.LOGGER.warn(
          "ScoreBasedSpawnEventStrategy despawnCondition requires amount parameter, but it was not provided in the config, defaulting to never despawning entity.");
      return false;
    }
    try {
      int amount = Integer.parseInt(amountObj.toString());
      return gameContextRecord.gameState().getScore() >= amount;
    } catch (NumberFormatException e) {
      LoggingManager.LOGGER.warn(
          "ScoreBasedSpawnEventStrategy despawnCondition parameter 'amount' must be an integer, but received: {}",
          amountObj);
      return false;
    }
  }
}
