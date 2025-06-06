package oogasalad.player.view;

import static oogasalad.engine.utility.LanguageManager.getMessage;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import oogasalad.engine.controller.MainController;
import oogasalad.engine.utility.constants.GameConfig;
import oogasalad.engine.view.components.FormattingUtil;
import oogasalad.networking.GameClient;
import oogasalad.player.controller.LobbyNetworkController;

public class NetworkedGameLobbyView {

  private final VBox myRoot;
  private final MainController myMainController;
  private final String folderPath;

  // Lobby Components
  private TextField ipField;
  private TextField portField;
  private Button createServerButton;
  private Button joinServerButton;
  private Button leaveServerButton;
  private ListView<String> playerStatusList;
  private Button readyButton;
  private boolean isReady = false;
  private Label statusLabel;

  private final LobbyNetworkController lobbyNetworkController = new LobbyNetworkController();
  private FormattingUtils formattingUtils = new FormattingUtils();

  public NetworkedGameLobbyView(MainController controller, String path) {
    this.myMainController = controller;
    this.folderPath = path;
    myRoot = new VBox(15);
    initializeLayout();
    initializeLobbyUI();
  }

  public VBox getRoot() {
    return myRoot;
  }

  private void initializeLayout() {
    myRoot.setPrefSize(GameConfig.WIDTH, GameConfig.HEIGHT);
    myRoot.setAlignment(Pos.TOP_CENTER);
    myRoot.setPadding(new Insets(20));
  }

  private void initializeLobbyUI() {
    VBox topbar = createTopBar();
    HBox connectionFields = createConnectionFields();
    HBox connectionButtons = createConnectionButtons();
    playerStatusList = new ListView<>();
    playerStatusList.setPrefHeight(200);

    readyButton = new Button(getMessage("READY"));
    readyButton.setOnAction(e -> toggleReady());
    readyButton.setDisable(true);
    readyButton.getStyleClass().add("small-button");

    Label ipInfoLabel = new Label("Your IP: " + lobbyNetworkController.getLocalIpAddress());

    statusLabel = new Label();

    myRoot.getChildren().addAll(
        topbar,
        connectionFields,
        connectionButtons,
        ipInfoLabel,
        playerStatusList,
        statusLabel,
        readyButton);
  }

  private VBox createTopBar() {
    Label titleLabel = FormattingUtil.createTitle(getMessage("CREATE_JOIN_GAME_SERVER"));
    titleLabel.setMaxWidth(Double.MAX_VALUE);
    titleLabel.setAlignment(Pos.TOP_CENTER);

    Button backButton = FormattingUtil.createSmallButton(getMessage("BACK_BUTTON"));
    backButton.setOnAction(e -> handleBackButton());
    VBox topBar = new VBox(10, backButton, titleLabel);
    topBar.getStyleClass().add("game-selector-top-bar");
    return topBar;
  }

  private void handleBackButton() {
    myMainController.hideNetworkedLobbyView();
    myMainController.showGameSelectorView();
    handleDisconnect();
  }

  private void handleDisconnect() {
    lobbyNetworkController.disconnect();
    updateUiOnConnectOrDisconnect(lobbyNetworkController.isConnected());
    playerStatusList.getItems().clear();
  }

  private HBox createConnectionFields() {
    ipField = new TextField("127.0.0.1");
    ipField.setPromptText(getMessage("IP_PROMPT"));
    portField = new TextField("8000");
    portField.setPromptText(getMessage("PORT_PROMPT"));
    HBox box = new HBox(10, new Label("IP:"), ipField, new Label("Port:"), portField);
    box.getStyleClass().add("lobby-connection-field");
    return box;
  }

  private HBox createConnectionButtons() {
    createServerButton = new Button(getMessage("CREATE_SERVER_BUTTON"));
    createServerButton.setOnAction(e -> handleCreateServer());
    createServerButton.getStyleClass().add("small-button");
    joinServerButton = new Button(getMessage("JOIN_SERVER_BUTTON"));
    joinServerButton.setOnAction(e -> handleJoinServer());
    joinServerButton.getStyleClass().add("small-button");
    leaveServerButton = new Button(getMessage("LEAVE_SERVER_BUTTON"));
    leaveServerButton.setOnAction(e -> handleDisconnect());
    leaveServerButton.setDisable(true);
    leaveServerButton.getStyleClass().add("small-button");
    HBox box = new HBox(10, createServerButton, joinServerButton, leaveServerButton);
    box.getStyleClass().add("lobby-connection-field");
    return box;
  }

