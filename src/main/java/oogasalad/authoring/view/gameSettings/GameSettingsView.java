package oogasalad.authoring.view.gameSettings;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import oogasalad.authoring.controller.AuthoringController;
import oogasalad.authoring.view.CollisionRuleEditorView;
import oogasalad.authoring.view.ViewException;
import oogasalad.engine.config.CollisionRule;
import oogasalad.engine.records.config.model.SettingsRecord;
import oogasalad.engine.records.config.model.losecondition.LoseConditionInterface;
import oogasalad.engine.records.config.model.wincondition.WinConditionInterface;
import oogasalad.engine.utility.LanguageManager;
import oogasalad.engine.utility.LoggingManager;
import oogasalad.engine.utility.ThemeManager;
import oogasalad.engine.view.components.FormattingUtil;

import java.util.List;
import org.apache.logging.log4j.Level;

/**
 * GameSettingsView manages the interface for editing game metadata, settings, win/lose conditions,
 * and cheat codes. It provides functionality to update the view from the model, save user changes
 * back to the model, and open the collision rules editor popup.
 * <p>
 * Organized using a ScrollPane containing a VBox layout inside an HBox root.
 *
 * @author William He, Angela Predolac
 */
public class GameSettingsView {

  private final AuthoringController controller;

  private final HBox rootNode;
  private final MetadataEditor metadataEditor;
  private final SettingsEditor settingsEditor;
  private final ConditionEditor conditionEditor;
  private final CheatCodeEditor cheatCodeEditor;
  private final MultiplayerEditor multiplayerEditor;

  /**
   * Creates a new GameSettingsView for editing game configuration.
   *
   * @param controller the AuthoringController managing the model
   */
  public GameSettingsView(AuthoringController controller) {
    this.controller = controller;
    this.rootNode = new HBox();
    this.rootNode.setSpacing(15);
    this.rootNode.setPadding(new Insets(15));
    this.rootNode.setAlignment(Pos.CENTER_LEFT);

    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setFitToWidth(true);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setPrefViewportHeight(200); // limit height to a compact size like before
    scrollPane.getStyleClass().add("game-settings-scroll-pane");

    VBox contentBox = new VBox(10);
    contentBox.setPadding(new Insets(5));

    Label titleLabel = new Label(LanguageManager.getMessage("GAME_SETTINGS"));
    titleLabel.getStyleClass().add("settings-title");

    metadataEditor = new MetadataEditor(
        controller.getModel().getGameTitle(),
        controller.getModel().getAuthor(),
        controller.getModel().getGameDescription()
    );

    settingsEditor = new SettingsEditor(
        controller.getModel().getDefaultSettings().gameSpeed(),
        controller.getModel().getDefaultSettings().startingLives(),
        controller.getModel().getDefaultSettings().initialScore()
    );

    conditionEditor = new ConditionEditor(
        ConditionLoader.loadConditionClasses(
            "oogasalad.engine.records.config.model.wincondition", WinConditionInterface.class
        ),
        ConditionLoader.loadConditionClasses(
            "oogasalad.engine.records.config.model.losecondition", LoseConditionInterface.class
        )
    );

    cheatCodeEditor = new CheatCodeEditor();

    multiplayerEditor = new MultiplayerEditor();

    HBox buttonBox = createButtonBox();

    GridPane settingsGrid = new GridPane();
    settingsGrid.getStyleClass().add("settings-grid");

    settingsGrid.add(metadataEditor.getNode(), 0, 0, 4, 1);
    settingsGrid.add(settingsEditor.getNode(), 0, 1, 4, 1);
    settingsGrid.add(conditionEditor.getNode(), 0, 2, 4, 1);
    settingsGrid.add(cheatCodeEditor.getNode(), 0, 3, 4, 1);
    settingsGrid.add(multiplayerEditor, 0, 4, 4, 1);

    contentBox.getChildren().addAll(
        titleLabel,
        settingsGrid,
        buttonBox
    );

    scrollPane.setContent(contentBox);
    rootNode.getChildren().add(scrollPane);
  }

