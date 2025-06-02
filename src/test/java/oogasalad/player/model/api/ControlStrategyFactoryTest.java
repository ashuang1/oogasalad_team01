package oogasalad.player.model.api;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Map;

import oogasalad.engine.config.EntityPlacement;
import oogasalad.engine.records.config.ModeConfigRecord;
import oogasalad.engine.records.config.model.controlConfig.*;
import oogasalad.engine.records.model.EntityTypeRecord;
import oogasalad.player.controller.GameInputManager;
import oogasalad.player.model.GameMapInterface;
import oogasalad.player.model.exceptions.ControlStrategyException;
import oogasalad.player.model.strategies.control.ControlStrategyInterface;
import oogasalad.player.model.strategies.control.testdoubles.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ControlStrategyFactoryTest {

  private GameInputManager mockInput;
  private GameMapInterface mockMap;
  private EntityPlacement mockPlacement;
  private EntityTypeRecord mockType;
  private final String mockMode = "DEFAULT";

  @BeforeEach
  void setup() throws Exception {
    mockInput = mock(GameInputManager.class);
    mockMap = mock(GameMapInterface.class);
    mockPlacement = mock(EntityPlacement.class);
    mockType = mock(EntityTypeRecord.class);

    when(mockPlacement.getMode()).thenReturn(mockMode);
    when(mockPlacement.getType()).thenReturn(mockType);

    // Override the strategy package for testing
    Field strategyPackageField = ControlStrategyFactory.class.getDeclaredField("STRATEGY_PACKAGE");
    strategyPackageField.setAccessible(true);
    strategyPackageField.set(null, "oogasalad.player.model.strategies.control.testdoubles.");
  }

  private void mockControlConfig(ControlConfigInterface config) {
    ModeConfigRecord mockProperties = mock(ModeConfigRecord.class);
    when(mockProperties.controlConfig()).thenReturn(config);

    when(mockType.modes()).thenReturn(Map.of(mockMode, mockProperties));
  }

  @Test
  void createControlStrategy_noneControl_strategyCreated() {
    mockControlConfig(new NoneControlConfigRecord());

    ControlStrategyInterface strategy = ControlStrategyFactory.createControlStrategy(mockInput,
        mockPlacement, mockMap, -1, null, null);
    assertInstanceOf(NoneControlStrategy.class, strategy);
  }

  @Test
  void createControlStrategy_keyboardControl_strategyCreated() {
    mockControlConfig(new KeyboardControlConfigRecord(null));

    ControlStrategyInterface strategy = ControlStrategyFactory.createControlStrategy(mockInput,
        mockPlacement, mockMap, -1, null, null);
    assertInstanceOf(KeyboardControlStrategy.class, strategy);
  }

  @Test
  void createControlStrategy_targetControl_strategyCreated() {
    mockControlConfig(new TargetControlConfigRecord("A*", null, null));

    ControlStrategyInterface strategy = ControlStrategyFactory.createControlStrategy(mockInput,
        mockPlacement, mockMap, -1, null, null);
    assertInstanceOf(TargetControlStrategy.class, strategy);
  }

  @Test
  void createControlStrategy_conditionalControl_strategyCreated() {
    mockControlConfig(new ConditionalControlConfigRecord(5, "A*", "Dijkstra", null, null));

    ControlStrategyInterface strategy = ControlStrategyFactory.createControlStrategy(mockInput,
        mockPlacement, mockMap, -1, null, null);
    assertInstanceOf(ConditionalControlStrategy.class, strategy);
  }

  @Test
  void createControlStrategy_invalidStrategyControl_throwsControlStrategyException() {
    // Use anonymous subclass to simulate unsupported config
    mockControlConfig(new ControlConfigInterface() {
    });

    assertThrows(ControlStrategyException.class,
        () -> ControlStrategyFactory.createControlStrategy(mockInput, mockPlacement, mockMap, -1, null, null));
  }
}