  private void toggleReady() {
    isReady = !isReady;
    readyButton.setText(isReady ? getMessage("CANCEL_READY") : getMessage("READY"));
    GameClient client = lobbyNetworkController.getClient();
    client.setReadyStatus(isReady);
    client.setPlayerStatusListener(this::updatePlayerStatus);
  }

  private void handleCreateServer() {
    String port = portField.getText();
    if (!isValidPort(port)) {
      statusLabel.setText(getMessage("INVALID_PORT"));
      return;
    }
    try {
      int portNumber = Integer.parseInt(port);
      lobbyNetworkController.startServer(portNumber);
      registerStartGameListener(lobbyNetworkController.getClient());
      updateUiOnConnectOrDisconnect(lobbyNetworkController.isConnected());
      statusLabel.setText("Server created on port " + port);
    } catch (Exception e) {
      statusLabel.setText(getMessage("INVALID_PORT"));
    }
  }

  private void handleJoinServer() {
    String ip = ipField.getText();
    String port = portField.getText();
    if (!(isValidIP(ip) && isValidPort(port) && lobbyNetworkController.isServerReachable(ip, Integer.parseInt(port)) )) {
      statusLabel.setText(getMessage("INVALID_IP_PORT"));
      return;
    }

    int portNumber = Integer.parseInt(port);
    lobbyNetworkController.joinServer(ip, portNumber);
    GameClient client = lobbyNetworkController.getClient();
    registerStartGameListener(client);
    updateUiOnConnectOrDisconnect(lobbyNetworkController.isConnected());
    client.setDisconnectListener(() -> {
      setConnectionUiState(false);
      playerStatusList.getItems().clear();
    });

    statusLabel.setText("Attempting to join " + ip + ":" + port);
  }

  private void registerStartGameListener(GameClient client) {
    client.setStartGameListener(() -> {
      Platform.runLater(() -> {
        int myPlayerId = client.getPlayerId();
        var activePlayerIds = client.getActivePlayerIds();

        if (!myMainController.showGamePlayerView(folderPath, false,
            myPlayerId, activePlayerIds)) {
          formattingUtils.showErrorDialog(getMessage("ERROR"), getMessage("CANNOT_LOAD_GAME"));
        } else {
          myMainController.hideNetworkedLobbyView();
        }
      });
    });
  }

  private void updateUiOnConnectOrDisconnect(boolean isConnected) {
    setConnectionUiState(isConnected);
    GameClient client = lobbyNetworkController.getClient();
    if (isConnected && client != null) {
      client.setPlayerStatusListener(this::updatePlayerStatus);
    }
  }

  private void setConnectionUiState(boolean isConnected) {
    createServerButton.setDisable(isConnected);
    joinServerButton.setDisable(isConnected);
    leaveServerButton.setDisable(!isConnected);
    readyButton.setDisable(!isConnected);
    statusLabel.setText(isConnected ? getMessage("CONNECTED") : getMessage("DISCONNECTED"));
  }

  private boolean isValidIP(String ip) {
    return ip != null && !ip.isBlank(); // You can add more advanced validation
  }

  private boolean isValidPort(String port) {
    try {
      int p = Integer.parseInt(port);
      return p > 1024 && p < 65535;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Called when a new player joins or updates ready status
   */
  public void updatePlayerStatus(Map<Integer, Boolean> playerStatusMap) {
    Platform.runLater(() -> {
      playerStatusList.getItems().clear();
      for (Map.Entry<Integer, Boolean> entry : playerStatusMap.entrySet()) {
        String status = entry.getValue() ? "READY" : "NOT READY";
        playerStatusList.getItems().add("Player " + entry.getKey() + ": " + status);
      }
    });
  }
}
