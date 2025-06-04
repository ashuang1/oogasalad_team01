package oogasalad.player.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import oogasalad.engine.config.EntityPlacement;
import oogasalad.engine.exceptions.InvalidPositionException;
import oogasalad.engine.records.MultiplayerContextRecord;
import oogasalad.engine.records.config.ConfigModelRecord;
import oogasalad.engine.records.config.model.ParsedLevelRecord;
import oogasalad.engine.records.config.model.SettingsRecord;
import oogasalad.engine.records.model.MapInfoRecord;
import oogasalad.player.controller.GameInputManager;
import oogasalad.player.model.Entity;
import oogasalad.player.model.GameMapInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class GameMapFactoryTest {

  private ConfigModelRecord configModel;
  private GameInputManager inputManager;
  private EntityPlacement entityPlacement1;
  private EntityPlacement entityPlacement2;
  private Entity entity1;
  private Entity entity2;
  private SettingsRecord settings;

  @BeforeEach
  void setUp() {
    configModel = mock(ConfigModelRecord.class);
    inputManager = mock(GameInputManager.class);
    entityPlacement1 = mock(EntityPlacement.class);
    entityPlacement2 = mock(EntityPlacement.class);
    entity1 = mock(Entity.class);
    entity2 = mock(Entity.class);
    settings = mock(SettingsRecord.class);
  }

  @Test
  void createGameMap_validConfig_createsMapSuccessfully() throws InvalidPositionException {
    when(configModel.settings()).thenReturn(settings);

    when(entity1.getEntityPlacement()).thenReturn(entityPlacement1);
    when(entity2.getEntityPlacement()).thenReturn(entityPlacement2);

    when(entityPlacement1.getX()).thenReturn(1.0);
    when(entityPlacement1.getY()).thenReturn(1.0);
    when(entityPlacement2.getX()).thenReturn(2.0);
    when(entityPlacement2.getY()).thenReturn(2.0);

    // Mock level and placements
    List<ParsedLevelRecord> levels = new ArrayList<>();
    levels.add(
        new ParsedLevelRecord(List.of(entityPlacement1, entityPlacement2),
            new MapInfoRecord("test", 10, 10),
            null, null));
    when(configModel.levels()).thenReturn(levels);

    // ChatGPT on how to mock entity factory correctly
    try (MockedStatic<EntityFactory> mockedFactory = mockStatic(EntityFactory.class)) {
      mockedFactory.when(() -> EntityFactory.createEntity(eq(inputManager), eq(entityPlacement1),
              any(GameMapInterface.class), any(ConfigModelRecord.class), any()))
          .thenReturn(entity1);
      mockedFactory.when(() -> EntityFactory.createEntity(eq(inputManager), eq(entityPlacement2),
              any(GameMapInterface.class), any(ConfigModelRecord.class),
              any()))
          .thenReturn(entity2);

      GameMapInterface gameMap = GameMapFactory.createGameMap(inputManager, configModel, 0,
          new MultiplayerContextRecord(-1, null, null));

      assertNotNull(gameMap);
      assertTrue(gameMap.getEntityAt(1, 1).isPresent());
      assertTrue(gameMap.getEntityAt(2, 2).isPresent());
      assertEquals(gameMap.getEntityAt(1, 1).get(), entity1);
      assertEquals(gameMap.getEntityAt(2, 2).get(), entity2);
    }
  }

  @Test
  void createGameMap_invalidConfig_throwsException() {
    when(configModel.settings()).thenReturn(settings);

    when(entity1.getEntityPlacement()).thenReturn(entityPlacement1);
    when(entity2.getEntityPlacement()).thenReturn(entityPlacement2);

    when(entityPlacement1.getX()).thenReturn(-1.0);
    when(entityPlacement1.getY()).thenReturn(1.0);
    when(entityPlacement2.getX()).thenReturn(2.0);
    when(entityPlacement2.getY()).thenReturn(2.0);

    // Mock level and placements
    List<ParsedLevelRecord> levels = new ArrayList<>();
    levels.add(
        new ParsedLevelRecord(List.of(entityPlacement1, entityPlacement2),
            new MapInfoRecord("wrap", 10, 10),
            null, null));
    when(configModel.levels()).thenReturn(levels);

    // ChatGPT on how to mock entity factory correctly
    try (MockedStatic<EntityFactory> mockedFactory = mockStatic(EntityFactory.class)) {
      mockedFactory.when(() -> EntityFactory.createEntity(
              eq(inputManager), eq(entityPlacement1), any(GameMapInterface.class), eq(configModel),
              any()))
          .thenReturn(entity1);

      mockedFactory.when(() -> EntityFactory.createEntity(
              eq(inputManager), eq(entityPlacement2), any(GameMapInterface.class), eq(configModel),
              any()))
          .thenReturn(entity2);

      assertThrows(InvalidPositionException.class,
          () -> GameMapFactory.createGameMap(inputManager, configModel, 0,
              new MultiplayerContextRecord(-1, null, null)));
    }
  }

  @Test
  void createGameMap_emptyEntityList_createsEmptyMap() throws InvalidPositionException {
    when(configModel.settings()).thenReturn(settings);
    // Mock level and placements
    List<ParsedLevelRecord> levels = new ArrayList<>();
    levels.add(
        new ParsedLevelRecord(List.of(), new MapInfoRecord("wrap", 10, 10),
            null, null));
    when(configModel.levels()).thenReturn(levels);

    GameMapInterface gameMap = GameMapFactory.createGameMap(inputManager, configModel, 0,
        new MultiplayerContextRecord(-1, null, null));

    assertNotNull(gameMap);
    for (int x = 0; x < 5; x++) {
      for (int y = 0; y < 5; y++) {
        assertTrue(gameMap.getEntityAt(x, y).isEmpty());
      }
    }
  }


}
