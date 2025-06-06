package oogasalad.engine.records;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import oogasalad.engine.records.CollisionContextRecord.StrategyAppliesTo;
import oogasalad.player.model.Entity;
import oogasalad.player.model.GameMapInterface;
import oogasalad.player.model.GameStateInterface;
import org.junit.jupiter.api.Test;

class CollisionContextTest {

  private final Entity entity1 = mock(Entity.class);
  private final Entity entity2 = mock(Entity.class);
  private final GameMapInterface gameMap = mock(GameMapInterface.class);
  private final GameStateInterface gameState = mock(GameStateInterface.class);

  @Test
  void collisionContext_validInputs_createsCollisionContextSuccessfully() {
    CollisionContextRecord context = new CollisionContextRecord(entity1, entity2, gameMap,
        gameState, StrategyAppliesTo.ENTITY1);

    assertEquals(entity1, context.entity1());
    assertEquals(entity2, context.entity2());
    assertEquals(gameMap, context.gameMap());
    assertEquals(gameState, context.gameState());
  }

  // theres probably a way to do this without doing one for each one

  @Test
  void collisionContext_nullEntity1_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () ->
        new CollisionContextRecord(null, entity2, gameMap, gameState, StrategyAppliesTo.ENTITY1));
  }

  @Test
  void collisionContext_nullEntity2_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () ->
        new CollisionContextRecord(entity1, null, gameMap, gameState, StrategyAppliesTo.ENTITY1));
  }

  @Test
  void collisionContext_nullGameMap_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () ->
        new CollisionContextRecord(entity1, entity2, null, gameState, StrategyAppliesTo.ENTITY1));
  }

  @Test
  void collisionContext_nullGameState_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () ->
        new CollisionContextRecord(entity1, entity2, gameMap, null, StrategyAppliesTo.ENTITY1));
  }
}
