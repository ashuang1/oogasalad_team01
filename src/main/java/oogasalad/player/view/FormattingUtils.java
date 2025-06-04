package oogasalad.player.view;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import oogasalad.engine.view.components.FormattingUtil;

/**
 * Utility class for displaying standardized UI dialogs.
 */
public class FormattingUtils {

  /**
   * Displays an error dialog with the given title and message using standard styling.
   *
   * @param title   the title of the error dialog
   * @param message the message to display in the dialog
   */
  public void showErrorDialog(String title, String message) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    FormattingUtil.applyStandardDialogStyle(alert);
    alert.showAndWait();
  }
}
