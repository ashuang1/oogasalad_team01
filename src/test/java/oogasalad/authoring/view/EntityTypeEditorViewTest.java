package oogasalad.authoring.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import oogasalad.authoring.controller.AuthoringController;
import oogasalad.authoring.model.AuthoringModel;
import oogasalad.engine.records.config.ImageConfigRecord;
import oogasalad.engine.records.config.ModeConfigRecord;
import oogasalad.engine.records.config.model.EntityPropertiesRecord;
import oogasalad.engine.records.config.model.controlConfig.KeyboardControlConfigRecord;
import oogasalad.engine.records.model.EntityTypeRecord;
import oogasalad.engine.utility.LanguageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class EntityTypeEditorViewTest extends ApplicationTest {

  private EntityTypeEditorView view;
  private EntityTypeRecord mockEntityType;

  @BeforeEach
  public void setUp() {
    LanguageManager.setLanguage("English");
    AuthoringController mockController = mock(AuthoringController.class);
    view = new EntityTypeEditorView(mockController);

    // === Construct ModeConfig using new record-based structure ===
    String imagePath = "mock.png";
    ImageConfigRecord image = new ImageConfigRecord(
        imagePath,
        14,
        14,
        2,
        1.0
    );

    EntityPropertiesRecord entityProps = new EntityPropertiesRecord(
        "Default",
        List.of()
    );

    ModeConfigRecord mockMode = new ModeConfigRecord("Default", entityProps,
        new KeyboardControlConfigRecord(null), image, 1.0);
    Map<String, ModeConfigRecord> modeMap = new HashMap<>();
    modeMap.put("Default", mockMode);

    mockEntityType = new EntityTypeRecord("Pacman", modeMap,
        List.of());

    AuthoringModel mockModel = mock(AuthoringModel.class);
    when(mockController.getModel()).thenReturn(mockModel);
  }


  @Test
  public void setEntityType_InitializesFields() {
    view.setEntityType(mockEntityType);

    VBox root = (VBox) view.getRoot();
    TextField typeField = (TextField) root.getChildren().get(1);
    assertEquals("Pacman", typeField.getText());
  }

}
