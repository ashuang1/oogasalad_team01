package oogasalad.player.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import javafx.scene.Group;
import javafx.scene.Scene;
import oogasalad.engine.records.GameContextRecord;
import oogasalad.engine.records.config.ConfigModelRecord;
import oogasalad.engine.records.config.model.ParsedLevelRecord;
import oogasalad.engine.records.config.model.SettingsRecord;
import oogasalad.engine.records.config.model.losecondition.LivesBasedConditionRecord;
import oogasalad.engine.records.config.model.wincondition.EntityBasedConditionRecord;
import oogasalad.player.model.GameMap;
import oogasalad.player.model.GameMapInterface;
import oogasalad.player.model.GameState;
import oogasalad.player.view.GameMapView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import util.DukeApplicationTest;

@ExtendWith(MockitoExtension.class)
class GameLoopControllerTest extends DukeApplicationTest {

  // I used ChatGPT to assist in writing the test methods below.
  private GameLoopController gameLoopController;
  private GameMapView gameMapView;
  private GameMapInterface gameMap;

  @BeforeEach
  void setUp() {
    gameMap = Mockito.spy(new GameMap(10, 10));

    SettingsRecord gameSettings = new SettingsRecord(1.0, 5, 5,
        new EntityBasedConditionRecord("dot"), new LivesBasedConditionRecord(), new HashSet<>(), false);
    GameState gameState = new GameState(gameSettings);
    GameContextRecord gameContext = new GameContextRecord(
        Mockito.spy(new GameInputManager(mock(Scene.class), mock(Group.class))), gameMap,
        gameState);

    ConfigModelRecord mockConfigModel = mock(ConfigModelRecord.class);
    when(mockConfigModel.winCondition()).thenReturn(new EntityBasedConditionRecord("dot"));
    when(mockConfigModel.loseCondition()).thenReturn(new LivesBasedConditionRecord());
    when(mockConfigModel.settings()).thenReturn(
        new SettingsRecord(1.0, 1, 1, null, null, new HashSet<>(), false));

    gameMapView = Mockito.spy(
        new GameMapView(gameContext, mockConfigModel, "data/games/BasicPacMan/"));
    gameLoopController = Mockito.spy(
        new GameLoopController(mockConfigModel, gameContext, gameMapView, mock(
            ParsedLevelRecord.class)));
  }

  @Test
  void pauseGame_WhenCalledTwice_DoesNotThrowException() {
    gameLoopController.pauseGame();
    assertDoesNotThrow(() -> gameLoopController.pauseGame());
  }

  @Test
  void pauseGame_EnsureNoUpdateCallsAfterPause_Success() throws InterruptedException {
    // Pause the game FIRST
    runAsJFXAction(() -> gameLoopController.pauseGame());

    // Clear invocations AFTER ensuring pause
    clearInvocations(gameMap, gameMapView);

    // Wait to ensure no updates happen after pause
    Thread.sleep(150);

    // Now verify that no updates happened after pausing
    verify(gameMap, times(0)).update();
    verify(gameMapView, times(0)).update();
  }


  @Test
  void resumeGame_AfterPause_DoesNotThrowException() {
    gameLoopController.pauseGame();
    assertDoesNotThrow(() -> gameLoopController.resumeGame());
  }

  @Test
  void gameLoop_AfterShortDelay_UpdatesModelAndView() throws InterruptedException {
    Thread.sleep(100);
    verify(gameMap, atLeastOnce()).update();
    verify(gameMapView, atLeastOnce()).update();
  }

  @Test
  void gameSpeedMultiplier_GetAndSet_Success() {
    double newSpeed = 2.0;
    gameLoopController.setGameSpeedMultiplier(newSpeed);
    assertEquals(newSpeed, gameLoopController.getGameSpeedMultiplier());
  }


  @Test
  void resumeGame_UpdatesAfterResume_Success() throws InterruptedException {
    // First pause the game
    gameLoopController.pauseGame();

    // Clear any previous invocations
    clearInvocations(gameMap, gameMapView);

    // Resume the game
    gameLoopController.resumeGame();

    // Wait a bit to allow for updates
    Thread.sleep(100);

    // Verify updates occurred after resume
    verify(gameMap, atLeastOnce()).update();
    verify(gameMapView, atLeastOnce()).update();
  }

  @Test
  void pauseAndResume_MultipleTimesInSequence_Success() {
    // Test multiple pause/resume cycles
    assertDoesNotThrow(() -> {
      for (int i = 0; i < 3; i++) {
        gameLoopController.pauseGame();
        gameLoopController.resumeGame();
      }
    });
  }

  @Test
  void gameLoop_VerifyUpdateFrequency_Success() throws InterruptedException {
    // Clear any previous invocations
    clearInvocations(gameMap, gameMapView);

    // Wait for multiple update cycles
    Thread.sleep(300);

    // Verify that multiple updates occurred
    verify(gameMap, atLeast(2)).update();
    verify(gameMapView, atLeast(2)).update();
  }

  @Test
  void pauseGame_VerifyGameStatePreserved_Success() {
    // Set initial game speed
    double initialSpeed = 1.5;
    gameLoopController.setGameSpeedMultiplier(initialSpeed);

    // Pause game
    gameLoopController.pauseGame();

    // Verify game speed remains unchanged after pause
    assertEquals(initialSpeed, gameLoopController.getGameSpeedMultiplier());
  }

  @Test
  void resumeGame_WithoutPriorPause_DoesNotThrowException() {
    assertDoesNotThrow(() -> gameLoopController.resumeGame());
  }

}
