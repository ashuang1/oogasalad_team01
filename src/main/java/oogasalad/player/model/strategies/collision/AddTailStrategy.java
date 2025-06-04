package oogasalad.player.model.strategies.collision;

import oogasalad.engine.config.EntityPlacement;
import oogasalad.engine.exceptions.InvalidPositionException;
import oogasalad.engine.records.CollisionContextRecord;
import oogasalad.engine.records.MultiplayerContextRecord;
import oogasalad.engine.records.model.EntityTypeRecord;
import oogasalad.player.model.Entity;
import oogasalad.player.model.api.EntityFactory;

/**
 * Adds tail to pacman entity for snake game
 */
public class AddTailStrategy implements CollisionStrategyInterface {

  @Override
  public void handleCollision(CollisionContextRecord context) throws InvalidPositionException {
    Entity movingEntity = context.entity1();

    double previousX = movingEntity.getEntityPlacement().getPreviousX();
    double previousY = movingEntity.getEntityPlacement().getPreviousY();

    if (previousX != movingEntity.getEntityPlacement().getX()
        || previousY != movingEntity.getEntityPlacement().getY()) {

      EntityTypeRecord tailType = movingEntity.getConfig().entityTypes().get(3);

      EntityPlacement tailPlacement = new EntityPlacement(
          tailType,
          previousX,
          previousY,
          "Default"
      );

      Entity tail = EntityFactory.createEntity(
          null,
          tailPlacement,
          context.gameMap(),
          movingEntity.getConfig(),
          new MultiplayerContextRecord(-1, null, null)
      );

      context.gameMap().addEntity(tail);
    }
  }
}
