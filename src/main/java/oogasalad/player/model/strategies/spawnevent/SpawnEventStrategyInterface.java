package oogasalad.player.model.strategies.spawnevent;

import oogasalad.engine.records.GameContextRecord;
import oogasalad.engine.records.config.model.SpawnEventRecord;

/**
 * An interface that defines how whether a spawn event should be spawned or despawned in current
 * iteration of game loop.
 *
 * @author Owen Jennings
 */
public interface SpawnEventStrategyInterface {

  /**
   * Determine if a spawn event entity should be spawned based on the specific strategy.
   *
   * @param spawnEvent        The spawn event you are checking for
   * @param gameContextRecord The game context information to use in your determination.
   * @return true if an entity should be spawned by the current point in the game, false otherwise.
   */
  boolean shouldSpawn(SpawnEventRecord spawnEvent, GameContextRecord gameContextRecord);

  /**
   * Determine if a spawn event entity should be despawned based on a specific strategy.
   *
   * @param spawnEvent        The spawn event that you are checking to see if it should be
   *                          despawned.
   * @param gameContextRecord The game context information you can use in your determination.
   * @return true if the current entity should be despawned, false otherwise.
   */
  boolean shouldDespawn(SpawnEventRecord spawnEvent, GameContextRecord gameContextRecord);
}
