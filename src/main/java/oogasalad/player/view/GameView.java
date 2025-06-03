package oogasalad.player.view;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import oogasalad.engine.records.GameContextRecord;
import oogasalad.engine.records.config.ConfigModelRecord;
import oogasalad.engine.utility.LanguageManager;
import oogasalad.engine.utility.LoggingManager;
import oogasalad.engine.utility.constants.GameConfig;
import oogasalad.player.controller.GameLoopController;
import oogasalad.player.model.Entity;
import oogasalad.player.model.save.GameSessionManager;

/**
 * The main game view of the player. Primarily encapsulates the game map view.
 *
 * @author Owen Jennings
 */
public class GameView {

  public static final int GAME_VIEW_WIDTH = GameConfig.WIDTH - 2 * GameConfig.MARGIN;
  public static final int GAME_VIEW_HEIGHT = GameConfig.HEIGHT - 2 * GameConfig.MARGIN;

  private final StackPane myRoot;
  private final GameLoopController myGameLoopController;
  private final Label endLabel = new Label();
  private final Button nextLevelButton = new Button();
  private final Button resetButton = new Button();
  private final Button saveButton = new Button();
  private static final String END_BUTTON_STYLE = "end-button";
  private boolean pendingLevelAdvance = false;
  private final GameSessionManager sessionManager;
  private final GameContextRecord myGameContext;

  /**
   * Create the game view.
   *
   * @param gameContext The game context for this view.
   * @param configModel The config model for this view.
   * @param levelIndex  The index of the level that is displayed on this view.
   * @param gameFolder  The full path to the game folder
   */
  public GameView(GameContextRecord gameContext, ConfigModelRecord configModel, int levelIndex,
      GameSessionManager sessionManager, String gameFolder) {
    myRoot = new StackPane();
    myGameContext = gameContext;
    GameMapView myGameMapView = new GameMapView(gameContext, configModel, gameFolder);
    myRoot.setPrefSize(GAME_VIEW_WIDTH, GAME_VIEW_HEIGHT);
    myRoot.setMinSize(GAME_VIEW_WIDTH, GAME_VIEW_HEIGHT);
    myRoot.setMaxSize(GAME_VIEW_WIDTH, GAME_VIEW_HEIGHT);
    myRoot.getChildren().add(myGameMapView.getCanvas());
    myRoot.setFocusTraversable(true);
    setBackgroundImage(configModel, levelIndex, gameFolder);
    boolean isFinalLevel = levelIndex >= configModel.levels().size() - 1;
    this.sessionManager = sessionManager;

    myGameLoopController = new GameLoopController(configModel, gameContext,
        myGameMapView,
        configModel.levels().get(sessionManager.getLevelOrder().get(levelIndex)));
    myGameMapView.setGameLoopController(myGameLoopController);
    setUpEndMessage();
    myGameMapView.setEndGameCallback(won -> showEndMessage(won, isFinalLevel));
  }

  // overload constructor for multiplayer game

  private void setBackgroundImage(ConfigModelRecord configModel, int levelIndex,
      String gameFolder) {
    // ChatGPT generated this code.
    Image backgroundImage = getBackgroundImage(configModel, levelIndex, gameFolder);
    if (backgroundImage != null) {
      BackgroundImage bgImage = new BackgroundImage(
          backgroundImage,
          BackgroundRepeat.REPEAT,
          BackgroundRepeat.NO_REPEAT,
          BackgroundPosition.CENTER,
          new BackgroundSize(
              1.0, 1.0,      // width, height as 100%
              true, true,    // treat width and height as percentages
              true,          // preserve aspect ratio
              false          // DO NOT cover area (contain instead)
          )
      );
      myRoot.setBackground(new Background(bgImage));
    }
  }

  private Image getBackgroundImage(ConfigModelRecord configModel, int levelIndex,
      String gameFolder) {
    final Image backgroundImage;
    String backgroundImagePath = configModel.levels().get(levelIndex).mapInfo()
        .backgroundImagePath();
    if (backgroundImagePath == null) {
      return null;
    }
    String imagePath = gameFolder + backgroundImagePath;
    try {
      backgroundImage = new Image(new FileInputStream(imagePath));
    } catch (FileNotFoundException e) {
      LoggingManager.LOGGER.warn("Unable to load background image for level {}", imagePath);
      throw new RuntimeException("Failed to load image from path: " + imagePath, e);
    }
    return backgroundImage;
  }

  /**
   * Return the javafx root element that is modified by this view class.
   *
   * @return A StackPane which is added to by this view class.
   */
  public StackPane getRoot() {
    return myRoot;
  }

