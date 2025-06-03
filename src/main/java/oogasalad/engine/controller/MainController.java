package oogasalad.engine.controller;

import java.util.Set;
import javafx.scene.Group;
import javafx.stage.Stage;
import oogasalad.authoring.controller.AuthoringController;
import oogasalad.authoring.model.AuthoringModel;
import oogasalad.authoring.view.mainView.AuthoringView;
import oogasalad.engine.utility.LoggingManager;
import oogasalad.engine.view.SplashScreenView;
import oogasalad.player.controller.GameInputManager;
import oogasalad.player.model.GameState;
import oogasalad.player.model.api.GameStateFactory;
import oogasalad.player.view.GameScreenView;
import oogasalad.player.view.GameSelectorView;
import oogasalad.player.view.NetworkedGameLobbyView;

/**
 * The main controller of the game engine. This class controls the interactions between the model
 * and view and transitions between views for the game engine.
 *
 * @author Owen Jennings
 */
public class MainController {

  private final Group myRoot;
  private final Stage myStage;
  private final GameInputManager myInputManager;
  private SplashScreenView mySplashScreenView = null;
  private AuthoringView myAuthoringView = null;
  private GameSelectorView myGameSelectorView = null;
  private NetworkedGameLobbyView myNetworkedGameLobbyView = null;

  /**
   * Create a main controller for the program.
   *
   * @param stage The stage of the main application.
   * @param root  The root element of the main application.
   */
  public MainController(Stage stage, Group root) {
    myRoot = root;
    myStage = stage;
    myInputManager = new GameInputManager(stage.getScene(), myRoot);
    showSplashScreen();
  }

  /**
   * Show the splash screen view, if it is not already being displayed.
   */
  public void showSplashScreen() {
    if (mySplashScreenView == null) {
      mySplashScreenView = new SplashScreenView(this);
    }
    if (!myRoot.getChildren().contains(mySplashScreenView.getRoot())) {
      myRoot.getChildren().add(mySplashScreenView.getRoot());
    }
  }

  /**
   * Hide the splash screen view.
   */
  public void hideSplashScreen() {
    if (myRoot.getChildren().contains(mySplashScreenView.getRoot())) {
      myRoot.getChildren().remove(mySplashScreenView.getRoot());
    } else {
      LoggingManager.LOGGER.warn(
          "Attempted to hide the splash screen, even though it wasn't being displayed.");
    }
  }

  /**
   * Show the game selector view if it is not already being displayed.
   */
  public void showGameSelectorView() {
    if (myGameSelectorView == null) {
      myGameSelectorView = new GameSelectorView(this);
    }
    if (!myRoot.getChildren().contains(myGameSelectorView.getRoot())) {
      myGameSelectorView.resetUploadSection();
      myRoot.getChildren().add(myGameSelectorView.getRoot());
    }
  }

  /**
   * Hide the game selector view.
   */
  public void hideGameSelectorView() {
    if (myRoot.getChildren().contains(myGameSelectorView.getRoot())) {
      myRoot.getChildren().remove(myGameSelectorView.getRoot());
    } else {
      LoggingManager.LOGGER.warn(
          "Attempted to hide the game selector screen, even though it wasn't being displayed.");
    }
    myGameSelectorView = null;
  }

  /**
   * Show the game player view if it is not already being displayed.
   *
   * @param gameFolderName name of game folder to create
   * @param randomized     if levels should be randomized
   * @return true if the game player was successfully loaded and false if the view could not be
   * loaded
   */
  public boolean showGamePlayerView(String gameFolderName, boolean randomized) {
    return showGamePlayerViewInternal(gameFolderName, randomized, null, null);
  }

  /**
   * Show the game player view for a networked game if it is not already being displayed.
   *
   * @param gameFolderName name of game folder to create
   * @param randomized     if levels should be randomized
   * @param playerId       client's player id
   * @param activePlayerIds set of active player ids
   * @return true if the game player was successfully loaded and false if the view could not be
   * loaded
   */
  public boolean showGamePlayerView(String gameFolderName, boolean randomized, int playerId,
      Set<Integer> activePlayerIds) {
    return showGamePlayerViewInternal(gameFolderName, randomized, playerId, activePlayerIds);
  }

  private boolean showGamePlayerViewInternal(String gameFolderName, boolean randomized,
      Integer playerId, Set<Integer> activePlayerIds) {
    try {
      GameState myGameState = GameStateFactory.createFromConfig(gameFolderName);
      GameScreenView myGameScreenView;

      if (playerId != null && activePlayerIds != null) {
        myGameScreenView = new GameScreenView(this, myGameState, gameFolderName,
            randomized, playerId, activePlayerIds);
      } else {
        myGameScreenView = new GameScreenView(this, myGameState, gameFolderName, randomized);
      }

      myInputManager.getRoot().getChildren().add(myGameScreenView.getRoot());
      myInputManager.setGameScreenView(myGameScreenView);
    } catch (Exception e) {
      LoggingManager.LOGGER.warn("Unable to load game player view for folder: {}", gameFolderName);
      LoggingManager.LOGGER.warn(e.getMessage());
      return false;
    }
    return true;
  }

  /**
   * Show the authoring environment view if not already displayed.
   */
  public void showAuthoringView() {
    if (myAuthoringView == null) {
      AuthoringModel model = new AuthoringModel();
      myAuthoringView = new AuthoringView();
      AuthoringController controller = new AuthoringController(model, myAuthoringView, this);
      myAuthoringView.setController(controller);
    }
    if (!myInputManager.getRoot().getChildren().contains(myAuthoringView.getNode())) {
      myInputManager.getRoot().getChildren().add(myAuthoringView.getNode());
    }
  }

  /**
   * Hide the authoring view if it is currently being displayed.
   */
  public void hideAuthoringView() {
    if (myRoot.getChildren().contains(myAuthoringView.getNode())) {
      myRoot.getChildren().remove(myAuthoringView.getNode());
      myAuthoringView = null;
    } else {
      LoggingManager.LOGGER.warn(
          "Attempted to hide the authoring environment, even though it wasn't being displayed.");
    }
  }

  /**
   * Show the networked game lobby view if not already displayed.
   */
  public void showNetworkedGameLobbyView(String path) {
    if (myNetworkedGameLobbyView == null) {
      myNetworkedGameLobbyView = new NetworkedGameLobbyView(this, path);
    }
    if (!myRoot.getChildren().contains(myNetworkedGameLobbyView.getRoot())) {
      myRoot.getChildren().add(myNetworkedGameLobbyView.getRoot());
    }
  }

  /**
   * Hide the networked game lobby view.
   */
  public void hideNetworkedLobbyView() {
    if (myRoot.getChildren().contains(myNetworkedGameLobbyView.getRoot())) {
      myRoot.getChildren().remove(myNetworkedGameLobbyView.getRoot());
    } else {
      LoggingManager.LOGGER.warn(
          "Attempted to hide the networked game lobby screen, even though it wasn't being displayed.");
    }
  }

  /**
   * Get the main stage of this controller.
   *
   * @return The stage associated with this controller.
   */
  public Stage getStage() {
    return myStage;
  }

  /**
   * Get the input manager initialized in MainController.
   */
  public GameInputManager getInputManager() {
    return myInputManager;
  }
}
