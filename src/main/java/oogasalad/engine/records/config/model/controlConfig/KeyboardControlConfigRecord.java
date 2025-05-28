package oogasalad.engine.records.config.model.controlConfig;

public record KeyboardControlConfigRecord(Integer player) implements ControlConfigInterface {

  @Override
  public Integer getPlayer() {
    return player;
  }
}
