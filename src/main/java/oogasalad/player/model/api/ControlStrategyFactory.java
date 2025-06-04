package oogasalad.player.model.api;


import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import oogasalad.engine.config.EntityPlacement;
import oogasalad.engine.records.MultiplayerContextRecord;
import oogasalad.engine.records.config.ModeConfigRecord;
import oogasalad.engine.records.config.model.controlConfig.ControlConfigInterface;
import oogasalad.player.controller.GameInputManager;
import oogasalad.player.model.GameMapInterface;
import oogasalad.player.model.exceptions.ControlStrategyException;
import oogasalad.player.model.strategies.control.ControlStrategyInterface;
import oogasalad.player.model.strategies.control.KeyboardControlStrategy;
import oogasalad.player.model.strategies.control.RemoteControlStrategy;

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
 * {@link #createControlStrategy(GameInputManager, EntityPlacement, GameMapInterface,
 * MultiplayerContextRecord)} method, which
 * returns an instance of the appropriate {@link ControlStrategyInterface}.</p>
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

  private static String STRATEGY_PACKAGE = "oogasalad.player.model.strategies.control."; // Keep field as global and non-final for testing purposes.

  /**
   * Creates a {@link ControlStrategyInterface} instance based on the control type of the given
   * {@link EntityPlacement}.
   *
   * @param input           the {@link GameInputManager} to be used by the control strategy
   * @param entityPlacement the {@link EntityPlacement} containing the control type
   * @param gameMap         the {@link GameMapInterface} to be used by the control strategy
   * @param mpContext       record containing localPlayerId, activePlayerIds, remoteMap
   * @return an instance of {@link ControlStrategyInterface} corresponding to the control type
   * @throws ControlStrategyException if the control strategy cannot be created or instantiated
   */
  public static ControlStrategyInterface createControlStrategy(
      GameInputManager input, EntityPlacement entityPlacement,
      GameMapInterface gameMap, MultiplayerContextRecord mpContext)
      throws ControlStrategyException {

    String mode = entityPlacement.getMode();
    ModeConfigRecord modeConfig = entityPlacement.getType().modes().get(mode);
    ControlConfigInterface controlConfig = modeConfig.controlConfig();
    Integer playerIdOfEntity = controlConfig.getPlayer();

    if (mpContext != null) {
      ControlStrategyInterface strategy = getControlStrategyForNetworkedGame(
          input, entityPlacement, gameMap, mpContext.localPlayerId(), mpContext.activePlayerIds(),
          mpContext.remoteMap(), playerIdOfEntity);
      if (strategy != null) {
        return strategy;
      }
    }

    String className =
        STRATEGY_PACKAGE + controlConfig.getClass().getSimpleName()
            .replace("ConfigRecord", "Strategy");

    try {
      Class<?> strategyClass = Class.forName(className);
      return instantiateStrategy(strategyClass, input, entityPlacement, gameMap, controlConfig);
    } catch (Exception e) {
      throw new ControlStrategyException(
          "Failed to create strategy for control type: " + controlConfig.getClass() + " "
              + className, e);
    }
  }

  private static ControlStrategyInterface getControlStrategyForNetworkedGame(GameInputManager input,
      EntityPlacement entityPlacement, GameMapInterface gameMap, int localPlayerId,
      Set<Integer> activePlayerIds, Map<Integer, RemoteControlStrategy> remoteMap,
      Integer playerIdOfEntity) {
    if (playerIdOfEntity != null && activePlayerIds != null
        && activePlayerIds.contains(playerIdOfEntity)) {
      if (playerIdOfEntity == localPlayerId) {
        return new KeyboardControlStrategy(input, gameMap, entityPlacement);
      } else if (remoteMap != null) {
        RemoteControlStrategy remote = new RemoteControlStrategy();
        remoteMap.put(playerIdOfEntity, remote);
        return remote;
      }
    }
    return null;
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

      if (matchesFourArgConstructor(constructor)) {
        return (ControlStrategyInterface) constructor.newInstance(
            input,
            gameMap,
            placement,
            controlConfig
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

  private static boolean matchesFourArgConstructor(Constructor<?> constructor) {
    Class<?>[] paramTypes = constructor.getParameterTypes();
    return paramTypes.length == 4 &&
        paramTypes[0].equals(GameInputManager.class) &&
        paramTypes[1].equals(GameMapInterface.class) &&
        paramTypes[2].equals(EntityPlacement.class) &&
        paramTypes[3].equals(ControlConfigInterface.class);
  }

  private static boolean isPublicConstructor(Constructor<?> constructor) {
    return Modifier.isPublic(constructor.getModifiers());
  }

}