  /**
   * Returns the root Node for this view.
   *
   * @return the Node containing the game settings editor
   */
  public Node getNode() {
    return rootNode;
  }

  /**
   * Updates the UI fields to match the current values in the model.
   */
  public void updateFromModel() {
    metadataEditor.update(
        controller.getModel().getGameTitle(),
        controller.getModel().getAuthor(),
        controller.getModel().getGameDescription()
    );
    settingsEditor.update(
        controller.getModel().getDefaultSettings().gameSpeed(),
        controller.getModel().getDefaultSettings().startingLives(),
        controller.getModel().getDefaultSettings().initialScore()
    );
    conditionEditor.update(new ConditionState(
        controller.getModel().getDefaultSettings().winCondition().getConditionType(),
        controller.getModel().getDefaultSettings().winCondition().getConditionValue().orElse(""),
        controller.getModel().getDefaultSettings().loseCondition().getConditionType(),
        String.valueOf(controller.getModel().getDefaultSettings().startingLives()))
    );
    cheatCodeEditor.update(controller.getModel().getDefaultSettings().cheatTypes());
    multiplayerEditor.update(controller.getModel().getDefaultSettings().isMultiplayer());
  }

  /**
   * Saves the current UI values into the model. Displays a confirmation alert upon successful save,
   * or an error alert if saving fails.
   */
  public void saveSettings() {
    controller.getModel().setGameTitle(metadataEditor.getTitle());
    controller.getModel().setAuthor(metadataEditor.getAuthor());
    controller.getModel().setGameDescription(metadataEditor.getDescription());
    try {
      SettingsRecord updatedSettings = new SettingsRecord(
          settingsEditor.getGameSpeed(),
          settingsEditor.getStartingLives(),
          settingsEditor.getInitialScore(),
          conditionEditor.createSelectedWinCondition(),
          conditionEditor.createSelectedLoseCondition(),
          cheatCodeEditor.getSelectedCheats(),
          multiplayerEditor.getIsMultiplayerEnabled()
      );
      controller.getModel().setDefaultSettings(updatedSettings);
    } catch (ViewException ex) {
      Alert errorAlert = new Alert(AlertType.ERROR);
      errorAlert.setTitle(LanguageManager.getMessage("SAVE_ERROR"));
      FormattingUtil.applyStandardDialogStyle(errorAlert);
      errorAlert.showAndWait();
      LoggingManager.LOGGER.log(Level.ERROR, LanguageManager.getMessage("SAVE_ERROR"), ex);
      return;
    }

    Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
    confirmation.setTitle(LanguageManager.getMessage("SAVED"));
    confirmation.setHeaderText(null);
    confirmation.setContentText(LanguageManager.getMessage("GAME_SETTINGS_SAVED"));
    FormattingUtil.applyStandardDialogStyle(confirmation);
    confirmation.showAndWait();
  }

  /**
   * Displays a popup window for editing collision rules.
   */
  public void showCollisionRulesPopup() {
    CollisionRuleEditorView collisionEditor = new CollisionRuleEditorView(controller);
    Dialog<List<CollisionRule>> dialog = collisionEditor.getDialog();
    ThemeManager.getInstance().registerScene(dialog.getDialogPane().getScene());

    collisionEditor.showAndWait().ifPresent(updatedRules ->
        controller.getModel().setCollisionRules(updatedRules)
    );
  }

  /**
   * Creates the HBox containing the Save and Collision Rules buttons.
   *
   * @return the HBox with action buttons
   */
  private HBox createButtonBox() {
    Button saveButton = new Button(LanguageManager.getMessage("SAVE_SETTINGS"));
    saveButton.setOnAction(e -> saveSettings());

    Button collisionRulesButton = new Button(LanguageManager.getMessage("COLLISION_RULES"));
    collisionRulesButton.setOnAction(e -> showCollisionRulesPopup());

    HBox buttonBox = new HBox(15, saveButton, collisionRulesButton);
    buttonBox.setAlignment(Pos.CENTER_LEFT);
    return buttonBox;
  }
}
