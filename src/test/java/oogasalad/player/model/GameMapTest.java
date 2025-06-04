package oogasalad.player.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.*;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import oogasalad.engine.config.EntityPlacement;
import oogasalad.engine.exceptions.EntityNotFoundException;
import oogasalad.engine.exceptions.InvalidPositionException;
import oogasalad.engine.records.MultiplayerContextRecord;
import oogasalad.engine.records.config.ConfigModelRecord;
import oogasalad.engine.records.config.ModeConfigRecord;
import oogasalad.engine.records.config.model.controlConfig.ControlConfigInterface;
import oogasalad.engine.records.config.model.controlConfig.KeyboardControlConfigRecord;
import oogasalad.engine.records.model.EntityTypeRecord;
import oogasalad.engine.utility.constants.GameConfig;
import oogasalad.player.controller.GameInputManager;
import oogasalad.player.model.api.EntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import util.DukeApplicationTest;

@ExtendWith(MockitoExtension.class)
class GameMapTest extends DukeApplicationTest {

  private final int width = 10;
  private final int height = 10;
  private GameMapInterface myGameMap;
  private Entity myEntity;
  private GameInputManager myInput;

  @Override
  public void start(Stage stage) {
    Group root = new Group();
    Scene myScene = new Scene(root, GameConfig.WIDTH, GameConfig.HEIGHT);
    myInput = new GameInputManager(myScene, root);
  }

  @BeforeEach
  void setUp() {
    myGameMap = new GameMap(width, height);
    Map<String, ModeConfigRecord> modes = new HashMap<>();
    ControlConfigInterface mockControl = new KeyboardControlConfigRecord(null);
    ModeConfigRecord newMode = new ModeConfigRecord("Default", null, mockControl, null, null);
    modes.put("Default", newMode);
    EntityTypeRecord data = new EntityTypeRecord("test", modes, new ArrayList<String>());
    EntityPlacement placement = new EntityPlacement(data, 5, 5, "Default");
    myEntity = EntityFactory.createEntity(myInput, placement, myGameMap,
        mock(ConfigModelRecord.class),
        new MultiplayerContextRecord(-1, null, null));
  }

  @Test
  void addEntity_attemptAddingValidEntity_Success() {
    assertDoesNotThrow(() -> myGameMap.addEntity(myEntity));
    assertEquals(myEntity, myGameMap.iterator().next());
  }

  @Test
  void addEntity_attemptAddingEntityOutOfBounds_ThrowException() {
    myEntity.getEntityPlacement().setX(width + 1);
    myEntity.getEntityPlacement().setY(height + 1);
    assertThrows(InvalidPositionException.class, () -> myGameMap.addEntity(myEntity));
  }

  @Test
  void removeEntity_attemptRemovingValidEntity_Success() {
    assertDoesNotThrow(() -> myGameMap.addEntity(myEntity));
    assertDoesNotThrow(() -> myGameMap.removeEntity(myEntity));
    assertFalse(myGameMap.iterator().hasNext());
  }

  @Test
  void removeEntity_attemptRemovingNonExistentEntity_ThrowException() {
    assertThrows(EntityNotFoundException.class, () -> myGameMap.removeEntity(myEntity));
  }


  @Test
  void getEntityAt_attemptGetEntityAt_ReturnsEntityAtPosition() {
    assertDoesNotThrow(() -> myGameMap.addEntity(myEntity));
    Optional<Entity> result = myGameMap.getEntityAt((int) myEntity.getEntityPlacement().getX(),
        (int) myEntity.getEntityPlacement().getY());
    assertTrue(result.isPresent());
    assertEquals(myEntity, result.get());
  }

  @Test
  void getEntityAt_attemptGetOfNonExistentEntity_ReturnsEmptyOptional() {
    assertDoesNotThrow(() -> myGameMap.addEntity(myEntity));
    assertTrue(myGameMap.getEntityAt(1, 1).isEmpty());
  }


