package oogasalad.player.model.strategies.control.pathfinding;

import java.util.List;
import oogasalad.engine.config.EntityPlacement;
import oogasalad.engine.utility.constants.Directions.Direction;
import oogasalad.player.model.GameMapInterface;

/**
 * A strategy for pathfinding that calculates the direction to move in order to minimize the
 * Euclidean distance to a specified target.
 */
public class EuclideanPathFindingStrategy implements PathFindingStrategyInterface {

  @Override
  public int[] getPath(GameMapInterface map, int startX, int startY, int targetX, int targetY,
      EntityPlacement thisEntity, Direction thisDirection) {
    List<int[]> possibleDirections = PathFindingStrategyHelperMethods.getValidDirections(map,
        startX, startY, thisEntity, thisDirection);
    int[] bestDirection = findBestDirection(possibleDirections, targetX, targetY);

    return calculateOffset(startX, startY, bestDirection);
  }

  private int[] findBestDirection(List<int[]> directions, int targetX, int targetY) {
    double minDistance = Double.MAX_VALUE;
    int[] bestDirection = null;

    for (int[] pos : directions) {
      double distance = Math.sqrt(Math.pow(pos[0] - (double) targetX, 2) +
          Math.pow(pos[1] - (double) targetY, 2));
      if (distance < minDistance) {
        minDistance = distance;
        bestDirection = pos;
      }
    }
    return bestDirection;
  }

  private int[] calculateOffset(int startX, int startY, int[] bestDirection) {
    if (bestDirection != null) {
      return new int[]{bestDirection[0] - startX, bestDirection[1] - startY};
    }
    return new int[]{0, 0};
  }
}
