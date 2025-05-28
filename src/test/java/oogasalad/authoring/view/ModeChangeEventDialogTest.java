package oogasalad.authoring.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import oogasalad.authoring.model.LevelDraft;
import oogasalad.engine.records.config.ImageConfigRecord;
import oogasalad.engine.records.config.ModeConfigRecord;
import oogasalad.engine.records.config.model.EntityPropertiesRecord;
import oogasalad.engine.records.config.model.controlConfig.KeyboardControlConfigRecord;
import oogasalad.engine.records.model.EntityTypeRecord;
import oogasalad.engine.records.model.ModeChangeEventRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.DukeApplicationTest;

public class ModeChangeEventDialogTest extends DukeApplicationTest {

  private LevelDraft level;

  @Override
  public void start(Stage stage) {
    level = new LevelDraft("Test Level", "test.json");

    // Create dummy mode config records
    Map<String, ModeConfigRecord> modes = new HashMap<>();
    modes.put("chase", dummyMode("chase"));
    modes.put("scatter", dummyMode("scatter"));

    EntityTypeRecord entity = new EntityTypeRecord(
        "Ghost",
        modes,
        Collections.emptyList()
    );

    Map<String, EntityTypeRecord> map = new HashMap<>();
    map.put("Ghost", entity);

    ModeChangeEventDialog dialog = new ModeChangeEventDialog(map, level);
    dialog.initOwner(stage);
    dialog.show();
  }

  @BeforeEach
  void resetLevel() {
    level.getModeChangeEvents().clear();
  }

  @Test
  public void testAddValidTimeModeEvent() {
    // Get all combo boxes (in order: entityType, currentMode, nextMode, conditionType)
    ComboBox<String> entityBox = lookup(".combo-box").nth(0).query();
    ComboBox<String> currentModeBox = lookup(".combo-box").nth(1).query();
    ComboBox<String> nextModeBox = lookup(".combo-box").nth(2).query();
    ComboBox<String> conditionBox = lookup(".combo-box").nth(3).query();

    select(entityBox, "Ghost");
    select(currentModeBox, "chase");
    select(nextModeBox, "scatter");
    select(conditionBox, "TimeElapsed");

    // Enter amount in first visible text field (time amount)
    TextField amountField = lookup(".text-field").query();
    writeInputTo(amountField, "10");

    // Click the "Add Event" button
    clickOn(lookup("Add Event").queryButton());

    // Validate table content
    TableView<ModeChangeEventRecord> table = lookup(".table-view").query();
    assertEquals(1, table.getItems().size());

    ModeChangeEventRecord event = table.getItems().getFirst();
    assertEquals("Ghost", event.entityType().type());
    assertEquals("chase", event.modeChangeInfo().originalMode());
    assertEquals("scatter", event.modeChangeInfo().transitionMode());
    assertEquals("TimeElapsed", event.changeCondition().type());
    assertEquals("10", event.changeCondition().parameters().get("amount"));
  }

  private ModeConfigRecord dummyMode(String name) {
    return new ModeConfigRecord(
        name,
        new EntityPropertiesRecord(name, Collections.emptyList()),
        new KeyboardControlConfigRecord(null),
        new ImageConfigRecord("file:test.png", 32, 32, 4, 1.0),
        1.0
    );
  }
}
