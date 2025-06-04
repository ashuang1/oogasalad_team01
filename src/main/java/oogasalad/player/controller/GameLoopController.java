package oogasalad.player.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.animation.AnimationTimer;
import oogasalad.engine.config.EntityPlacement;
import oogasalad.engine.exceptions.EntityNotFoundException;
import oogasalad.engine.exceptions.InvalidPositionException;
import oogasalad.engine.records.GameContextRecord;
import oogasalad.engine.records.MultiplayerContextRecord;
import oogasalad.engine.records.config.ConfigModelRecord;
import oogasalad.engine.records.config.model.ModeChangeInfo;
import oogasalad.engine.records.config.model.ParsedLevelRecord;
import oogasalad.engine.records.config.model.SpawnEventRecord;
import oogasalad.engine.records.model.EntityTypeRecord;
import oogasalad.engine.records.model.ModeChangeEventRecord;
import oogasalad.engine.utility.LoggingManager;
import oogasalad.player.model.Entity;
import oogasalad.player.model.api.ModeChangeEventStrategyFactory;
import oogasalad.player.model.api.SpawnEventStrategyFactory;
import oogasalad.player.model.enums.CheatType;
import oogasalad.player.model.strategies.modechangeevent.ModeChangeEventStrategyInterface;
import oogasalad.player.model.strategies.spawnevent.SpawnEventStrategyInterface;
import oogasalad.player.view.GameMapView;

/**
 * A controller class that handles the game loop.
 */
public class GameLoopController {

  private AnimationTimer myGameLoop;
  private final GameContextRecord myGameContext;
  private final GameInputManager myGameInputManager;
  private final GameMapView myGameMapView;
  private final ParsedLevelRecord myLevel;
  private final Map<SpawnEventRecord, Entity> activeSpawnedEntities = new HashMap<>();
  private final ConfigModelRecord myConfig;
  private double myGameSpeedMultiplier;
  private double myTotalElapsedTime = 0;
  private final Map<String, Double> respawnableEntities;
  private final Map<String, Double> entityRespawnTimers = new HashMap<>();
  private final Map<String, Double> lastRespawnTimes = new HashMap<>();
  private double lastUpdateTime = -1;

  private MultiplayerContextRecord mpContext;

  /**
   * Initialize the game loop controller and start the animation. Calling the constructor will
   * automatically start the animation.
   *
   * @param gameConfig  The game config used for this game loop.
   * @param gameContext The game context to use for updating each frame.
   * @param gameMapView The game map view used with this animation loop.
   * @param level       The parsed level information for this game loop.
   */
  public GameLoopController(ConfigModelRecord gameConfig, GameContextRecord gameContext,
      GameMapView gameMapView,
      ParsedLevelRecord level, MultiplayerContextRecord mpContext) {
    myGameContext = gameContext;
    myGameMapView = gameMapView;
    myGameInputManager = gameContext.inputManager();
    myLevel = level;
    myGameSpeedMultiplier = gameConfig.settings().gameSpeed();
    myConfig = gameConfig;
    this.respawnableEntities = gameConfig.respawnableEntities();
    this.mpContext = mpContext;
    initializeGameLoop();
  }
  // this and following methods are written by ChatGPT

  /**
   * Initializes and starts the game loop using AnimationTimer.
   */
  private void initializeGameLoop() {
    myGameLoop = new AnimationTimer() {

      @Override
      public void handle(long now) {
        if (lastUpdateTime < 0) {
          lastUpdateTime = now;
          return;
        }

        double elapsedTime = (now - lastUpdateTime) / 1_000_000_000.0;
        double updateFrequency = (1.0 / 60.0) * (1 / myGameSpeedMultiplier);
        if (elapsedTime > updateFrequency) {
          updateGame();
          clearActiveSpawnedEntitiesIfTimeElapsedWasReset();
          myTotalElapsedTime += elapsedTime * myGameSpeedMultiplier;
          myGameContext.gameState()
              .setTimeElapsed(
                  myGameContext.gameState().getTimeElapsed() + elapsedTime * myGameSpeedMultiplier);
          lastUpdateTime = now;
        }
      }
    };
    myGameLoop.start(); // Start the game loop
  }

  private void clearActiveSpawnedEntitiesIfTimeElapsedWasReset() {
    if (myGameContext.gameState().getTimeElapsed() < myTotalElapsedTime) {
      for (Entity entity : activeSpawnedEntities.values()) {
        attemptRemovingEntity(entity);
      }
      activeSpawnedEntities.clear();
      myTotalElapsedTime = 0;
    }
  }

  private void attemptRemovingEntity(Entity entity) {
    if (myGameContext.gameMap().contains(entity)) {
      try {
        myGameContext.gameMap().removeEntity(entity);
      } catch (EntityNotFoundException e) {
        LoggingManager.LOGGER.warn("Unable to remove entity {}", entity, e);
      }
    }
  }

