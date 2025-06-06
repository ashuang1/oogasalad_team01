package oogasalad.player.model.strategies.collision;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import oogasalad.engine.records.CollisionContextRecord;
import oogasalad.player.model.GameStateInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateLivesStrategyTest {

  private GameStateInterface gameState;
  private CollisionContextRecord context;

  @BeforeEach
  void setUp() {
    gameState = mock(GameStateInterface.class);
    context = mock(CollisionContextRecord.class);

    when(context.gameState()).thenReturn(gameState);
  }

  // TODO: I'm not sure what else to test about this?

  @Test
  void handleCollision_positiveIncrement_increasesLives() {
    UpdateLivesStrategy strategy = new UpdateLivesStrategy(2);

    strategy.handleCollision(context);

    verify(gameState).updateLives(2);
  }

}
