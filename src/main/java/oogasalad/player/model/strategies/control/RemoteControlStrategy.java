package oogasalad.player.model.strategies.control;

import oogasalad.engine.config.EntityPlacement;
import oogasalad.engine.utility.constants.Directions.Direction;
import oogasalad.player.controller.GameInputManager;
import oogasalad.player.model.Entity;
import oogasalad.player.model.GameMapInterface;

public class RemoteControlStrategy implements ControlStrategyInterface {

  private final GameMapInterface myGameMap;
  private final EntityPlacement myEntityPlacement;
  private final GameInputManager myInputManager;

  /**
   * Constructs a RemoteControlStrategy with the specified input manager, game map, and entity
   * placement.
   *
   * @param input           the GameInputManager that handles keyboard input
   * @param gameMap         the GameMap representing the current game state
   * @param entityPlacement the EntityPlacement representing the entity's position
   */
  public RemoteControlStrategy(GameInputManager input,
      GameMapInterface gameMap, EntityPlacement entityPlacement) {
    myEntityPlacement = entityPlacement;
    myInputManager = input;
    myGameMap = gameMap;
  }

  @Override
  public void update(Entity entity) {
    updateCurrentDirectionFromKeyboardInput(entity);
  }

  private void updateCurrentDirectionFromKeyboardInput(Entity entity) {
    int myX = (int) myEntityPlacement.getX();
    int myY = (int) myEntityPlacement.getY();
    setEntityDirection(myX, myY, entity);
  }

  private void setEntityDirection(int myX, int myY, Entity entity) {
    setUpDirection(myX, myY, entity);
    setDownDirection(myX, myY, entity);
    setLeftDirection(myX, myY, entity);
    setRightDirection(myX, myY, entity);
  }

  private void setRightDirection(int myX, int myY, Entity entity) {
    if (entity.canMove(Direction.R) && myInputManager.isMovingRight() && checkNoWall(myX + 1,
        myY)) {
      entity.setEntityDirection(Direction.R);
    }
  }

  private void setLeftDirection(int myX, int myY, Entity entity) {
    if (entity.canMove(Direction.L) && myInputManager.isMovingLeft() && checkNoWall(myX - 1, myY)) {
      entity.setEntityDirection(Direction.L);
    }
  }

  private void setDownDirection(int myX, int myY, Entity entity) {
    if (entity.canMove(Direction.D) && myInputManager.isMovingDown() && checkNoWall(myX, myY + 1)) {
      entity.setEntityDirection(Direction.D);
    }
  }

  private void setUpDirection(int myX, int myY, Entity entity) {
    if (entity.canMove(Direction.U) && myInputManager.isMovingUp() && checkNoWall(myX, myY - 1)) {
      entity.setEntityDirection(Direction.U);
    }
  }

  private boolean checkNoWall(int x, int y) {
    return myGameMap.getEntityAt(x, y)
        .filter(entity -> entity.getEntityPlacement().getType().type().equals("Wall"))
        .isEmpty();
  }
}
