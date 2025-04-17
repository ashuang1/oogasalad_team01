package oogasalad.player.model.control;


import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import oogasalad.engine.input.GameInputManager;
import oogasalad.engine.model.EntityPlacement;
import oogasalad.engine.model.GameMapInterface;
import oogasalad.engine.model.controlConfig.ControlConfigInterface;
import oogasalad.player.model.exceptions.ControlStrategyException;

/**
 * The {@code ControlStrategyFactory} class is responsible for dynamically creating instances of
 * {@link ControlStrategyInterface} based on the control type specified in an
 * {@link EntityPlacement}. It uses reflection to identify and instantiate the appropriate control
 * strategy class, supporting constructors with varying argument requirements.
 *
 * <p>This factory class provides flexibility in creating control strategies by allowing
 * different implementations to be dynamically loaded at runtime. It supports constructors with
 * zero, two, or three arguments, ensuring compatibility with various control strategy
 * implementations.</p>
 *
 * <p>Usage of this class involves providing a {@link GameInputManager}, an
 * {@link EntityPlacement}, and a {@link GameMapInterface} to the
 * {@link #createControlStrategy(GameInputManager, EntityPlacement, GameMapInterface)} method, which returns
 * an instance of the appropriate {@link ControlStrategyInterface}.</p>
 *
 * <p>Note: This class assumes that the control strategy classes follow a specific naming
 * convention and are located within the {@code oogasalad.player.model.control} package. If the
 * required control strategy class cannot be found or instantiated, a
 * {@link ControlStrategyException} is thrown.</p>
 *
 * <p>Example control strategy naming convention: If the control type is "Keyboard", the
 * corresponding class should be named {@code KeyboardControlStrategy} and located in the specified
 * package.</p>
 *
 * <p>Potential exceptions include:</p>
 * <ul>
 *   <li>{@link ControlStrategyException} - Thrown if the control strategy cannot be created or instantiated.</li>
 * </ul>
 *
 * @author Jessica Chen
 */
public class ControlStrategyFactory {

  private static String STRATEGY_PACKAGE = "oogasalad.player.model.control."; // Keep field as global and non-final for testing purposes.

  /**
   * Creates a {@link ControlStrategyInterface} instance based on the control type of the given
   * {@link EntityPlacement}.
   *
   * @param input           the {@link GameInputManager} to be used by the control strategy
   * @param entityPlacement the {@link EntityPlacement} containing the control type
   * @param gameMap         the {@link GameMapInterface} to be used by the control strategy
   * @return an instance of {@link ControlStrategyInterface} corresponding to the control type
   * @throws ControlStrategyException if the control strategy cannot be created or instantiated
   */
  public static ControlStrategyInterface createControlStrategy(
      GameInputManager input, EntityPlacement entityPlacement,
      GameMapInterface gameMap)
      throws ControlStrategyException {
    ControlConfigInterface controlConfig = entityPlacement.getType().controlConfig();
    String className =
        STRATEGY_PACKAGE + controlConfig.getClass().getSimpleName().replace("Config", "Strategy");

    try {
      Class<?> strategyClass = Class.forName(className);
      return instantiateStrategy(strategyClass, input, entityPlacement, gameMap, controlConfig);
    } catch (Exception e) {
      throw new ControlStrategyException(
          "Failed to create strategy for control type: " + controlConfig.getClass() + " "
              + className, e);
    }
  }

  private static ControlStrategyInterface instantiateStrategy(Class<?> strategyClass,
      GameInputManager input,
      EntityPlacement placement,
      GameMapInterface gameMap, ControlConfigInterface controlConfig)
      throws ControlStrategyException {
    try {
      for (Constructor<?> constructor : strategyClass.getConstructors()) {
        if (isPublicConstructor(constructor)) {
          ControlStrategyInterface strategy = tryInstantiateStrategy(constructor, input, gameMap,
              placement,
              controlConfig);
          if (strategy != null) {
            return strategy;
          }
        }
      }
    } catch (Exception e) {
      throw new ControlStrategyException("Failed to instantiate strategy", e);
    }

    throw new ControlStrategyException(
        "No valid constructor found for: " + strategyClass.getName());
  }

  private static ControlStrategyInterface tryInstantiateStrategy(Constructor<?> constructor,
      GameInputManager input,
      GameMapInterface gameMap,
      EntityPlacement placement, ControlConfigInterface controlConfig)
      throws ControlStrategyException {
    try {
      // target and bfs, keyboard uses 3, basic uses 0
      if (matchesZeroArgConstructor(constructor)) {
        return (ControlStrategyInterface) constructor.newInstance(
        );
      }

      if (matchesThreeArgConstructorConfig(constructor)) {
        return (ControlStrategyInterface) constructor.newInstance(
            gameMap,
            placement,
            controlConfig
        );
      }

      if (matchesThreeArgConstructorInput(constructor)) {
        return (ControlStrategyInterface) constructor.newInstance(
            input,
            gameMap,
            placement
        );
      }

      return null;
    } catch (Exception e) {
      throw new ControlStrategyException("Failed to instantiate strategy", e);
    }
  }


  private static boolean matchesZeroArgConstructor(Constructor<?> constructor) {
    Class<?>[] paramTypes = constructor.getParameterTypes();
    return paramTypes.length == 0;
  }

  private static boolean matchesThreeArgConstructorConfig(Constructor<?> constructor) {
    Class<?>[] paramTypes = constructor.getParameterTypes();
    return paramTypes.length == 3 &&
        paramTypes[0].equals(GameMapInterface.class) &&
        paramTypes[1].equals(EntityPlacement.class) &&
        paramTypes[2].equals(ControlConfigInterface.class);
  }

  private static boolean matchesThreeArgConstructorInput(Constructor<?> constructor) {
    Class<?>[] paramTypes = constructor.getParameterTypes();
    return paramTypes.length == 3 &&
        paramTypes[0].equals(GameInputManager.class) &&
        paramTypes[1].equals(GameMapInterface.class) &&
        paramTypes[2].equals(EntityPlacement.class);
  }

  private static boolean isPublicConstructor(Constructor<?> constructor) {
    return Modifier.isPublic(constructor.getModifiers());
  }

}
