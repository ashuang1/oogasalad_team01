package oogasalad.player.model.api;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Map;

import oogasalad.engine.config.EntityPlacement;
import oogasalad.engine.records.config.ModeConfigRecord;
import oogasalad.engine.records.config.model.controlConfig.*;
import oogasalad.engine.records.config.model.controlConfig.targetStrategy.*;
import oogasalad.engine.records.model.EntityTypeRecord;
import oogasalad.player.model.GameMapInterface;
import oogasalad.player.model.exceptions.TargetStrategyException;
import oogasalad.player.model.strategies.control.targetcalculation.TargetStrategyInterface;
import oogasalad.player.model.strategies.control.targetcalculation.testdoubles.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TargetStrategyFactoryTest {

  private GameMapInterface mockMap;
  private EntityPlacement mockPlacement;
  private EntityTypeRecord mockType;
  private final String mockMode = "DEFAULT";

  @BeforeEach
  void setup() throws Exception {
    mockMap = mock(GameMapInterface.class);
    mockPlacement = mock(EntityPlacement.class);
    mockType = mock(EntityTypeRecord.class);

    when(mockPlacement.getType()).thenReturn(mockType);
    when(mockPlacement.getMode()).thenReturn(mockMode);
    when(mockPlacement.getTypeString()).thenReturn("SomeType");

    Field pkgField = TargetStrategyFactory.class.getDeclaredField("STRATEGY_PACKAGE");
    pkgField.setAccessible(true);
    pkgField.set(null, "oogasalad.player.model.strategies.control.targetcalculation.testdoubles.");
  }

  private void mockControlConfig(ControlConfigInterface controlConfig) {
    ModeConfigRecord mockProps = mock(ModeConfigRecord.class);
    when(mockProps.controlConfig()).thenReturn(controlConfig);

    when(mockType.modes()).thenReturn(Map.of(mockMode, mockProps));
  }

  private void mockTargetControlConfig(TargetCalculationConfigInterface targetConfig) {
    mockControlConfig(new TargetControlConfigRecord("", targetConfig, null));
  }

  private void mockConditionalControlConfig(TargetCalculationConfigInterface targetConfig) {
    mockControlConfig(new ConditionalControlConfigRecord(3, "", "", targetConfig));
  }

  @Test
  void createTargetStrategy_targetLocationStrategy_createdSuccessfully() {
    mockTargetControlConfig(new TargetLocationConfigRecord(10.0, 20.0));
    TargetStrategyInterface strategy = TargetStrategyFactory.createTargetStrategy(mockPlacement,
        mockMap);
    assertInstanceOf(TargetLocationStrategy.class, strategy);
  }

  @Test
  void createTargetStrategy_targetEntityStrategy_createdSuccessfully() {
    mockTargetControlConfig(new TargetEntityConfigRecord("Enemy"));
    TargetStrategyInterface strategy = TargetStrategyFactory.createTargetStrategy(mockPlacement,
        mockMap);
    assertInstanceOf(TargetEntityStrategy.class, strategy);
  }

  @Test
  void createTargetStrategy_targetAheadOfEntityStrategy_createdSuccessfully() {
    mockTargetControlConfig(new TargetAheadOfEntityConfigRecord("Ally", 2));
    TargetStrategyInterface strategy = TargetStrategyFactory.createTargetStrategy(mockPlacement,
        mockMap);
    assertInstanceOf(TargetAheadOfEntityStrategy.class, strategy);
  }

  @Test
  void createTargetStrategy_targetEntityWithTrapStrategy_createdSuccessfully() {
    mockConditionalControlConfig(new TargetEntityWithTrapConfigRecord("Enemy", 1, "Trap"));
    TargetStrategyInterface strategy = TargetStrategyFactory.createTargetStrategy(mockPlacement,
        mockMap);
    assertInstanceOf(TargetEntityWithTrapStrategy.class, strategy);
  }

  // NEGATIVE TESTS

  @Test
  void createTargetStrategy_unknownControlConfig_throwsException() {
    ControlConfigInterface unknownConfig = mock(ControlConfigInterface.class);
    mockControlConfig(unknownConfig);

    TargetStrategyException exception = assertThrows(
        TargetStrategyException.class,
        () -> TargetStrategyFactory.createTargetStrategy(mockPlacement, mockMap)
    );
    assertTrue(exception.getMessage().contains("No TargetStrategy available"));
  }

  @Test
  void createTargetStrategy_noValidConstructor_throwsException() {
    mockTargetControlConfig(new TargetNoConstructorConfig("Nope"));
    assertThrows(
        TargetStrategyException.class,
        () -> TargetStrategyFactory.createTargetStrategy(mockPlacement, mockMap)
    );
  }

  // Dummy config and strategy with no valid constructor
  record TargetNoConstructorConfig(String key) implements TargetCalculationConfigInterface {

  }

  static class TargetNoConstructorStrategy implements TargetStrategyInterface {

    private TargetNoConstructorStrategy() {
    }

    @Override
    public int[] getTargetPosition() {
      return new int[0];
    }
  }
}
