package oogasalad.engine.records.config.model.controlConfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * An interface that defines how to convert JSON objects into the correct corresponding java
 * records.
 *
 * @author Owen Jennings
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "controlStrategy")
@JsonSubTypes({
    @JsonSubTypes.Type(value = NoneControlConfigRecord.class, name = "None"),
    @JsonSubTypes.Type(value = KeyboardControlConfigRecord.class, name = "Keyboard"),
    @JsonSubTypes.Type(value = JumpControlConfigRecord.class, name = "Jump"),
    @JsonSubTypes.Type(value = TargetControlConfigRecord.class, name = "Target"),
    @JsonSubTypes.Type(value = ConditionalControlConfigRecord.class, name = "Conditional"),
    @JsonSubTypes.Type(value = ConstantDirectionControlConfigRecord.class, name = "ConstantDirection"),
    @JsonSubTypes.Type(value = FollowEntityControlConfigRecord.class, name = "FollowEntity")
})
public interface ControlConfigInterface {

  default Integer getPlayer() {
    return null;
  }
}
