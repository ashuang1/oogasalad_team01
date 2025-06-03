package oogasalad.player.view;

import static oogasalad.engine.utility.constants.GameConfig.WIDTH;

import java.io.IOException;
import java.util.Set;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import oogasalad.engine.config.JsonConfigParser;
import oogasalad.engine.controller.MainController;
import oogasalad.engine.exceptions.ConfigException;
import oogasalad.engine.records.GameContextRecord;
import oogasalad.engine.records.config.ConfigModelRecord;
import oogasalad.engine.utility.LoggingManager;
import oogasalad.player.controller.LevelController;
import oogasalad.player.model.GameStateInterface;
import oogasalad.player.model.exceptions.SaveFileException;
import oogasalad.player.model.save.GameSessionManager;

/**
 * The view that displays only the game grid.
 *
 * @author Owen Jennings
 */
public class GamePlayerView {

  public static final String GAME_CONFIG_JSON = "gameConfig.json";

  private final String gameFolderPath;
  private final StackPane myPane;
  private final MainController myMainController;
  private final GameStateInterface myGameState;
  private final boolean isRandomized;
  private GameView myGameView;
  private ConfigModelRecord myConfigModel = null;
  private final GameSessionManager sessionManager;
  private LevelController levelController;

  private int myPlayerId;
  private Set<Integer> activePlayerIds;

  /**
   * Constructs a GamePlayerView object that represents the visual interface for the game player.
   *
   * @param controller     the main controller that manages the game logic and interactions
   * @param gameState      the current state of the game, providing access to game data
   * @param gameFolderPath the name of the folder containing game-specific resources
   * @param randomized     a flag indicating whether the game is randomized
   */
  public GamePlayerView(MainController controller, GameStateInterface gameState,
      String gameFolderPath, boolean randomized) {
    myPane = new StackPane();
    myMainController = controller;
    myGameState = gameState;
    this.isRandomized = randomized;
    this.gameFolderPath = gameFolderPath;
    this.sessionManager = new GameSessionManager(this.gameFolderPath,
        extractFolderName(gameFolderPath) + "_Save");

    myPane.setPrefWidth(WIDTH);
    myPane.getStyleClass().add("game-player-view");

    initializeGame();
  }

  /**
   * Get the root stack pane that is used to display elements in the view.
   *
   * @return A StackPane JavaFX object that is added to for this view.
   */
  public StackPane getPane() {
    return myPane;
  }

  private void initializeGame() {
    loadConfigFromFile();
    loadOrCreateSession();
    updateGameStateFromSession();

    levelController = new LevelController(myMainController, myConfigModel, isRandomized,
        sessionManager);
    loadGameViewFromSession();
  }

  private void loadOrCreateSession() {
    try {
      sessionManager.loadExistingSession();
      LoggingManager.LOGGER.info("Loaded existing save file for '{}'", gameFolderPath);
    } catch (IOException e) {
      sessionManager.startNewSession(myConfigModel);
      LoggingManager.LOGGER.info("No save found, created new session for '{}'", gameFolderPath);
    }
  }

  private void updateGameStateFromSession() {
    myGameState.resetState();
    myGameState.setLives(sessionManager.getLives());
    myGameState.updateScore(sessionManager.getCurrentScore());
    myGameState.updateHighScore(sessionManager.getHighScore());
  }

  private void loadConfigFromFile() {
    JsonConfigParser configParser = new JsonConfigParser();
    try {
      myConfigModel = configParser.loadFromFile(gameFolderPath + "/gameConfig.json");
    } catch (ConfigException e) {
      LoggingManager.LOGGER.warn("Failed to load config file: {}", GAME_CONFIG_JSON, e);
    }
  }

  private void loadGameViewFromSession() {
    clearPreviousGameView();
    if (levelController.getCurrentLevelMap() != null) {
      initializeGameView();
      addGameViewActions();
      myPane.getChildren().add(buildFullView(myGameView.getRoot()));
    }
  }

  private void clearPreviousGameView() {
    if (myGameView != null) {
      myPane.getChildren().remove(myGameView.getRoot());
    }
  }

  private void initializeGameView() {
    int logicalIndex = levelController.getCurrentLevelIndex();
    int actualMappedIndex = sessionManager.getLevelOrder().get(logicalIndex);

    LoggingManager.LOGGER.info("Loading mapped level {} (logical index {})", actualMappedIndex,
        logicalIndex);

    myGameView = new GameView(
        new GameContextRecord(myMainController.getInputManager(),
            levelController.getCurrentLevelMap(), myGameState),
        myConfigModel,
        logicalIndex,
        sessionManager,
        (gameFolderPath + "/")
    );
  }

  private void addGameViewActions() {
    myGameView.setNextLevelAction(this::handleNextLevel);
    myGameView.setResetAction(this::handleResetGame);
    myGameView.setSaveAction(this::handleSaveGame);
  }

  private String extractFolderName(String path) {
    String[] parts = path.split("/");
    return parts[parts.length - 1];
  }


  private void handleSaveGame() {
    try {
      if (myGameView.isPendingLevelAdvance()) {
        sessionManager.advanceLevel(myGameState.getScore());
        LoggingManager.LOGGER.info("Level won: saving with next level progress!");
      }
      sessionManager.updateHighScore(myGameState.getHighScore());
      sessionManager.save();
      LoggingManager.LOGGER.info("Game saved successfully!");
    } catch (SaveFileException e) {
      LoggingManager.LOGGER.warn("Failed to save game progress: {}", e.getMessage());
    }
  }

  private BorderPane buildFullView(StackPane gameViewRoot) {
    BorderPane container = new BorderPane();
    container.setCenter(gameViewRoot);

    return container;
  }

  /**
   * Moves to next level if there is a next level
   */
  public void handleNextLevel() {
    if (levelController.hasNextLevel()) {
      levelController.incrementLevel();
      sessionManager.advanceLevel(myGameState.getScore());
      sessionManager.updateHighScore(myGameState.getHighScore());

      refreshGame();
    } else {
      LoggingManager.LOGGER.info("All levels complete — cannot advance further.");
    }
  }

  /**
   * Resets current game to its starting position including Game State
   */
  public void handleResetGame() {
    myGameState.resetTimeElapsed();
    sessionManager.resetSession(myConfigModel);
    refreshGame();
  }

  private void refreshGame() {
    myPane.getChildren().clear();

    updateGameStateFromSession();

    levelController = new LevelController(myMainController, myConfigModel, isRandomized,
        sessionManager);
    loadGameViewFromSession();

    myGameView.resetControlledEntitiesToSpawn();

  }


  /**
   * Returns privately stored GameView.
   */
  public GameView getGameView() {
    return myGameView;
  }

  /**
   * Returns the game session manager for this view.
   *
   * @return A game session manager object.
   */
  public GameSessionManager getGameSessionManager() {
    return sessionManager;
  }

  /**
   * Set playerId and activePlayerIds obtained from network client.
   *
   * @param playerId client's player id
   * @param activePlayerIds set of active player ids
   */
  public void setPlayerContext(int playerId, Set<Integer> activePlayerIds) {
    this.myPlayerId = playerId;
    this.activePlayerIds = activePlayerIds;
  }
}
