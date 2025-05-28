package oogasalad.authoring.view;

import static oogasalad.engine.utility.constants.GameConfig.ELEMENT_SPACING;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import oogasalad.engine.records.config.model.controlConfig.ControlConfigInterface;
import oogasalad.engine.records.config.model.controlConfig.NoneControlConfigRecord;
import oogasalad.engine.records.config.model.controlConfig.targetStrategy.TargetCalculationConfigInterface;
import oogasalad.engine.utility.FileUtility;
import oogasalad.engine.utility.LanguageManager;
import oogasalad.engine.view.components.FormattingUtil;
import oogasalad.player.model.strategies.control.ControlManager;

/**
 * A class that allows the user to select and edit a control type for an entity mode.
 *
 * @author Owen Jennings
 */
public class ControlTypeEditorView {

  private static final String TARGET_CALCULATION_CONFIG = "targetCalculationConfig";
  private static final String PATH_FINDING_STRATEGY = "pathFindingStrategy";
  private final ComboBox<String> controlTypeBox;
  private final VBox controlTypeParameters;
  private final List<TextField> controlTypeParameterFields;
  private final List<TextField> targetStrategyParameterFields;
  private final Map<String, ComboBox<String>> controlTypeComboBoxes = new HashMap<>();
  private final VBox root;

  /**
   * Initialize a control type editor view with the default values for each field.
   */
  public ControlTypeEditorView() {
    root = new VBox();
    root.setSpacing(ELEMENT_SPACING);
    root.setPadding(new Insets(ELEMENT_SPACING));
    root.getStyleClass().add("control-type-editor-view");

    controlTypeBox = new ComboBox<>();
    controlTypeBox.getItems().addAll(ControlManager.getControlStrategies());
    controlTypeBox.setValue(LanguageManager.getMessage("NONE"));
    controlTypeBox.setOnAction(e -> updateControlParameterFields());
    controlTypeBox.setId("control-type-selector");

    controlTypeParameters = new VBox(ELEMENT_SPACING);
    controlTypeParameters.setPrefSize(400, 200);
    controlTypeParameters.setPadding(new Insets(ELEMENT_SPACING));
    controlTypeParameterFields = new ArrayList<>();
    targetStrategyParameterFields = new ArrayList<>();

    ScrollPane controlTypeScrollPane = new ScrollPane(controlTypeParameters);
    controlTypeScrollPane.setFitToWidth(true);

    controlTypeScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    controlTypeScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

    root.getChildren().addAll(
        new Label(LanguageManager.getMessage("CONTROL_STRATEGY")), controlTypeBox,
        controlTypeScrollPane
    );

    populateControlConfigUI(new NoneControlConfigRecord());
  }

  /**
   * Get the root JavaFX element of this view.
   *
   * @return A VBox object containing all the fields of this view.
   */
  public VBox getRoot() {
    return root;
  }


  /**
   * Construct a control config object using the values of the user input into this view.
   *
   * @return A control config interface constructed from this view.
   */
  public ControlConfigInterface getControlConfig() {
    String controlType = controlTypeBox.getValue();
    Map<String, Class<?>> requiredFieldTypes = ControlManager.getControlRequiredFields(controlType);
    List<String> requiredFieldOrder = ControlManager.getControlRequiredFieldsOrder(controlType);
    List<Object> constructorArgs = new ArrayList<>();
    try {
      getConstructorArguments(requiredFieldOrder, requiredFieldTypes, constructorArgs);
      return getControlConfigInterface(controlType, constructorArgs);

    } catch (Exception e) {
      // ✅ FIX 1: Avoid JavaFX dialogs during test mode
      if (Platform.isFxApplicationThread()) {
        showError("Error building control config: " + e.getMessage());
      } else {
        System.err.println("Error building control config: " + e.getMessage());
      }

      // ✅ FIX 2: Still fail test cleanly
      throw new ViewException("Error building control config: ", e);
    }
  }

