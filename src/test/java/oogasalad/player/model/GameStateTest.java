package oogasalad.player.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import oogasalad.engine.records.config.model.SettingsRecord;
import oogasalad.engine.records.config.model.losecondition.LivesBasedConditionRecord;
import oogasalad.engine.records.config.model.wincondition.EntityBasedConditionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameStateTest {

  private GameState gameState;

  @BeforeEach
  void setUp() {
    SettingsRecord gameSettings = new SettingsRecord(1.0, 5, 5,
        new EntityBasedConditionRecord("dot"), new LivesBasedConditionRecord(), new HashSet<>(), false);
    gameState = new GameState(gameSettings);
  }

  @Test
  void updateScore_addSubScore_updatesScoreCorrectly() {
    gameState.updateScore(10);
    assertEquals(15, gameState.getScore());

    gameState.updateScore(-3);
    assertEquals(12, gameState.getScore());
  }

  @Test
  void updateLives_addSubLives_updatesLivesCorrectly() {
    gameState.updateLives(-1);
    assertEquals(4, gameState.getLives());

    gameState.updateLives(2);
    assertEquals(6, gameState.getLives());
  }
}
