package oogasalad.engine.records.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import oogasalad.engine.records.config.model.losecondition.LoseConditionInterface;
import oogasalad.engine.records.config.model.wincondition.WinConditionInterface;
import oogasalad.player.model.enums.CheatType;

/**
 * Represents the configuration settings for a game. This record encapsulates various parameters
 * that define the initial state and behavior of the game.
 *
 * @param gameSpeed     The speed at which the game progresses, represented as a Double.
 * @param startingLives The number of lives the player starts with, represented as an Integer.
 * @param initialScore  The initial score of the player, represented as an Integer.
 * @param winCondition  A WinCondition object that storing information determining when a win has
 *                      occurred.
 * @author Owen Jennings, Jessica Chen
 */
public record SettingsRecord(Double gameSpeed, Integer startingLives, Integer initialScore,
                             WinConditionInterface winCondition,
                             LoseConditionInterface loseCondition,
                             Set<CheatType> cheatTypes,
                             @JsonProperty(defaultValue = "false") boolean isMultiplayer) {

}