  private void setUpEndMessage() {
    endLabel.setId("endLabel");
    configureEndNode(endLabel, "end-label", null);
    StackPane.setAlignment(endLabel, Pos.CENTER);

    nextLevelButton.setId("nextLevelButton");
    configureEndNode(nextLevelButton, END_BUTTON_STYLE, LanguageManager.getMessage("NEXT_LEVEL"));

    resetButton.setId("resetButton");
    configureEndNode(resetButton, END_BUTTON_STYLE, LanguageManager.getMessage("RESET_GAME"));
    saveButton.setFocusTraversable(Boolean.FALSE);
    saveButton.setId("saveButton");
    configureEndNode(saveButton, "save-button", LanguageManager.getMessage("SAVE_GAME"));
    saveButton.setFocusTraversable(Boolean.FALSE);
  }


  private void configureEndNode(Node node, String styleClass, String text) {
    node.setVisible(false);
    node.getStyleClass().add(styleClass);
    if (node instanceof Labeled && text != null) {
      ((Labeled) node).setText(text);
    }
    myRoot.getChildren().add(node);
  }

  private void showEndMessage(boolean gameWon, boolean isFinalLevel) {
    String messageKey = determineEndMessageKey(gameWon, isFinalLevel);
    endLabel.setText(LanguageManager.getMessage(messageKey));

    sessionManager.updateHighScore(myGameContext.gameState().getHighScore());
    configureButtonVisibility(gameWon, isFinalLevel);

    if (gameWon && !isFinalLevel) {
      pendingLevelAdvance = true;
      saveFinalProgress();
    }
  }


  private String determineEndMessageKey(boolean gameWon, boolean isFinalLevel) {
    if (!gameWon) {
      return "GAME_OVER";
    }
    return isFinalLevel ? "GAME_WON" : "LEVEL_PASSED";
  }

  private void configureButtonVisibility(boolean gameWon, boolean isFinalLevel) {
    endLabel.setVisible(true);
    nextLevelButton.setVisible(gameWon && !isFinalLevel);
    resetButton.setVisible(!gameWon || isFinalLevel);
    saveButton.setVisible(gameWon && !isFinalLevel);
  }

  /**
   * Sets the action to be executed when the next level button is clicked.
   *
   * @param action a {@code Runnable} representing the restart behavior
   */
  public void setNextLevelAction(Runnable action) {
    nextLevelButton.setOnAction(e -> action.run());
  }

  /**
   * Sets the action to be executed when the save button is clicked.
   *
   * @param action a {@code Runnable} representing the restart behavior
   */
  public void setSaveAction(Runnable action) {
    try {
      saveButton.setOnAction(e -> action.run());
    } catch (Exception e) {
      LoggingManager.LOGGER.warn("Unable to save game", e);
    }
  }

  /**
   * Returns if the level is pending advance
   */
  public boolean isPendingLevelAdvance() {
    return pendingLevelAdvance;
  }

  /**
   * Saves progress whenever game ends
   */
  private void saveFinalProgress() {
    try {
      sessionManager.save();
      LoggingManager.LOGGER.info("Final progress saved after game over or game win!");
    } catch (Exception e) {
      LoggingManager.LOGGER.warn("Failed to save final progress: {}", e.getMessage());
    }
  }

  /**
   * Sets the action to be executed when the reset button is clicked.
   *
   * <p>This allows external components (such as {@link GamePlayerView}) to define what
   * should happen when the player chooses to restart the current level. The provided
   * {@link Runnable} will be invoked when the restart button is activated.</p>
   *
   * @param action a {@code Runnable} representing the restart behavior
   */
  public void setResetAction(Runnable action) {
    resetButton.setOnAction(e -> action.run());
  }

  /**
   * Pause the game loop from the game loop controller associated with this game view.
   */
  public void pauseGame() {
    myGameLoopController.pauseGame();
  }

  /**
   * Resume the game loop from the game loop controller associated with this game view.
   */
  public void resumeGame() {
    myGameLoopController.resumeGame();
  }

  /**
   * Resets all player-controlled entities back to their initial spawn positions and zeroes their
   * velocity.
   */
  public void resetControlledEntitiesToSpawn() {
    for (Entity entity : myGameContext.gameMap()) {
      entity.getEntityPlacement().moveTo(
          entity.getEntityPlacement().getInitialX(),
          entity.getEntityPlacement().getInitialY()
      );
      entity.setDx(0);
      entity.setDy(0);
    }
  }


}
