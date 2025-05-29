package oogasalad.authoring.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import oogasalad.authoring.controller.AuthoringController;
import oogasalad.authoring.model.AuthoringModel;
import oogasalad.authoring.view.gameSettings.GameSettingsView;
import oogasalad.engine.records.config.model.SettingsRecord;
import oogasalad.engine.records.config.model.losecondition.LivesBasedConditionRecord;
import oogasalad.engine.records.config.model.wincondition.SurviveForTimeConditionRecord;
import oogasalad.engine.utility.LanguageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.DukeApplicationTest;

public class GameSettingsViewTest extends DukeApplicationTest {

  private GameSettingsView view;
  private AuthoringController mockController;
  private AuthoringModel mockModel;
  private SettingsRecord defaultSettings;

  @Override
  public void start(Stage stage) {
    LanguageManager.setLanguage("English");
    mockController = mock(AuthoringController.class);
    mockModel = mock(AuthoringModel.class);

    defaultSettings = new SettingsRecord(1.0, 3, 100, new SurviveForTimeConditionRecord(5),
        new LivesBasedConditionRecord(), new HashSet<>(), false);
    when(mockController.getModel()).thenReturn(mockModel);
    when(mockModel.getDefaultSettings()).thenReturn(defaultSettings);

    view = new GameSettingsView(mockController);

    stage.setScene(new javafx.scene.Scene((javafx.scene.Parent) view.getNode()));
    stage.show();
  }

  @BeforeEach
  public void resetMocks() {
    reset(mockController, mockModel);
    when(mockController.getModel()).thenReturn(mockModel);
    when(mockModel.getDefaultSettings()).thenReturn(defaultSettings);
  }

  @Test
  public void updateFromModel_ModelChanges_ViewUpdates() {
    runAsJFXAction(() -> view.updateFromModel());
    Spinner<Double> gameSpeedSpinner = lookup(".spinner").queryAs(Spinner.class);
    assertEquals(1.0, gameSpeedSpinner.getValue(), 0.01);
  }

  @Test
  public void commitIntegerSpinnerValue_ValidInput_CommitsCorrectly() {
    runAsJFXAction(() -> {
      Spinner<Integer> spinner = lookup(".spinner").nth(1).queryAs(Spinner.class);
      clickOn(spinner.getEditor()).write("5").type(KeyCode.ENTER);
      view.updateFromModel();
      assertEquals(3, spinner.getValue()); // default mock value
    });
  }

  @Test
  public void saveSettings_Click_ModelUpdated() {
    runAsJFXAction(() -> {
      ScrollPane pane = lookup(".scroll-pane").queryAs(ScrollPane.class);
      pane.setVvalue(1.0); // Scroll all the way down
    });
    Button saveButton = lookup(LanguageManager.getMessage("SAVE_SETTINGS")).queryButton();
    clickOn(saveButton);
    verify(mockModel, times(1)).setDefaultSettings(any());
  }

}

