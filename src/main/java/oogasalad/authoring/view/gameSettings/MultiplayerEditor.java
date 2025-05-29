package oogasalad.authoring.view.gameSettings;

import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

public class MultiplayerEditor extends VBox {

  private CheckBox multiplayerCheckBox = new CheckBox("Multiplayer");

  public MultiplayerEditor() {
    getChildren().add(multiplayerCheckBox);
  }

  public boolean getIsMultiplayerEnabled() {
    return multiplayerCheckBox.isSelected();
  }

  public void update(boolean value) {
    multiplayerCheckBox.setSelected(value);
  }
}