  private void getConstructorArguments(List<String> requiredFieldOrder,
      Map<String, Class<?>> requiredFieldTypes,
      List<Object> constructorArgs) {
    int textFieldIndex = 0;

    for (String param : requiredFieldOrder) {
      textFieldIndex = extractAllConstructorArguments(requiredFieldTypes, constructorArgs, param,
          textFieldIndex);
    }
  }

  private int extractAllConstructorArguments(Map<String, Class<?>> requiredFieldTypes,
      List<Object> constructorArgs, String param, int textFieldIndex) {
    Class<?> type = requiredFieldTypes.get(param);

    if (param.startsWith(PATH_FINDING_STRATEGY)) {
      constructorArgs.add(controlTypeComboBoxes.get(param).getValue());

    } else if (param.startsWith(TARGET_CALCULATION_CONFIG)) {
      constructorArgs.add(buildTargetStrategyFromUI());

    } else {
      textFieldIndex = extractConstructorArgument(constructorArgs, textFieldIndex, type);
    }
    return textFieldIndex;
  }

  private int extractConstructorArgument(List<Object> constructorArgs, int textFieldIndex,
      Class<?> type) {
    String input = controlTypeParameterFields.get(textFieldIndex++).getText();
    // ✅ Handle empty input for nullable types like Integer
    if (input.isEmpty()) {
      constructorArgs.add(null);
    } else {
      constructorArgs.add(FileUtility.castInputToCorrectType(input, type));
    }
    return textFieldIndex;
  }

  private static ControlConfigInterface getControlConfigInterface(String controlType,
      List<Object> constructorArgs)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
    String fullClassName =
        "oogasalad.engine.records.config.model.controlConfig." + controlType
            + "ControlConfigRecord";
    Class<?> configClass = Class.forName(fullClassName);
    Constructor<?> constructor = configClass.getDeclaredConstructors()[0];

