package oogasalad.player.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oogasalad.engine.config.EntityPlacement;
import oogasalad.engine.records.GameContextRecord;
import oogasalad.engine.records.config.ConfigModelRecord;
import oogasalad.engine.records.config.ImageConfigRecord;
import oogasalad.engine.records.config.ModeConfigRecord;
import oogasalad.engine.records.config.model.controlConfig.ControlConfigInterface;
import oogasalad.engine.records.config.model.controlConfig.KeyboardControlConfigRecord;
import oogasalad.engine.records.config.model.losecondition.LivesBasedConditionRecord;
import oogasalad.engine.records.config.model.wincondition.EntityBasedConditionRecord;
import oogasalad.engine.records.model.EntityTypeRecord;
import oogasalad.player.model.Entity;
import oogasalad.player.model.GameMapInterface;
import oogasalad.player.model.GameStateInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameMapControllerTest {

  private GameMapInterface mockGameMap;
  private GameStateInterface mockGameState;
  private GameMapController controller;

  @BeforeEach
  public void setUp() {
    mockGameMap = mock(GameMapInterface.class);
    mockGameState = mock(GameStateInterface.class);
    GameInputManager mockInputManager = mock(GameInputManager.class);
    GameContextRecord gameContext = new GameContextRecord(mockInputManager, mockGameMap,
        mockGameState);

    ConfigModelRecord mockConfigModel = mock(ConfigModelRecord.class);
    when(mockConfigModel.winCondition()).thenReturn(
        new EntityBasedConditionRecord("dot")); // or any WinCondition
    when(mockConfigModel.loseCondition()).thenReturn(new LivesBasedConditionRecord());

    controller = new GameMapController(gameContext, mockConfigModel);
  }

  @Test
  public void updateEntityModels_setVelocityForEntity_entityPositionUpdates() throws Exception {
    Map<String, ModeConfigRecord> modes = new HashMap<>();
    ControlConfigInterface mockControl = new KeyboardControlConfigRecord(null);
    ImageConfigRecord image = new ImageConfigRecord("sldha", 1, 1, 1, 1.0);
    ModeConfigRecord newMode = new ModeConfigRecord("Default", null, mockControl, image, null);
    modes.put("Default", newMode);
    EntityTypeRecord data = new EntityTypeRecord("test", modes, new ArrayList<String>());
    EntityPlacement placement = new EntityPlacement(data, 5, 5, "Default");
    GameInputManager mockInputManager = mock(GameInputManager.class);
    Entity entity = new Entity(mockInputManager, placement, mockGameMap,
        mock(ConfigModelRecord.class));
    entity.setDx(1);
    entity.setDy(-1);

    List<Entity> entities = List.of(entity);

    doReturn(entities.iterator()).when(mockGameMap).iterator();
    when(mockGameState.getScore()).thenReturn(0);

    controller.updateEntityModels();

    assertEquals(6, entity.getEntityPlacement().getX());
    assertEquals(4, entity.getEntityPlacement().getY());
  }
}

