package oogasalad.player.view;

import static oogasalad.engine.utility.LanguageManager.getMessage;
import static oogasalad.engine.utility.constants.GameConfig.ELEMENT_SPACING;
import static oogasalad.engine.utility.constants.GameConfig.HEIGHT;
import static oogasalad.engine.utility.constants.GameConfig.WIDTH;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import oogasalad.engine.config.JsonConfigParser;
import oogasalad.engine.controller.MainController;
import oogasalad.engine.exceptions.ConfigException;
import oogasalad.engine.records.config.GameConfigRecord;
import oogasalad.engine.utility.FileUtility;
import oogasalad.engine.utility.LanguageManager;
import oogasalad.engine.utility.LoggingManager;
import oogasalad.engine.utility.ThemeManager;
import oogasalad.engine.utility.constants.GameConfig;
import oogasalad.engine.view.components.FormattingUtil;

/**
 * The game selector splash screen after game player button on splash screen is clicked.
 *
 * @author Austin Huang
 */
public class GameSelectorView {

  private static final String GAMES_FOLDER_PATH = "data/games/";
  public static final int GAME_CARD_WIDTH = 200;

  private final VBox myRoot;
  private final MainController myMainController;
  private final Map<String, String> gameNameToFolder = new HashMap<>();
  private final List<GameConfigRecord> gameConfigRecords;

  private final JsonConfigParser configParser = new JsonConfigParser();
  private final FileChooser fileChooser = new FileChooser();
  private final FormattingUtils formattingUtils = new FormattingUtils();

  private Label fileLabel;
  private Button startButton;

  /**
   * The GameSelectorView class is responsible for displaying the game selection interface in the
   * application. It initializes the layout, loads game configurations, and creates a user interface
   * for selecting and uploading game files.
   *
   * @param mainController the main controller of the application, used to manage interactions and
   *                       retrieve the primary stage for theming
   */
  public GameSelectorView(MainController mainController) {
    this.myMainController = mainController;
    this.myRoot = new VBox(20);
    myRoot.setPrefSize(WIDTH, HEIGHT);
    myRoot.getStyleClass().add("game-selector-view");
    myRoot.setPadding(new Insets(ELEMENT_SPACING * 2, ELEMENT_SPACING * 4, ELEMENT_SPACING * 2,
        ELEMENT_SPACING * 4));

    ThemeManager.getInstance().registerScene(mainController.getStage().getScene());

    initializeLayout();

    gameConfigRecords = loadGameConfigs();

    List<String> gameNames = gameConfigRecords.stream()
        .map(config -> config.metadata().gameTitle())
        .toList();

    Pagination gameGrid = createGamePagination(gameNames);

    myRoot.getChildren().addAll(createTopBar(), createFileUploadSection(), gameGrid);
  }


  /**
   * Retrieves the root VBox of the GameSelectorView.
   *
   * @return the root VBox containing the UI elements of the GameSelectorView.
   */
  public VBox getRoot() {
    return myRoot;
  }

  /**
   * Resets the upload section of the game selector view. This method updates the file label to
   * indicate that no file is selected and disables the start button to prevent further actions
   * until a file is uploaded.
   */
  public void resetUploadSection() {
    fileLabel.setText(getMessage("NO_FILE_SELECTED"));
    startButton.setDisable(true);
  }

  private void initializeLayout() {
    myRoot.setPrefSize(GameConfig.WIDTH, GameConfig.HEIGHT);
    myRoot.setAlignment(Pos.CENTER);
  }

  private VBox createTopBar() {
    Label titleLabel = FormattingUtil.createTitle(getMessage("GAME_PLAYER"));
    titleLabel.setMaxWidth(Double.MAX_VALUE);
    titleLabel.setAlignment(Pos.TOP_CENTER);

    Button backButton = FormattingUtil.createSmallButton(getMessage("BACK_BUTTON"));
    backButton.setOnAction(e -> {
      myMainController.hideGameSelectorView();
      myMainController.showSplashScreen();
    });
    VBox topBar = new VBox(10, backButton, titleLabel);
    topBar.getStyleClass().add("game-selector-top-bar");
    return topBar;
  }

