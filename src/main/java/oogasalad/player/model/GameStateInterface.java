package oogasalad.player.model;

import java.util.List;

/**
 * The {@code GameState} interface defines methods to manage HUD elements and game metadata like
 * score, lives, and serialization functionality for pause/save/resume capabilities. It is used to
 * keep track of dynamic values and HUD components throughout the game.
 *
 * @author Luke Fu
 */
public interface GameStateInterface {

  /**
   * Updates the player's score by the specified amount.
   *
   * @param delta the number of points to add (can be negative to subtract points).
   */
  void updateScore(int delta);

  /**
   * Retrieves the current score of the player.
   *
   * @return the current score.
   */
  int getScore();

  /**
   * Updates the number of remaining lives for the player.
   *
   * @param delta the number of lives to add (can be negative to subtract lives).
   */
  void updateLives(int delta);

  /**
   * Retrieves the current number of lives.
   *
   * @return the number of remaining lives.
   */
  int getLives();

  /**
   * Update game over to reflect game status.
   *
   * @param gameOver status of game over.
   */
  void setGameOver(boolean gameOver);

  /**
   * Get the time elapsed in the game.
   *
   * @return A double representing time elapsed since the last reset.
   */
  double getTimeElapsed();

  /**
   * Set the amount of time that has elapsed so far in the game.
   *
   * @param timeElapsed The amount of time that has elapsed.
   */
  void setTimeElapsed(double timeElapsed);

  /**
   * Reset the time elapsed counter.
   */
  void resetTimeElapsed();

  /**
   * Retrieves status of if game is over.
   *
   * @return if game is over.
   */
  boolean isGameOver();

  /**
   * Saves the current game state to a file.
   *
   * @param filePath the path to the file where the game state should be saved.
   * @throws SaveFailedException if the game state could not be saved.
   */
  void saveState(String filePath) throws SaveFailedException;

  /**
   * Loads a saved game state from a file.
   *
   * @param filePath the path to the file to load the game state from.
   * @throws LoadFailedException if the game state could not be loaded.
   */
  void loadState(String filePath) throws LoadFailedException;

  /**
   * Registers a new HUD component (e.g., score box, pause button, level display).
   *
   * @param component the HUDComponent to be registered.
   */
  void addHudComponent(HudComponent component);

  /**
   * Returns all currently registered HUD components that should be displayed.
   *
   * @return list of HUD components active in the HUD.
   */
  List<HudComponent> getHudComponents();

  /**
   * Resets the game state to its initial configuration, including score, lives, and registered
   * HUD.
   */
  void resetState();

  /*
   * Supporting types for GameState API
   */

  /**
   * Marker or functional interface representing a HUD element.
   */
  interface HudComponent {
    // Extendable interface for things like ScoreBox, PauseButton, etc.
  }

  /**
   * Exception thrown when saving the game state fails.
   */
  class SaveFailedException extends Exception {

    /**
     * Throws exception with corresponding message.
     */
    public SaveFailedException(String message) {
      super(message);
    }
  }

  /**
   * Exception thrown when loading the game state fails.
   */
  class LoadFailedException extends Exception {

    /**
     * Loads failed exception with corresponding message.
     */
    public LoadFailedException(String message) {
      super(message);
    }

  }
}
