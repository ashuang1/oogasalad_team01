package oogasalad.authoring.view.gameSettings;

import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

/**
 * A UI component that allows users to enable or disable multiplayer mode in the game settings.
 * This component consists of a single checkbox labeled "Multiplayer".
 *
 * @author Austin Huang
 */
public class MultiplayerEditor extends VBox {

  private final CheckBox multiplayerCheckBox = new CheckBox("Multiplayer");

  /**
   * Constructs a new {@code MultiplayerEditor} and adds the multiplayer checkbox to the layout.
   */
  public MultiplayerEditor() {
    getChildren().add(multiplayerCheckBox);
  }

  /**
   * Returns whether the multiplayer checkbox is selected.
   *
   * @return {@code true} if multiplayer mode is enabled, {@code false} otherwise
   */
  public boolean getIsMultiplayerEnabled() {
    return multiplayerCheckBox.isSelected();
  }

  /**
   * Updates the checkbox to reflect the given multiplayer setting.
   *
   * @param value {@code true} to select the checkbox (enable multiplayer), {@code false} to
   *                          deselect it
   */
  public void update(boolean value) {
    multiplayerCheckBox.setSelected(value);
  }
}