  @Test
  void iterator_ensureEntityIteratorContainsAddedEntities_Success() {
    assertDoesNotThrow(() -> myGameMap.addEntity(myEntity));
    Map<String, ModeConfigRecord> modes = new HashMap<>();
    ControlConfigInterface mockControl = new KeyboardControlConfigRecord(null);
    ModeConfigRecord newMode = new ModeConfigRecord("Default", null, mockControl, null, null);
    modes.put("Default", newMode);
    EntityTypeRecord data = new EntityTypeRecord("test", modes, new ArrayList<String>());
    EntityPlacement placement = new EntityPlacement(data, 5, 5, "Default");
    Entity secondEntity = EntityFactory.createEntity(myInput, placement, myGameMap,
        mock(ConfigModelRecord.class),
        new MultiplayerContextRecord(-1, null, null));
    assertDoesNotThrow(() -> myGameMap.addEntity(secondEntity));
    for (Entity next : myGameMap) {
      assertTrue(next.equals(secondEntity) || next.equals(myEntity));
    }
  }

  @Test
  void getWidth_attemptGetWidth_ReturnsWidth() {
    assertEquals(width, myGameMap.getWidth());
  }

  @Test
  void getHeight_attemptGetHeight_ReturnsHeight() {
    assertEquals(height, myGameMap.getHeight());
  }

  @Test
  void isValidPosition_attemptGetValidPosition_ReturnsTrue() {
    assertTrue(myGameMap.isValidPosition(0, 0));
    assertTrue(myGameMap.isValidPosition(width - 1, height - 1));
  }

  @Test
  void isValidPosition_attemptGetInvalidPosition_ReturnsFalse() {
    assertFalse(myGameMap.isValidPosition(-1, 0));
    assertFalse(myGameMap.isValidPosition(0, -1));
    assertFalse(myGameMap.isValidPosition(width, height));
  }

  @Test
  void isNotBlocked_attemptNoEntityAtPosition_ReturnsTrue() {
    assertTrue(myGameMap.isNotBlocked("Anything", 1, 1));
  }

  @Test
  void isNotBlocked_attemptNonBlockingEntityAtPosition_ReturnsTrue()
      throws InvalidPositionException {
    // Create an entity that does NOT block anything
    Map<String, ModeConfigRecord> modes = new HashMap<>();
    ControlConfigInterface mockControl = new KeyboardControlConfigRecord(null);
    ModeConfigRecord newMode = new ModeConfigRecord("Default", null, mockControl, null, null);
    modes.put("Default", newMode);
    EntityTypeRecord data = new EntityTypeRecord("test", modes, new ArrayList<String>());
    EntityPlacement placement = new EntityPlacement(data, 5, 5, "Default");
    Entity nonBlockingEntity = EntityFactory.createEntity(myInput, placement, myGameMap,
        mock(ConfigModelRecord.class),
        new MultiplayerContextRecord(-1, null, null));
    myGameMap.addEntity(nonBlockingEntity);

    assertTrue(myGameMap.isNotBlocked("anyType", 3, 3));
  }

  @Test
  void isNotBlocked_attemptBlockingEntityAtPosition_ReturnsFalse() throws InvalidPositionException {
    // Create an entity that blocks "Player"
    Map<String, ModeConfigRecord> modes = new HashMap<>();
    ControlConfigInterface mockControl = new KeyboardControlConfigRecord(null);
    ModeConfigRecord newMode = new ModeConfigRecord("Default", null, mockControl, null, null);
    modes.put("Default", newMode);
    EntityTypeRecord data = new EntityTypeRecord("test", modes, List.of("Player"));
    EntityPlacement placement = new EntityPlacement(data, 4, 4, "Default");
    Entity blockingEntity = EntityFactory.createEntity(myInput, placement, myGameMap,
        mock(ConfigModelRecord.class),
        new MultiplayerContextRecord(-1, null, null));
    myGameMap.addEntity(blockingEntity);

    assertFalse(myGameMap.isNotBlocked("Player", 4, 4));
  }
}