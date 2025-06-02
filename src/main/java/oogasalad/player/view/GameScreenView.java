package oogasalad.player.view;

import static oogasalad.engine.utility.constants.GameConfig.HEIGHT;
import static oogasalad.engine.utility.constants.GameConfig.WIDTH;

import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import oogasalad.engine.controller.MainController;
import oogasalad.player.model.GameStateInterface;
import oogasalad.player.model.save.GameSessionManager;
import oogasalad.player.view.components.HudView;

/**
 * A view that displays the Heads-up display (HUD) and the game player view.
 *
 * @author Owen Jennings, Troy Ludwig
 */
public class GameScreenView {

  private final VBox myRoot;
  private final MainController mainController;
  private final GameStateInterface gameState;
  private final GamePlayerView myGamePlayerView;
  private final HudView hudView;
  private int lastScore;
  private int lastLives;

  /**
   * Create a game screen view.
   *
   * @param controller     The main controller for the player view.
   * @param gameState      The game state object for this current game.
   * @param gameFolderPath name of game folder to create game from.
   * @param randomized     if levels should be randomized
   */
  public GameScreenView(MainController controller, GameStateInterface gameState,
      String gameFolderPath, boolean randomized) {
    super();
    myRoot = new VBox();
    this.gameState = gameState;
    this.mainController = controller;

    myGamePlayerView = new GamePlayerView(controller, gameState, gameFolderPath,
        randomized);
    GameView gameView = myGamePlayerView.getGameView();

    hudView = new HudView(
        gameState,
        gameView,
        this::handleReturnToMainMenu
    );

    myRoot.getChildren().addAll(hudView, myGamePlayerView.getPane());
    myRoot.getStyleClass().add("root");
    myRoot.setPrefSize(WIDTH, HEIGHT);

    // Store initial values
    lastScore = gameState.getScore();
    lastLives = gameState.getLives();

    // Timeline to check for changes every 100ms
    Timeline hudUpdater = new Timeline(
        new KeyFrame(Duration.millis(100), event -> checkAndUpdateHud())
    );
    hudUpdater.setCycleCount(Timeline.INDEFINITE);
    hudUpdater.play();
  }

  /**
   * Create a game screen view for networked games.
   *
   * @param controller     The main controller for the player view.
   * @param gameState      The game state object for this current game.
   * @param gameFolderPath name of game folder to create game from.
   * @param randomized     if levels should be randomized
   * @param myPlayerId     client's player id
   * @param activePlayerIds set of active player ids from server
   */
  public GameScreenView(MainController controller, GameStateInterface gameState,
      String gameFolderPath, boolean randomized, int myPlayerId, Set<Integer> activePlayerIds) {
    this(controller, gameState, gameFolderPath, randomized);
    myGamePlayerView.setPlayerContext(myPlayerId, activePlayerIds);
  }

  /**
   * Get the root VBox for this view.
   *
   * @return A JavaFX VBox to display information from this view.
   */
  public VBox getRoot() {
    return myRoot;
  }

  private void handleReturnToMainMenu() {
    mainController.getInputManager().getRoot().getChildren().remove(myRoot);
    mainController.showSplashScreen();
    GameSessionManager gameSessionManager = myGamePlayerView.getGameSessionManager();
    gameSessionManager.updateHighScore(gameState.getHighScore());
    gameSessionManager.save();
  }


  /**
   * Checks if score or lives have changed before updating HUD.
   */
  private void checkAndUpdateHud() {
    if (gameState.getScore() != lastScore || gameState.getLives() != lastLives) {
      hudView.update(gameState);
      lastScore = gameState.getScore();
      lastLives = gameState.getLives();
    }
  }

  /**
   * Gets GamePlayerView for purposes of updating level and resetting game
   */
  public GamePlayerView getGamePlayerView() {
    return myGamePlayerView;
  }


}
