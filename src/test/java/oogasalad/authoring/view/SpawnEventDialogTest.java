package oogasalad.authoring.view;

import static org.junit.jupiter.api.Assertions.*;

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
import oogasalad.engine.records.config.model.SpawnEventRecord;
import oogasalad.engine.records.config.model.controlConfig.KeyboardControlConfigRecord;
import oogasalad.engine.records.model.EntityTypeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.DukeApplicationTest;

public class SpawnEventDialogTest extends DukeApplicationTest {

  private LevelDraft level;

  @Override
  public void start(Stage stage) {
    level = new LevelDraft("Test Level", "level_test.json");

    Map<String, ModeConfigRecord> modes = new HashMap<>();
    modes.put("Default", dummyMode("Default"));

    EntityTypeRecord entity = new EntityTypeRecord(
        "Cherry",
        modes,
        Collections.emptyList()
    );

    Map<String, EntityTypeRecord> entityTypes = new HashMap<>();
    entityTypes.put("Cherry", entity);

    SpawnEventDialog dialog = new SpawnEventDialog(entityTypes, level);
    dialog.initOwner(stage);
    dialog.show();
  }

  @BeforeEach
  public void clearSpawnEvents() {
    level.getSpawnEvents().clear();
  }

  @Test
  public void testAddValidSpawnEvent() {
    // Select entity type
    ComboBox<String> entityTypeBox = lookup(".combo-box").nth(0).query();
    select(entityTypeBox, "Cherry");

    // Select mode
    ComboBox<String> modeBox = lookup(".combo-box").nth(1).query();
    select(modeBox, "Default");

    // Enter X and Y
    TextField xField = lookup(".text-field").nth(0).query();
    TextField yField = lookup(".text-field").nth(1).query();
    writeInputTo(xField, "12.5");
    writeInputTo(yField, "8");

    // Select spawn condition type
    ComboBox<String> spawnCondTypeBox = lookup(".combo-box").nth(2).query();
    select(spawnCondTypeBox, "ScoreBased");

    // Enter spawn condition amount
    TextField spawnAmount = lookup(".text-field").nth(2).query();
    writeInputTo(spawnAmount, "700");

    // Click add
    clickOn(lookup("Add Spawn Event").queryButton());

    TableView<SpawnEventRecord> table = lookup(".table-view").query();
    assertEquals(1, table.getItems().size());

    SpawnEventRecord event = table.getItems().get(0);
    assertEquals("Cherry", event.entityType().type());
    assertEquals("Default", event.mode());
    assertEquals(12.5, event.x());
    assertEquals(8, event.y());
    assertEquals("ScoreBased", event.spawnCondition().type());
    assertEquals(700, event.spawnCondition().parameters().get("amount"));
    assertNull(event.despawnCondition());
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
