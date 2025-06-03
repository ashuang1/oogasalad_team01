package oogasalad.player.model;

import oogasalad.engine.records.config.model.SettingsRecord;

/**
 * Implementation of the GameState interface. This class manages the player's score, lives, HUD
 * components, and if the game is multiplayer. It also provides functionality for saving and loading
 * game states.
 *
 * @author Troy Ludwig
 */
public class GameState implements GameStateInterface {

  private int score;
  private int highScore;
  private int lives;
  private final int startingLives;
  private final int initialScore;
  private double timeElapsed = 0;
  private final boolean isMultiplayer;

  /**
   * Loads in game settings given the game setting record.
   *
   * @param gameSettings contains default settings for given game.
   */
  public GameState(SettingsRecord gameSettings) {
    this.startingLives = gameSettings.startingLives();
    this.initialScore = gameSettings.initialScore();
    this.score = initialScore;
    this.lives = startingLives;
    this.highScore = 0;
    this.isMultiplayer = gameSettings.isMultiplayer();
  }

  @Override
  public void updateScore(int delta) {
    this.score += delta;
    if (this.score > highScore) {
      highScore = score;
    }
  }

  @Override
  public int getScore() {
    return score;
  }

  @Override
  public void updateLives(int delta) {
    this.lives += delta;
  }

  @Override
  public int getLives() {
    return lives;
  }

  @Override
  public void resetState() {
    this.score = initialScore;
    this.lives = startingLives;
    this.timeElapsed = 0;
  }

  @Override
  public double getTimeElapsed() {
    return timeElapsed;
  }

  @Override
  public void setTimeElapsed(double timeElapsed) {
    this.timeElapsed = timeElapsed;
  }

  @Override
  public void resetTimeElapsed() {
    this.timeElapsed = 0;
  }

  @Override
  public int getHighScore() {
    return this.highScore;
  }

  @Override
  public void updateHighScore(int highScore) {
    if (this.highScore < highScore) {
      this.highScore = highScore;
    }
  }

  @Override
  public void setLives(int lives) {
    this.lives = lives;
  }

  @Override
  public boolean getIsMultiplayer() {
    return isMultiplayer;
  }
}