    return (ControlConfigInterface) constructor.newInstance(constructorArgs.toArray());
  }

  /**
   * Populate the control config user interface with the values in this provided configuration
   * interface.
   *
   * @param config The config you want to use to populate this view.
   */
  public void populateControlConfigUI(ControlConfigInterface config) {
    String type = config.getClass().getSimpleName().replace("ControlConfigRecord", "");
    controlTypeBox.setValue(type);

    controlTypeParameters.getChildren().clear();
    controlTypeParameterFields.clear();
    targetStrategyParameterFields.clear();
    controlTypeComboBoxes.clear();

    ControlManager.getControlRequiredFields(type);
    List<String> fieldOrder = ControlManager.getControlRequiredFieldsOrder(type);

    for (String field : fieldOrder) {
      Label label = new Label(field + ":");

      if (field.startsWith(PATH_FINDING_STRATEGY)) {
        ComboBox<String> combo = new ComboBox<>();
        combo.setItems(
            FXCollections.observableArrayList(ControlManager.getPathFindingStrategies()));
        combo.setValue(getFieldValueFromConfig(config, field));
        controlTypeComboBoxes.put(field, combo);
        controlTypeParameters.getChildren().add(new VBox(label, combo));

      } else if (field.startsWith(TARGET_CALCULATION_CONFIG)) {
        ComboBox<String> targetCombo = new ComboBox<>();
        targetCombo.setItems(
            FXCollections.observableArrayList(ControlManager.getTargetCalculationStrategies()));
        String selected = getStrategyValueFromConfig(config, field);
        targetCombo.setValue(selected);
        controlTypeComboBoxes.put(field, targetCombo);

        VBox targetParamsBox = new VBox(5);

        TargetCalculationConfigInterface strategyConfig = getTargetStrategyFromConfig(config,
            field);
        updateTargetParameterFields(targetCombo, targetParamsBox, strategyConfig);

        targetCombo.setOnAction(e -> updateTargetParameterFields(targetCombo, targetParamsBox));

        controlTypeParameters.getChildren().add(new VBox(label, targetCombo, targetParamsBox));

      } else {
        String value = getFieldValueFromConfig(config, field);
        TextField tf = FormattingUtil.createTextField(value);
        controlTypeParameterFields.add(tf);
        controlTypeParameters.getChildren().add(new VBox(label, tf));
      }
    }
  }


  private void updateControlParameterFields() {
    controlTypeParameters.getChildren().clear();
    controlTypeParameterFields.clear();

    VBox parameterBox = new VBox(5);
    List<String> requiredFields =
        ControlManager.getControlRequiredFieldsOrder(controlTypeBox.getValue());
    for (String parameter : requiredFields) {
      Node parameterNode = createParameterNode(parameter);
      parameterBox.getChildren().add(parameterNode);
    }
    controlTypeParameters.getChildren().add(parameterBox);
  }


  private Node createParameterNode(String parameter) {
    Label parameterLabel = new Label(parameter + ": ");

    if (parameter.startsWith(PATH_FINDING_STRATEGY)) {
      return createPathFindingStrategyNode(parameterLabel, parameter);

    } else if (parameter.startsWith(TARGET_CALCULATION_CONFIG)) {
      return createTargetCalculationConfigNode(parameterLabel, parameter);

    } else {
      return createGenericParameterNode(parameterLabel);
    }
  }


  private Node createPathFindingStrategyNode(Label parameterLabel, String parameter) {
    ComboBox<String> pathStrategyBox = new ComboBox<>();
    pathStrategyBox.getItems().addAll(ControlManager.getPathFindingStrategies());
    pathStrategyBox.setId("path-finding-combo");
    controlTypeComboBoxes.put(parameter, pathStrategyBox);

    VBox container = new VBox(ELEMENT_SPACING);
    container.setPadding(new Insets(ELEMENT_SPACING));
    container.getChildren().addAll(parameterLabel, pathStrategyBox);
    return container;
  }

  private Node createTargetCalculationConfigNode(Label parameterLabel, String parameter) {
    ComboBox<String> targetStrategyDropdown = new ComboBox<>();
    targetStrategyDropdown.getItems().addAll(ControlManager.getTargetCalculationStrategies());
    targetStrategyDropdown.setId("target-calculation-combo");
    controlTypeComboBoxes.put(parameter, targetStrategyDropdown);

    VBox targetParameterBox = new VBox(5);
    targetStrategyDropdown.setOnAction(
        e -> updateTargetParameterFields(targetStrategyDropdown, targetParameterBox));

    VBox container = new VBox(ELEMENT_SPACING);
    container.setPadding(new Insets(ELEMENT_SPACING));
    container.getChildren().addAll(parameterLabel, targetStrategyDropdown, targetParameterBox);
    return container;
  }

  private void updateTargetParameterFields(ComboBox<String> dropdown, VBox targetParameterBox,
      TargetCalculationConfigInterface config) {
    // ChatGPT generated this method.
    targetParameterBox.getChildren().clear();
    String selectedStrategy = dropdown.getValue();
    List<String> targetParams = ControlManager.getTargetRequiredFieldsOrder(selectedStrategy);

    targetStrategyParameterFields.clear();

    generateTargetParameterTextFields(targetParameterBox, config, targetParams);
  }

  private void generateTargetParameterTextFields(VBox targetParameterBox,
      TargetCalculationConfigInterface config,
      List<String> targetParams) {
    createAllTargetParameterFieldsFromConfig(targetParameterBox, config, targetParams);
  }

  private void createAllTargetParameterFieldsFromConfig(VBox targetParameterBox,
      TargetCalculationConfigInterface config,
      List<String> targetParams) {
    if (!targetParams.isEmpty()) {
      for (String targetParam : targetParams) {
        createTextFieldForCurrentParameter(targetParameterBox, config, targetParam);
      }
    }
  }

  private void createTextFieldForCurrentParameter(VBox targetParameterBox,
      TargetCalculationConfigInterface config,
      String targetParam) {
    Label targetParamLabel = new Label(targetParam + ": ");
    TextField targetParamField = FormattingUtil.createTextField();
    attemptSettingTargetParameterFieldValue(config, targetParam, targetParamField);
    targetStrategyParameterFields.add(targetParamField);
    targetParameterBox.getChildren().addAll(targetParamLabel, targetParamField);
  }

  private static void attemptSettingTargetParameterFieldValue(
      TargetCalculationConfigInterface config, String targetParam,
      TextField targetParamField) {
    if (config != null) {
      try {
        var field = config.getClass().getDeclaredField(targetParam);
        field.setAccessible(true);
        Object value = field.get(config);
        targetParamField.setText(value != null ? value.toString() : "");
      } catch (Exception ignored) {
      }
    }
  }


  private void updateTargetParameterFields(ComboBox<String> dropdown, VBox targetParameterBox) {
    targetParameterBox.getChildren().clear();
    String selectedStrategy = dropdown.getValue();
    targetStrategyParameterFields.clear();
    List<String> targetParams = ControlManager.getTargetRequiredFieldsOrder(selectedStrategy);

    if (!targetParams.isEmpty()) {
      for (String targetParam : targetParams) {
        Label targetParamLabel = new Label(targetParam + ": ");
        TextField targetParamField = FormattingUtil.createTextField();
        targetParamField.setId("field-" + targetParam);
        targetStrategyParameterFields.add(targetParamField);
        targetParameterBox.getChildren().addAll(targetParamLabel, targetParamField);
      }
    }
  }

  private Node createGenericParameterNode(Label parameterLabel) {
    TextField parameterField = FormattingUtil.createTextField();
    controlTypeParameterFields.add(parameterField);

    VBox container = new VBox(ELEMENT_SPACING);
    container.setPadding(new Insets(ELEMENT_SPACING));
    container.getChildren().addAll(parameterLabel, parameterField);
    return container;
  }

  private String getFieldValueFromConfig(ControlConfigInterface config, String fieldName) {
    try {
      var field = config.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      Object value = field.get(config);
      return value != null ? value.toString() : "";
    } catch (Exception e) {
      return "";
    }
  }

  private String getStrategyValueFromConfig(ControlConfigInterface config, String fieldName) {
    try {
      var field = config.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      Object strategy = field.get(config);
      return strategy != null ? strategy.getClass().getSimpleName().replace("ConfigRecord", "")
          : "";
    } catch (Exception e) {
      return "";
    }
  }

  private TargetCalculationConfigInterface getTargetStrategyFromConfig(
      ControlConfigInterface config, String fieldName) {
    try {
      var field = config.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return (TargetCalculationConfigInterface) field.get(config);
    } catch (Exception e) {
      return null;
    }
  }


  private TargetCalculationConfigInterface buildTargetStrategyFromUI() throws ViewException {
    try {
      String selectedStrategy = controlTypeComboBoxes.get(TARGET_CALCULATION_CONFIG).getValue();
      Map<String, Class<?>> requiredTypes = ControlManager.getTargetRequiredFields(
          selectedStrategy);
      List<String> fieldOrder = ControlManager.getTargetRequiredFieldsOrder(selectedStrategy);

      List<Object> paramValues = new ArrayList<>();
      int index = 0;
      for (String param : fieldOrder) {
        String input = targetStrategyParameterFields.get(index++).getText();
        paramValues.add(FileUtility.castInputToCorrectType(input, requiredTypes.get(param)));
      }

      String fullClassName =
          "oogasalad.engine.records.config.model.controlConfig.targetStrategy." + selectedStrategy
              + "ConfigRecord";
      Class<?> strategyClass = Class.forName(fullClassName);
      Constructor<?> constructor = strategyClass.getDeclaredConstructors()[0];

      return (TargetCalculationConfigInterface) constructor.newInstance(paramValues.toArray());
    } catch (ViewException e) {
      throw e; // Don't show error duplicate error in ui.
    } catch (Exception e) {
      showError("Error building target strategy: " + e.getMessage());
      throw new ViewException("Error building target strategy: ", e);
    }
  }

  private void showError(String msg) {
    Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
    FormattingUtil.applyStandardDialogStyle(alert);
    alert.showAndWait();
  }

}
