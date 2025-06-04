package oogasalad.engine.records.config.model.controlConfig;

import oogasalad.engine.records.config.model.controlConfig.targetStrategy.TargetCalculationConfigInterface;

public record ConditionalControlConfigRecord(
    int radius,
    String pathFindingStrategyInRadius,
    String pathFindingStrategyOutRadius,
    TargetCalculationConfigInterface targetCalculationConfig,
    Integer player
) implements ControlConfigInterface {

  @Override
  public Integer getPlayer() {
    return player;
  }
}
