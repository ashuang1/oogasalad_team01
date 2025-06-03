package oogasalad.player.view;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import oogasalad.engine.view.components.FormattingUtil;

public class FormattingUtils {

  public void showErrorDialog(String title, String message) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    FormattingUtil.applyStandardDialogStyle(alert);
    alert.showAndWait();
  }
}
