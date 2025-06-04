package oogasalad.player.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import oogasalad.engine.config.EntityPlacement;
import oogasalad.engine.records.config.ConfigModelRecord;
import oogasalad.engine.records.config.ModeConfigRecord;
import oogasalad.engine.records.config.model.controlConfig.ControlConfigInterface;
import oogasalad.engine.records.model.EntityTypeRecord;
import oogasalad.engine.utility.constants.Directions.Direction;
import oogasalad.player.controller.GameInputManager;
import oogasalad.player.model.api.ControlStrategyFactory;
import oogasalad.player.model.strategies.control.ControlStrategyInterface;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class EntityTest {

  private EntityPlacement mockPlacement;
  private ModeConfigRecord mockMode;
  private Entity entity;
  private MockedStatic<ControlStrategyFactory> controlStrategyFactoryMock;


  @BeforeEach
  void setUp() {
    mockPlacement = mock(EntityPlacement.class);
    GameInputManager mockInput = mock(GameInputManager.class);
    GameMapInterface mockMap = mock(GameMapInterface.class);

    // MOCK STATIC METHOD
    controlStrategyFactoryMock = mockStatic(ControlStrategyFactory.class);
    controlStrategyFactoryMock
        .when(() -> ControlStrategyFactory.createControlStrategy(any(), any(), any(), any()))
        .thenReturn(mock(ControlStrategyInterface.class));

    // Create dummy mode
    Map<String, ModeConfigRecord> modes = new HashMap<>();
    ModeConfigRecord newMode = new ModeConfigRecord("Default", null,
        mock(ControlConfigInterface.class), null, null);
    modes.put("Default", newMode);

    when(mockPlacement.getType()).thenReturn(new EntityTypeRecord("test", modes, null));
    when(mockPlacement.getMode()).thenReturn("Default");

    entity = new Entity(mockInput, mockPlacement, mockMap, mock(ConfigModelRecord.class), null);
  }

  @AfterEach
  void tearDown() {
    // VERY IMPORTANT: Close the static mock after each test!
    controlStrategyFactoryMock.close();
  }


  @Test
  void getEntityPlacement_validEntity_returnsEntity() {
    assertEquals(mockPlacement, entity.getEntityPlacement());
  }

  @Test
  void setEntityDirection_validDirection_updatesDirection() {
    entity.setEntityDirection(Direction.R);
    assertEquals(Direction.R, entity.getEntityDirection());
    assertEquals(Direction.R.getDx() * entity.getSpeed(), entity.getDx());
    assertEquals(Direction.R.getDy() * entity.getSpeed(), entity.getDy());
  }

  @Test
  void setEntityDirection_horizontalToVertical_snapsX() {
    when(mockPlacement.getX()).thenReturn(2.18);
    doNothing().when(mockPlacement).setX(anyDouble());

    entity.setEntityDirection(Direction.L); // Initial direction
    entity.setEntitySnapDirection(Direction.U); // Change to vertical

    verify(mockPlacement).setX(2.0); // Snaps to whole number
    assertEquals(Direction.U, entity.getEntityDirection());
  }

  @Test
  void setEntityDirection_verticalToHorizontal_snapsY() {
    when(mockPlacement.getY()).thenReturn(5.19);
    doNothing().when(mockPlacement).setY(anyDouble());

    entity.setEntityDirection(Direction.U); // Initial direction
    entity.setEntitySnapDirection(Direction.R); // Change to horizontal

    verify(mockPlacement).setY(5.0);
    assertEquals(Direction.R, entity.getEntityDirection());
  }

  @Test
  void setEntityDirection_notWithinSnap_noSnap() {
    when(mockPlacement.getX()).thenReturn(2.5); // Not within snapping range
    entity.setEntityDirection(Direction.L);
    entity.setEntitySnapDirection(Direction.U); // Should NOT snap
    verify(mockPlacement, never()).setX(anyDouble());
  }

  @Test
  void setDxAndGetDx_setsDx_setsAndReturnsDx() {
    entity.setDx(1.23);
    assertEquals(1.23, entity.getDx());
  }

  @Test
  void setDxAndGetDy_setsDy_setsAndReturnsDy() {
    entity.setDy(4.56);
    assertEquals(4.56, entity.getDy());
  }

  @Test
  void testCanMove_validAndInvalidMoveHorizontal_returnsTrueThenFalse() {
    when(mockPlacement.getType()).thenReturn(new EntityTypeRecord("test", null, null));
    when(mockPlacement.getY()).thenReturn(4.00005);
    assertTrue(entity.canMove(Direction.R));
    when(mockPlacement.getY()).thenReturn(4.5);
    assertFalse(entity.canMove(Direction.R));
  }

  @Test
  void testCanMove_validAndInvalidMoveVertical_returnsTrueThenFalse() {
    when(mockPlacement.getX()).thenReturn(2.1);
    assertTrue(entity.canMove(Direction.D));
    when(mockPlacement.getX()).thenReturn(2.5);
    assertFalse(entity.canMove(Direction.D));
  }
}