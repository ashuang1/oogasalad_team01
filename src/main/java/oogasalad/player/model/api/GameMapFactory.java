package oogasalad.player.model.api;

import oogasalad.engine.config.EntityPlacement;
import oogasalad.engine.exceptions.InvalidPositionException;
import oogasalad.engine.records.MultiplayerContextRecord;
import oogasalad.engine.records.config.ConfigModelRecord;
import oogasalad.player.controller.GameInputManager;
import oogasalad.player.model.Entity;
import oogasalad.player.model.GameMap;
import oogasalad.player.model.GameMapInterface;

/**
 * A factory design pattern to create a GameMap provided a configuration model.
 *
 * @author Owen Jennings
 */
public class GameMapFactory {

  /**
   * Create a game map with the provided configuration model.
   *
   * @param configModel The configuration model you wish to use to create the game map.
   * @param levelIndex  The index of the level you want to load a map for. (0 indexed)
   * @return A game map object
   * @throws InvalidPositionException Whenever the map cannot be created because an entity with an
   *                                  invalid position was added.
   */
  public static GameMapInterface createGameMap(GameInputManager input,
      ConfigModelRecord configModel,
      int levelIndex, MultiplayerContextRecord mcContext)
      throws InvalidPositionException {
    int width = configModel.levels().get(levelIndex).mapInfo().width();
    int height = configModel.levels().get(levelIndex).mapInfo().height();
    GameMap gameMap = new GameMap(width, height); // Hardcoded for now
    for (EntityPlacement entityPlacement : configModel.levels().get(levelIndex).placements()) {
      Entity entity = EntityFactory.createEntity(input, entityPlacement, gameMap, configModel,
          mcContext);
      gameMap.addEntity(entity);
    }
    return gameMap;
  }
}