  private HBox createFileUploadSection() {
    Button uploadButton = FormattingUtil.createSmallButton(LanguageManager.getMessage("UPLOAD"));

    startButton = FormattingUtil.createSmallButton(LanguageManager.getMessage("START"));
    startButton.setDisable(true);

    fileLabel = new Label(LanguageManager.getMessage("NO_FILE_SELECTED"));

    fileChooser.setTitle(LanguageManager.getMessage("OPEN_FILE"));
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));

    uploadButton.setOnAction(e -> handleFileUpload());
    startButton.setOnAction(e -> startGameFromUpload());

    HBox section = new HBox(10, uploadButton, startButton, fileLabel);
    section.getStyleClass().add("game-selector-file-upload");
    return section;
  }

  private Pagination createGamePagination(List<String> gameNames) {
    int itemsPerPage = 4;
    int totalPages = (int) Math.ceil((double) gameNames.size() / itemsPerPage);

    Pagination pagination = new Pagination(totalPages, 0);
    pagination.setPageFactory(pageIndex -> {
      VBox pageBox = new VBox(10);
      pageBox.setAlignment(Pos.CENTER);

      if (gameNames.isEmpty()) {
        Label emptyLabel = new Label(LanguageManager.getMessage("NO_GAMES_AVAILABLE"));
        pageBox.getChildren().add(emptyLabel);
        return pageBox;
      }

      int start = pageIndex * itemsPerPage;
      int end = Math.min(start + itemsPerPage, gameNames.size());
      List<String> pageGames = gameNames.subList(start, end);

      FlowPane gameGrid = new FlowPane();
      gameGrid.setHgap(20);
      gameGrid.setVgap(20);
      gameGrid.setPrefWrapLength(WIDTH - 4 * ELEMENT_SPACING); // Allow wrapping
      gameGrid.setAlignment(Pos.CENTER);

      for (String name : pageGames) {
        VBox gameCard = createGameCard(name);
        gameCard.setPrefWidth(GAME_CARD_WIDTH);
        gameGrid.getChildren().add(gameCard);
      }

      pageBox.getChildren().add(gameGrid);
      return pageBox;
    });

    return pagination;
  }

  VBox createGameCard(String gameName) {
    VBox card = new VBox(10);
    card.setAlignment(Pos.CENTER);
    card.getStyleClass().add("game-card");

    ImageView image = new ImageView(getGameImage(gameName));
    image.setFitWidth(GAME_CARD_WIDTH);
    image.setFitHeight(300);

    Label nameLabel = FormattingUtil.createHeading(gameName);
    nameLabel.setWrapText(true);
    nameLabel.setMaxWidth(GAME_CARD_WIDTH);

    Button randomizeButton = FormattingUtil.createSmallButton(
        LanguageManager.getMessage("RANDOMIZE"));
    randomizeButton.setWrapText(true);
    randomizeButton.setMaxWidth(GAME_CARD_WIDTH);
    randomizeButton.setOnAction(
        e -> attemptShowingGamePlayerView(gameNameToFolder.get(gameName), true));

    Button infoButton = new Button("i");
    infoButton.getStyleClass().add("icon-button");
    infoButton.setOnAction(e -> showMetadataPopup(gameName));

    HBox buttonBox = new HBox(5, randomizeButton, infoButton);
    buttonBox.setAlignment(Pos.CENTER);

    card.getChildren().addAll(image, nameLabel, buttonBox);
    card.setOnMouseClicked(
        e -> attemptShowingGamePlayerView(gameNameToFolder.get(gameName), false));

    return card;
  }

  private void attemptShowingGamePlayerView(String path, boolean randomize) {
    boolean isMultiplayer = false;
    try {
      GameConfigRecord config = configParser.loadGameConfig(path + "/gameConfig.json");
      isMultiplayer = config.settings().isMultiplayer();
    } catch (ConfigException e) {
      LoggingManager.LOGGER.warn("Could not load config: {}", path, e);
    }
    if (isMultiplayer) {
      myMainController.showNetworkedGameLobbyView(path);
      myMainController.hideGameSelectorView();
    }
    else if (!myMainController.showGamePlayerView(path, randomize)) {
      formattingUtils.showErrorDialog(getMessage("ERROR"), getMessage("CANNOT_LOAD_GAME"));
    } else {
      myMainController.hideGameSelectorView();
    }
  }

  private void showMetadataPopup(String gameName) {
    GameConfigRecord config = gameConfigRecords.stream()
        .filter(g -> g.metadata().gameTitle().equals(gameName))
        .findFirst()
        .orElse(null);

    if (config == null) {
      formattingUtils.showErrorDialog("Error", "Game configuration not found.");
      return;
    }

    Alert infoDialog = new Alert(AlertType.INFORMATION);
    infoDialog.setTitle(LanguageManager.getMessage("GAME_INFO"));
    infoDialog.setHeaderText(gameName);
    infoDialog.setContentText(String.format(
        getMessage("INFO_DIALOG_MESSAGE"),
        config.metadata().author(),
        config.metadata().gameDescription()
    ));

    FormattingUtil.applyStandardDialogStyle(infoDialog);

    infoDialog.showAndWait();
  }


  private List<GameConfigRecord> loadGameConfigs() {
    List<String> folderNames = FileUtility.getFolderNamesInDirectory(GAMES_FOLDER_PATH);
    List<GameConfigRecord> configs = new ArrayList<>();
    String currentPath = System.getProperty("user.dir");

    for (String folder : folderNames) {
      try {
        String filePath = currentPath + "/" + GAMES_FOLDER_PATH + folder;
        GameConfigRecord config = configParser.loadGameConfig(
            filePath + "/gameConfig.json");
        configs.add(config);
        gameNameToFolder.put(config.metadata().gameTitle(), filePath);
      } catch (ConfigException e) {
        LoggingManager.LOGGER.warn("Could not load config: {}", folder, e);
      }
    }
    return configs;
  }

  private Image getGameImage(String gameName) {
    GameConfigRecord config = gameConfigRecords.stream()
        .filter(g -> g.metadata().gameTitle().equals(gameName))
        .findFirst()
        .orElse(null);

    String folderName = gameNameToFolder.get(gameName);
    try {
      if (config != null && config.metadata().image() != null) {
        return new Image(
            new FileInputStream(folderName + "/" + config.metadata().image()));
      }
    } catch (Exception e) {
      LoggingManager.LOGGER.warn("Failed to load image for: {}", gameName);
    }
    return new Image(
        Objects.requireNonNull(getClass().getResourceAsStream("/assets/images/placeholder.png")));
  }

  private void handleFileUpload() {
    File selectedFile = fileChooser.showOpenDialog(null);
    if (selectedFile != null) {
      String strippedPath = getConvertedFilePathFromFile(selectedFile);
      fileLabel.setText(strippedPath);
      startButton.setDisable(false);
    }
  }

  private static String getConvertedFilePathFromFile(File selectedFile) {
    String path = selectedFile.getAbsolutePath();
    return path.substring(0, path.lastIndexOf("/gameConfig.json"));
  }

  private void startGameFromUpload() {
    try {
      attemptShowingGamePlayerView(fileLabel.getText(), false);
    } catch (Exception e) {
      LoggingManager.LOGGER.error("Exception: {}", e.getMessage());
      formattingUtils.showErrorDialog(getMessage("ERROR"), e.getMessage());
    }
  }
}

