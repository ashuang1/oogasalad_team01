package oogasalad.authoring.view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import javafx.scene.Scene;
import javafx.stage.Stage;
import oogasalad.authoring.controller.AuthoringController;
import oogasalad.authoring.controller.LevelController;
import oogasalad.authoring.model.AuthoringModel;
import oogasalad.authoring.model.LevelDraft;
import oogasalad.authoring.view.mainView.AuthoringView;
import oogasalad.engine.records.config.model.SettingsRecord;
import oogasalad.engine.records.config.model.losecondition.LivesBasedConditionRecord;
import oogasalad.engine.records.config.model.wincondition.SurviveForTimeConditionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.DukeApplicationTest;

public class AuthoringViewTest extends DukeApplicationTest {

  private AuthoringView authoringView;
  private AuthoringController mockController;
  private LevelController mockLevelController;

  @Override
  public void start(Stage stage) {
    mockController = mock(AuthoringController.class);
    mockLevelController = mock(LevelController.class);
    AuthoringModel mockModel = mock(AuthoringModel.class);
    LevelDraft mockLevel = mock(LevelDraft.class);

    when(mockModel.getDefaultSettings()).thenReturn(
        new SettingsRecord(2.0, 2, 2, new SurviveForTimeConditionRecord(5),
            new LivesBasedConditionRecord(), new HashSet<>(), false));
    when(mockModel.getCurrentLevel()).thenReturn(mockLevel);
    when(mockLevel.getEntityPlacements()).thenReturn(List.of());
    when(mockLevelController.getCurrentLevel()).thenReturn(mockLevel);

    when(mockController.getModel()).thenReturn(mockModel);
    when(mockController.getLevelController()).thenReturn(mockLevelController);

    authoringView = new AuthoringView();
    authoringView.setController(mockController);

    Scene scene = new Scene(authoringView.getNode(), 1200, 800);
    stage.setScene(scene);
    stage.show();
  }


  @BeforeEach
  public void setup() {
    reset(mockController, mockLevelController);
  }

  @Test
  public void setController_ValidController_SubViewsInitialized() {
    AuthoringModel mockModel = mock(AuthoringModel.class);
    when(mockModel.getDefaultSettings()).thenReturn(
        new SettingsRecord(2.0, 2, 2, new SurviveForTimeConditionRecord(5),
            new LivesBasedConditionRecord(), new HashSet<>(), false));

    when(mockController.getModel()).thenReturn(mockModel);
    when(mockController.getLevelController()).thenReturn(mockLevelController);

    LevelDraft mockLevel = mock(LevelDraft.class);
    when(mockModel.getCurrentLevel()).thenReturn(mockLevel);
    when(mockLevel.getEntityPlacements()).thenReturn(List.of());
    when(mockLevelController.getCurrentLevel()).thenReturn(mockLevel);

    interact(() -> authoringView.setController(mockController));

    assertNotNull(authoringView.getCanvasView(), "CanvasView should be initialized");
    assertNotNull(authoringView.getEntitySelectorView(),
        "EntitySelectorView should be initialized");
    assertNotNull(authoringView.getEntityEditorView(),
        "EntityTypeEditorView should be initialized");
    assertNotNull(authoringView.getGameSettingsView(), "GameSettingsView should be initialized");
    assertNotNull(authoringView.getLevelSelectorView(), "LevelSelectorView should be initialized");

    verify(mockController, atLeastOnce()).getLevelController();
    verify(mockLevelController).initDefaultLevelIfEmpty();
  }


  @Test
  public void getEntityEditorView_DefaultVisibility_NotVisible() {
    assertFalse(authoringView.getEntityEditorView().getRoot().isVisible(),
        "EntityTypeEditorView should be hidden by default");
  }

  @Test
  public void setController_WindowMaximized_PlatformRunLaterExecuted() {
    assertNotNull(authoringView.getNode().getScene(), "Scene should be set when shown");
  }
}
