package oogasalad.player.model.strategies.control.targetcalculation;

import java.util.Map;
import java.util.Optional;
import oogasalad.player.model.Entity;
import oogasalad.player.model.GameMapInterface;

/**
 * TargetEntityStrategy is a concrete implementation of the TargetStrategy interface that determines
 * the target position based on the location of a specific type of entity on the game map. This
 * strategy is useful for identifying the position of entities such as players, enemies, or objects
 * during gameplay.
 *
 * <p>The class utilizes a GameMap to search for the first occurrence of an entity of the specified
 * type and calculates its position on the map. The target type is specified during the construction
 * of the object.
 *
 * <p>Example usage:
 *
 * <pre>
 * {@code
 * GameMap gameMap = ...; // Initialize game map
 * TargetStrategy targetStrategy = new TargetEntityStrategy(gameMap, "enemy");
 * int[] targetPosition = targetStrategy.getTargetPosition();
 * }
 * </pre>
 *
 * <p>Note: This class assumes that the specified target type exists on the map. If no entity of
 * the specified type is found, the behavior of the class is undefined (currently 0, 0)..
 *
 * @author Jessica Chen
 * @author Chatgpt for javadoc
 */
public class TargetEntityStrategy implements TargetStrategyInterface {

  private final GameMapInterface myGameMap;
  private final String myTargetType;

  /**
   * Constructs a TargetEntityStrategy object that determines the target based on where that target
   * is on the map
   *
   * @param gameMap        the game map on which the target calculation is performed
   * @param strategyConfig strategy configuration containing the target type
   */
  public TargetEntityStrategy(GameMapInterface gameMap, Map<String, Object> strategyConfig) {
    myGameMap = gameMap;
    myTargetType = TargetStrategyHelperMethods.validateAndGetKeyString(strategyConfig,
        "targetType");
  }

  @Override
  public int[] getTargetPosition() {
    Optional<Entity> entity = TargetStrategyHelperMethods.findFirstEntityOfType(myGameMap,
        myTargetType);

    return entity.map(value -> new int[]{(int) value.getEntityPlacement().getX(),
        (int) value.getEntityPlacement().getY()}).orElseGet(() -> new int[]{0, 0});

  }

}
