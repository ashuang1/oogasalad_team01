package oogasalad.authoring.view;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import oogasalad.engine.records.config.model.controlConfig.ConditionalControlConfigRecord;
import oogasalad.engine.records.config.model.controlConfig.ControlConfigInterface;
import oogasalad.engine.records.config.model.controlConfig.KeyboardControlConfigRecord;
import oogasalad.engine.records.config.model.controlConfig.NoneControlConfigRecord;
import oogasalad.engine.records.config.model.controlConfig.TargetControlConfigRecord;
import oogasalad.engine.records.config.model.controlConfig.targetStrategy.TargetEntityConfigRecord;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import util.DukeApplicationTest;

class ControlTypeEditorViewTest extends DukeApplicationTest {

  private ControlTypeEditorView controlTypeEditorView;
  private ControlConfigInterface controlType;

  public void start(Stage stage) {
    controlTypeEditorView = new ControlTypeEditorView();
    Scene scene = new Scene(controlTypeEditorView.getRoot(), 600, 400);
    stage.setScene(scene);
    stage.show();
  }

  @Test
  void getControlConfig_ValidateDefaultType_ReturnsNoneControlStrategy() {
    controlType = new NoneControlConfigRecord();
    assertEquals(controlTypeEditorView.getControlConfig(), controlType);
  }

  @Test
  void getControlConfig_ValidateKeyboardConfigOnSelection_ReturnsKeyboardControlStrategy()
      throws TimeoutException {
    controlType = new KeyboardControlConfigRecord(null);
    clickOn("#control-type-selector");
    clickOn("Keyboard");
    waitForFxEvents();
    ControlConfigInterface actual = FxToolkit.setupFixture(
        () -> controlTypeEditorView.getControlConfig());

    assertEquals(controlType, actual);
  }

  @Test
  void getControlConfig_ValidTargetStrategyCreation_ReturnsTargetControlStrategy()
      throws TimeoutException {
    controlType = new TargetControlConfigRecord("Bfs", new TargetEntityConfigRecord("test"), null);
    clickOn("#control-type-selector");
    clickOn("Target");
    waitForFxEvents();
    clickOn("#path-finding-combo");
    clickOn("Bfs");
    clickOn("#target-calculation-combo");
    clickOn("TargetEntity");
    waitForFxEvents();
    writeInputTo(lookup("#field-targetType").queryAs(TextInputControl.class), "test");

    // Assert correct control config is returned:
    ControlConfigInterface result = FxToolkit.setupFixture(
        () -> controlTypeEditorView.getControlConfig());
    assertEquals(controlType, result);
  }

  @Test
  void getControlConfig_InvalidInputForTargetLocation_ThrowsException() {
    controlType = new TargetControlConfigRecord("Bfs", new TargetEntityConfigRecord("test"), null);
    clickOn("#control-type-selector");
    clickOn("Target");
    waitForFxEvents();
    clickOn("#path-finding-combo");
    clickOn("Bfs");
    clickOn("#target-calculation-combo");
    clickOn("TargetLocation");
    waitForFxEvents();
    writeInputTo(lookup("#field-targetX").queryAs(TextInputControl.class),
        "abc"); // not a valid coordinate.
    writeInputTo(lookup("#field-targetY").queryAs(TextInputControl.class), "def");

    // Assert throw error due to invalid input
    // Wrap the call to avoid FX thread exception
    ViewException[] thrown = new ViewException[1];
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(() -> {
      new Thread(() -> {
        // Slight delay to let the alert appear
        try {
          Thread.sleep(400);
          clickOn("OK"); // dismiss the alert dialog
          Thread.sleep(400);
          clickOn("OK");
        } catch (Exception ignored) {}
      }).start();

      try {
        controlTypeEditorView.getControlConfig();
      } catch (ViewException e) {
        thrown[0] = e;
      } finally {
        latch.countDown();
      }
    });

    try {
      latch.await(); // wait for FX thread to finish
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    assertNotNull(thrown[0]); // verify exception was thrown
  }

  @Test
  void populateControlConfig_PopulateViewFromConfigRecord_Success() {
    controlType = new TargetControlConfigRecord("Bfs", new TargetEntityConfigRecord("test"), null);
    // Use Platform.runLater to ensure the UI manipulation happens on the FX application thread
    // Asked ChatGPT for debugging this.
    Platform.runLater(() -> controlTypeEditorView.populateControlConfigUI(controlType));
    waitForFxEvents();
    assertEquals(controlType, controlTypeEditorView.getControlConfig());
  }

  @Test
  void populateControlConfig_PopulateViewFromConditionalConfigRecord_Success() {
    controlType = new ConditionalControlConfigRecord(5, "Bfs", "Random",
        new TargetEntityConfigRecord("test"), null);
    // Use Platform.runLater to ensure the UI manipulation happens on the FX application thread
    // Asked ChatGPT for debugging this.
    Platform.runLater(() -> controlTypeEditorView.populateControlConfigUI(controlType));
    waitForFxEvents();
    assertEquals(controlType, controlTypeEditorView.getControlConfig());
  }

}