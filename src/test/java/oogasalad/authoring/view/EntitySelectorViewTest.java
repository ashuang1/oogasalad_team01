package oogasalad.authoring.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import oogasalad.authoring.controller.AuthoringController;
import oogasalad.engine.records.config.ImageConfigRecord;
import oogasalad.engine.records.config.ModeConfigRecord;
import oogasalad.engine.records.config.model.EntityPropertiesRecord;
import oogasalad.engine.records.config.model.controlConfig.ControlConfigInterface;
import oogasalad.engine.records.config.model.controlConfig.KeyboardControlConfigRecord;
import oogasalad.engine.records.config.model.controlConfig.NoneControlConfigRecord;
import oogasalad.engine.records.model.EntityTypeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.DukeApplicationTest;

public class EntitySelectorViewTest extends DukeApplicationTest {

  private EntitySelectorView view;
  private AuthoringController mockController;
  private ModeConfigRecord mode;

  @Override
  public void start(javafx.stage.Stage stage) {
    mockController = mock(AuthoringController.class);
    view = new EntitySelectorView(mockController);
    javafx.scene.Scene scene = new javafx.scene.Scene(view.getRoot(), 500, 400);
    stage.setScene(scene);

    // --- Create ImageConfig ---
    ImageConfigRecord imageConfig = new ImageConfigRecord(
        "mock.png", // Should be a relative path or valid URI
        14,
        14,
        3,
        1.0
    );

    // --- Create EntityProperties ---
    ControlConfigInterface controlConfig = new KeyboardControlConfigRecord(null);

    EntityPropertiesRecord props = new EntityPropertiesRecord(
        "Mode 1",
        List.of()
    );

    // --- Create ModeConfig ---
    mode = new ModeConfigRecord("Default", props, new NoneControlConfigRecord(), imageConfig, 1.0);

    stage.show();
  }

  @BeforeEach
  public void setup() {
    reset(mockController);
  }

  @Test
  public void addButton_Click_CallsCreateNewEntityType() {
    Button addButton = lookup("+ Add Entity Type").queryButton();
    runAsJFXAction(() -> clickOn(addButton));
    verify(mockController).createNewEntityType();
  }

  @Test
  public void updateEntities_CreatesCorrectTiles() {

    // --- Create EntityType ---
    EntityTypeRecord entity = new EntityTypeRecord(
        "Ghost",
        Map.of("Default", mode),
        List.of()
    );

    // --- Run and verify ---
    runAsJFXAction(() -> view.updateEntities(List.of(entity)));

    FlowPane tileGrid = lookup(".flow-pane").query();
    assertEquals(1, tileGrid.getChildren().size());
    VBox tile = (VBox) tileGrid.getChildren().get(0);
    assertInstanceOf(ImageView.class, tile.getChildren().get(0));
  }


  @Test
  public void selectEntityType_ClickTile_CallsSelectEntityType() {
    EntityTypeRecord entity = new EntityTypeRecord(
        "Pacman",
        Map.of("Default", mode),
        List.of()
    );
    runAsJFXAction(() -> view.updateEntities(List.of(entity)));

    VBox tile = (VBox) ((FlowPane) lookup(".flow-pane").query()).getChildren().getFirst();
    runAsJFXAction(() -> clickOn(tile));

    verify(mockController).selectEntityType("Pacman");
  }

  @Test
  public void highlightEntityTile_HighlightsCorrectTile() {
    EntityTypeRecord entity = new EntityTypeRecord(
        "Wall",
        Map.of("Default", mode),
        List.of()
    );
    runAsJFXAction(() -> view.updateEntities(List.of(entity)));
    runAsJFXAction(() -> view.highlightEntityTile("Wall"));

    VBox tile = (VBox) ((FlowPane) lookup(".flow-pane").query()).getChildren().getFirst();
    assertTrue(tile.getStyleClass().contains("selected-tile"));
  }
}