  /**
   * Updates the game state and refreshes the entity positions.
   */
  private void updateGame() {
    //Updates the game map and entity positions
    myGameContext.gameMap().update();
    myGameMapView.update();
    if (myGameInputManager != null) {
      checkCheatKeys();
    }
    handleModeChangeEvents();
    handleSpawnEvents();
    updateRespawnTimers();
    checkRespawnableEntities();
  }

  private void checkRespawnableEntities() {
    double currentTime = myGameContext.gameState().getTimeElapsed();

    for (Map.Entry<String, Double> entry : myConfig.respawnableEntities().entrySet()) {
      String entityTypeName = entry.getKey();
      double respawnInterval = entry.getValue();

      double lastRespawnTime = lastRespawnTimes.getOrDefault(entityTypeName, -respawnInterval);

      if (currentTime - lastRespawnTime >= respawnInterval) {
        EntityTypeRecord entityType = myConfig.getEntityTypeByName(entityTypeName);
        respawnEntity(entityType);
        lastRespawnTimes.put(entityTypeName, currentTime);
      }
    }
  }

  private void updateRespawnTimers() {
    double deltaTime = myGameContext.gameState().getTimeElapsed();

    Iterator<Map.Entry<String, Double>> iterator = entityRespawnTimers.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Double> entry = iterator.next();
      String entityTypeName = entry.getKey();
      double currentTimer = entry.getValue() + deltaTime;
      double requiredRespawnTime = myConfig.respawnableEntities().get(entityTypeName);

      if (currentTimer >= requiredRespawnTime) {
        EntityTypeRecord entityType = myConfig.getEntityTypeByName(entityTypeName);
        respawnEntity(entityType);
        iterator.remove(); // Remove after respawning
      } else {
        entry.setValue(currentTimer); // Update timer
      }
    }
  }

  private void respawnEntity(EntityTypeRecord entityType) {
    try {
      List<double[]> openSpots = findAvailableSpots();

      if (openSpots.isEmpty()) {
        logNoAvailableSpots(entityType);
        return;
      }

      double[] chosenSpot = pickRandomSpot(openSpots);
      spawnEntityAtSpot(entityType, chosenSpot);

    } catch (InvalidPositionException e) {
      LoggingManager.LOGGER.warn("Failed to respawn entity {}", entityType.type(), e);
    }
  }

  private List<double[]> findAvailableSpots() {
    List<double[]> openSpots = new ArrayList<>();
    int mapWidth = myGameContext.gameMap().getWidth() / 2;
    int mapHeight = myGameContext.gameMap().getHeight();

    for (int y = 0; y < mapHeight; y++) {
      for (int x = 0; x < mapWidth; x++) {
        if (myGameContext.gameMap().getEntityAt(x, y).isEmpty()) {
          openSpots.add(new double[]{x, y});
        }
      }
    }
    return openSpots;
  }

  private void logNoAvailableSpots(EntityTypeRecord entityType) {
    LoggingManager.LOGGER.warn("No available spots to respawn {}", entityType.type());
  }

  private double[] pickRandomSpot(List<double[]> openSpots) {
    return openSpots.get((int) (Math.random() * openSpots.size()));
  }

  private void spawnEntityAtSpot(EntityTypeRecord entityType, double[] spot)
      throws InvalidPositionException {
    double spawnX = spot[0];
    double spawnY = spot[1];

    Entity newEntity = new Entity(
        myGameContext.inputManager(),
        new EntityPlacement(entityType, spawnX, spawnY, "Default"),
        myGameContext.gameMap(),
        myConfig, mpContext
    );
    myGameContext.gameMap().addEntity(newEntity);
  }

  private void handleSpawnEvents() {
    for (SpawnEventRecord spawnEvent : myLevel.spawnEvents()) {
      handleSpawnEvent(spawnEvent);
    }
  }

  private void handleSpawnEvent(SpawnEventRecord spawnEvent) {
    checkAndHandleSpawn(spawnEvent);
    checkAndHandleDespawn(spawnEvent);
  }

  private void checkAndHandleSpawn(SpawnEventRecord spawnEvent) {
    SpawnEventStrategyInterface spawnEventStrategy = SpawnEventStrategyFactory.createSpawnEventStrategy(
        spawnEvent.spawnCondition().type());
    if (spawnEventStrategy.shouldSpawn(spawnEvent, myGameContext
    )) {
      spawnEntity(spawnEvent);
    }
  }

  private void checkAndHandleDespawn(SpawnEventRecord spawnEvent) {
    SpawnEventStrategyInterface despawnEventStrategy = SpawnEventStrategyFactory.createSpawnEventStrategy(
        spawnEvent.despawnCondition().type());
    if (despawnEventStrategy.shouldDespawn(spawnEvent, myGameContext
    )) {
      despawnEntity(spawnEvent);
    }
  }


  private void spawnEntity(SpawnEventRecord spawnEvent) {
    // only spawn if it has not already been spawned
    if (!activeSpawnedEntities.containsKey(spawnEvent)) {
      // Create and spawn entity
      Entity newEntity = new Entity(null,
          new EntityPlacement(spawnEvent.entityType(), spawnEvent.x(), spawnEvent.y(),
              spawnEvent.mode()),
          myGameContext.gameMap(), myConfig, mpContext);
      myGameContext.gameMap().incrementEntityCount(newEntity.getEntityPlacement().getTypeString());
      try {
        myGameContext.gameMap().addEntity(newEntity);
        activeSpawnedEntities.put(spawnEvent, newEntity);
      } catch (InvalidPositionException e) {
        LoggingManager.LOGGER.warn("Could not add spawn entity {}", spawnEvent);
        throw new RuntimeException(e);
      }
    }
  }

  private void despawnEntity(SpawnEventRecord spawnEvent) {
    Entity entityToRemove = activeSpawnedEntities.get(spawnEvent);
    attemptRemovingEntity(entityToRemove);
    activeSpawnedEntities.remove(spawnEvent);
    if (entityToRemove != null) {
      String entityType = entityToRemove.getEntityPlacement().getTypeString();
      if (respawnableEntities.containsKey(entityType)) {
        entityRespawnTimers.put(entityType, 0.0);
      }
    }
  }

  private void handleModeChangeEvents() {
    handleModeReversion();
    for (ModeChangeEventRecord modeChangeEvent : myLevel.modeChangeEvents()) {
      handleModeChangeEvent(modeChangeEvent);
    }
  }

  private void handleModeChangeEvent(ModeChangeEventRecord modeChangeEvent) {
    ModeChangeEventStrategyInterface modeChangeEventStrategy = ModeChangeEventStrategyFactory.createSpawnEventStrategy(
        modeChangeEvent.changeCondition().type());
    if (modeChangeEventStrategy.shouldChange(modeChangeEvent, myGameContext
    )) {
      performModeChange(modeChangeEvent);
    }
  }

  private void performModeChange(ModeChangeEventRecord modeChangeEvent) {
    for (Entity entity : myGameContext.gameMap()) {
      if (!myGameContext.gameMap().getActiveModeChanges().containsKey(entity)) {
        if (entity.getEntityPlacement().getTypeString()
            .equals(modeChangeEvent.entityType().type())) {
          myGameContext.gameMap().getActiveModeChanges()
              .put(entity, modeChangeEvent.modeChangeInfo());
          entity.getEntityPlacement().setMode(modeChangeEvent.modeChangeInfo().transitionMode());
        }
      }
    }
  }

  private void handleModeReversion() {

    double currentTime = myGameContext.gameState().getTimeElapsed();
    Iterator<Map.Entry<Entity, ModeChangeInfo>> iterator =
        myGameContext.gameMap().getActiveModeChanges().entrySet().iterator();

    while (iterator.hasNext()) {
      Map.Entry<Entity, ModeChangeInfo> entry = iterator.next();
      Entity entity = entry.getKey();
      ModeChangeInfo info = entry.getValue();
      if (currentTime >= info.transitionTime() && currentTime < info.revertTime()) {
        entity.getEntityPlacement().setMode(info.transitionMode());
      }
      if (currentTime >= info.revertTime()) {
        entity.getEntityPlacement().setMode(info.originalMode());
        entity.updateControlStrategy();
        iterator.remove();
      }
    }
  }

  /**
   * Stops the loop when called
   */
  public void pauseGame() {
    if (myGameLoop != null) {
      myGameLoop.stop();
    }
  }

  /**
   * Starts the loop again
   */
  public void resumeGame() {
    if (myGameLoop != null) {
      lastUpdateTime = -1;
      myGameLoop.start();
    }
  }

  private void checkCheatKeys() {
    if (myConfig.settings().cheatTypes() != null) {
      for (CheatType cheat : myConfig.settings().cheatTypes()) {
        cheat.execute(myGameInputManager, myGameContext, this);
      }
    }
  }

  /**
   * Return multiplier
   *
   * @return multiplier
   */
  public double getGameSpeedMultiplier() {
    return myGameSpeedMultiplier;
  }

  /**
   * Set multiplier
   *
   * @param myGameSpeedMultiplier multiplier
   */
  public void setGameSpeedMultiplier(double myGameSpeedMultiplier) {
    this.myGameSpeedMultiplier = myGameSpeedMultiplier;
  }

}
