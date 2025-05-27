package oogasalad.engine.records.config.model.controlConfig;

import oogasalad.engine.records.config.model.controlConfig.targetStrategy.TargetCalculationConfigInterface;

public record TargetControlConfigRecord(
    String pathFindingStrategy,
    TargetCalculationConfigInterface targetCalculationConfig,
    Integer player
) implements ControlConfigInterface {

  @Override
  public Integer getPlayer() {
    return player;
  }
}
