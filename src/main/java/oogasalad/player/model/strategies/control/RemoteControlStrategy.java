package oogasalad.player.model.strategies.control;

import oogasalad.engine.utility.constants.Directions.Direction;
import oogasalad.player.model.Entity;

/**
 * A control strategy that moves the entity based on direction messages
 * received from the server (e.g., from another player's inputs).
 */
public class RemoteControlStrategy implements ControlStrategyInterface {

  private Direction latestDirection;

  public void setDirectionFromNetwork(Direction direction) {
    this.latestDirection = direction;
  }

  @Override
  public void update(Entity entity) {
    if (latestDirection != null && entity.canMove(latestDirection)) {
      entity.setEntityDirection(latestDirection);
    }
  }
}
