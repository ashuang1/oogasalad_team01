package oogasalad.engine.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oogasalad.engine.config.api.ConfigParserInterface;
import oogasalad.engine.exceptions.ConfigException;
import oogasalad.engine.exceptions.ConstantsManagerException;
import oogasalad.engine.records.config.CollisionConfigRecord;
import oogasalad.engine.records.config.ConfigModelRecord;
import oogasalad.engine.records.config.EntityConfigRecord;
import oogasalad.engine.records.config.GameConfigRecord;
import oogasalad.engine.records.config.ImageConfigRecord;
import oogasalad.engine.records.config.ModeConfigRecord;
import oogasalad.engine.records.config.model.*;
import oogasalad.engine.records.config.model.controlConfig.ControlConfigInterface;
import oogasalad.engine.records.config.model.losecondition.LoseConditionInterface;
import oogasalad.engine.records.config.model.wincondition.WinConditionInterface;
import oogasalad.engine.records.model.ConditionRecord;
import oogasalad.engine.records.model.EntityTypeRecord;
import oogasalad.engine.records.model.MapInfoRecord;
import oogasalad.engine.records.model.MetaDataRecord;
import oogasalad.engine.records.model.ModeChangeEventRecord;
import oogasalad.engine.utility.ConstantsManager;
import oogasalad.engine.utility.FileUtility;
import oogasalad.engine.utility.LoggingManager;

/**
 * The {@code JsonConfigParser} class is responsible for parsing game configuration files in JSON
 * format and converting them into {@link GameConfigRecord} objects. It uses the Jackson library to
 * handle JSON parsing and mapping.
 *
 * <p>
 * This class implements the {@link ConfigParserInterface} interface and provides methods to load
 * configuration data from a file, merge settings, and extract folder paths.
 *
 * <p>
 * Example usage:
 *
 * <pre>
 * JsonConfigParser parser = new JsonConfigParser();
 * GameConfig config = parser.loadFromFile("path/to/config.json");
 * </pre>
 *
 * @author Jessica Chen
 */
public class JsonConfigParser implements ConfigParserInterface {

  public static final String ENTITY_TYPE = "entityType";
  public static final String MODE_CHANGE = "modeChangeInfo";
  private final ObjectMapper mapper;
  private Map<String, EntityConfigRecord> entityMap;
  private final Map<String, EntityTypeRecord> entityTypeMap = new HashMap<>();


  private static final String JSON_IDENTIFIER = ".json";

  /**
   * Constructs a new JsonConfigParser instance and initializes the ObjectMapper used for parsing
   * JSON configurations.
   */
  public JsonConfigParser() {
    this.mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
  }

  /**
   * Loads a {@link ConfigModelRecord} from a JSON file at the specified file path.
   *
   * <p>
   * After deserializing the file, this method also resolves the entity types of each
   * {@link EntityPlacement} by matching their type strings to defined {@link EntityTypeRecord}s.
   *
   * @param filepath the path to the JSON configuration file to be loaded
   * @return the fully populated {@link ConfigModelRecord} object
   * @throws ConfigException if the file is missing or cannot be parsed correctly
   */
  public ConfigModelRecord loadFromFile(String filepath) throws ConfigException {

    String gameFolderBasePath = new File(filepath).getParent();

    // Step 1: Load primary game config JSON (e.g., gameConfig.json)
    GameConfigRecord gameConfig = loadGameConfig(filepath);

    // Step 2: Load entities from the entity folder
    entityMap = constructEntities(gameConfig.gameFolderPath());

    // Step 3: Extract basic metadata
    MetaDataRecord metaData = extractMetaData(gameConfig);

    // Step 4: Build all entity types
    List<EntityTypeRecord> entityTypes = createEntityTypes();

    // Step 5: Parse level entity placements + map info
    List<ParsedLevelRecord> levels = new ArrayList<>();
    List<MapInfoRecord> mapInfos = new ArrayList<>();
    String levelFolderPath;

    try {
      levelFolderPath =
          gameConfig.gameFolderPath() + ConstantsManager.getMessage("Config", "MAPS_FOLDER");
    } catch (ConstantsManagerException e) {
      throw new ConfigException("Error in getting maps folder from constants:", e);
    }

    for (LevelRecord level : gameConfig.levels()) {
      ParsedLevelRecord parsed = loadLevelConfig(
          levelFolderPath + level.levelMap() + JSON_IDENTIFIER);
      levels.add(parsed);
      mapInfos.add(parsed.mapInfo());
    }

    // Step 6: Create game settings with merged defaults and level-specific map info
    SettingsRecord settings = createGameSettings(gameConfig);

    // Step 7: Parse collision rules and win condition
    List<CollisionRule> collisionRules = convertToCollisionRules(gameConfig);
    WinConditionInterface winCondition = gameConfig.settings().winCondition();
    LoseConditionInterface loseCondition = gameConfig.settings().loseCondition();
    Map<String, Double> respawnableEntities = new HashMap<>();
    if (gameConfig.respawnableEntities() != null) {
      respawnableEntities = gameConfig.respawnableEntities();
    }

    // Step 8: Get current level from gameConfig
    int currentLevel = gameConfig.currentLevelIndex();
    // Step 9: Return the full config model using the first level only for now
    return new ConfigModelRecord(metaData, settings, entityTypes, levels, collisionRules,
        winCondition, loseCondition, currentLevel, respawnableEntities);
  }

  private ParsedLevelRecord loadLevelConfig(String filepath) throws ConfigException {
    try {
      JsonNode root = mapper.readTree(new File(filepath));
      MapInfoRecord mapInfo = mapper.treeToValue(root.get("mapInfo"), MapInfoRecord.class);

      Map<Integer, EntityTypeRecord> idToEntityType = buildEntityMappings(
          root.get("entityMappings"));
      Map<Integer, String> idToEntityName = buildEntityNames(root.get("entityMappings"));

      List<EntityPlacement> placements = parseLayout(root.get("layout"), idToEntityType,
          idToEntityName);

      List<SpawnEventRecord> spawnEvents = parseSpawnEvents(root, idToEntityType);
      List<ModeChangeEventRecord> modeChangeEvents = parseModeChangeEvents(root, idToEntityType);

      return new ParsedLevelRecord(placements, mapInfo, spawnEvents, modeChangeEvents);
    } catch (IOException e) {
      throw new ConfigException("Error in loading level config", e);
    }
  }

  private List<SpawnEventRecord> parseSpawnEvents(JsonNode rootNode,
      Map<Integer, EntityTypeRecord> idToEntityType)
      throws ConfigException {
    // ChatGPT generated the code for this method.
    List<SpawnEventRecord> spawnEvents = new ArrayList<>();
    JsonNode eventsNode = rootNode.get("spawnEvents");

    for (JsonNode eventNode : eventsNode) {
      int id = Integer.parseInt(eventNode.get(ENTITY_TYPE).asText()); // old: "8"
      EntityTypeRecord type = idToEntityType.get(id);
      if (type == null) {
        throw new ConfigException("Unknown entity ID in spawnEvents: " + id);
      }

      double x = eventNode.get("x").asDouble();
      double y = eventNode.get("y").asDouble();
      String mode = eventNode.get("mode").asText();
      ConditionRecord spawnCondition = parseCondition(eventNode.get("spawnCondition"));
      ConditionRecord despawnCondition = parseCondition(eventNode.get("despawnCondition"));

      spawnEvents.add(new SpawnEventRecord(type, spawnCondition, x, y, mode, despawnCondition));
    }

    return spawnEvents;
  }

  private List<ModeChangeEventRecord> parseModeChangeEvents(JsonNode rootNode,
      Map<Integer, EntityTypeRecord> idToEntityType) throws ConfigException {

    List<ModeChangeEventRecord> modeChangeEvents = new ArrayList<>();
    JsonNode eventsNode = rootNode.get("modeChangeEvents");

    for (JsonNode eventNode : eventsNode) {
      int id = Integer.parseInt(eventNode.get(ENTITY_TYPE).asText());
      EntityTypeRecord type = idToEntityType.get(id);
      if (type == null) {
        throw new ConfigException("Unknown entity ID in modeChangeEvents: " + id);
      }
      ModeChangeInfo changeInfo = new ModeChangeInfo(
          eventNode.get(MODE_CHANGE).get("originalMode").asText(),
          eventNode.get(MODE_CHANGE).get("transitionMode").asText(),
          eventNode.get(MODE_CHANGE).get("revertTime").asInt(),
          eventNode.get(MODE_CHANGE).get("transitionTime").asInt());

      ConditionRecord changeCondition = parseCondition(eventNode.get("changeCondition"));

      modeChangeEvents.add(new ModeChangeEventRecord(type, changeInfo, changeCondition));
    }

    return modeChangeEvents;
  }


  private ConditionRecord parseCondition(JsonNode conditionNode) {
    if (isNullOrMissing(conditionNode)) {
      return defaultCondition();
    }

    String type = getConditionType(conditionNode);
    Map<String, Object> parameters = extractParameters(conditionNode.get("parameters"));

    return new ConditionRecord(type, parameters);
  }

  private boolean isNullOrMissing(JsonNode node) {
    return node == null || node.isNull();
  }

  private ConditionRecord defaultCondition() {
    LoggingManager.LOGGER.warn("Condition is missing or incomplete. Using default 'Always'.");
    return new ConditionRecord("Always", Map.of());
  }

  private String getConditionType(JsonNode conditionNode) {
    JsonNode typeNode = conditionNode.get("type");
    if (isNullOrMissing(typeNode)) {
      LoggingManager.LOGGER.warn("Condition is missing a 'type' field. Using default 'Always'.");
      return "Always";
    }
    return typeNode.asText();
  }

  private Map<String, Object> extractParameters(JsonNode paramNode) {
    Map<String, Object> parameters = new HashMap<>();
    if (paramNode != null && paramNode.isObject()) {
      for (Iterator<Entry<String, JsonNode>> it = paramNode.fields(); it.hasNext(); ) {
        Map.Entry<String, JsonNode> entry = it.next();
        parameters.put(entry.getKey(), parseValue(entry.getValue()));
      }
    }
    return parameters;
  }

  private Object parseValue(JsonNode valueNode) {
    if (valueNode.isInt()) {
      return valueNode.asInt();
    }
    if (valueNode.isDouble()) {
      return valueNode.asDouble();
    }
    if (valueNode.isBoolean()) {
      return valueNode.asBoolean();
    }
    return valueNode.asText();
  }


  private Map<Integer, EntityTypeRecord> buildEntityMappings(JsonNode mappings)
      throws ConfigException {
    Map<Integer, EntityTypeRecord> idToType = new HashMap<>();
    for (JsonNode node : mappings) {
      int id = node.get("id").asInt();
      String name = node.get("entity").asText();
      EntityTypeRecord type = entityTypeMap.get(name);
      if (type == null) {
        throw new ConfigException("EntityType for '" + name + "' not found.");
      }
      idToType.put(id, type);
    }
    return idToType;
  }

  private Map<Integer, String> buildEntityNames(JsonNode mappings) {
    Map<Integer, String> idToName = new HashMap<>();
    for (JsonNode node : mappings) {
      idToName.put(node.get("id").asInt(), node.get("entity").asText());
    }
    return idToName;
  }

  private List<EntityPlacement> parseLayout(
      JsonNode layout,
      Map<Integer, EntityTypeRecord> idToType,
      Map<Integer, String> idToName
  ) {
    List<EntityPlacement> placements = new ArrayList<>();

    for (int y = 0; y < layout.size(); y++) {
      String[] rowEntries = layout.get(y).asText().trim().split("\\s+");
      for (int x = 0; x < rowEntries.length; x++) {
        String entry = rowEntries[x];
        if (!entry.equals("0")) {
          placements.add(parseEntityPlacement(entry, x, y, idToType, idToName));
        }
      }
    }

    return placements;
  }

  private EntityPlacement parseEntityPlacement(
      String entry,
      int x,
      int y,
      Map<Integer, EntityTypeRecord> idToType,
      Map<Integer, String> idToName
  ) {
    String[] parts = entry.split("\\.");
    int entityId = Integer.parseInt(parts[0]);
    int modeIndex = (parts.length > 1) ? Integer.parseInt(parts[1]) : 0;

    EntityTypeRecord type = idToType.get(entityId);
    String entityName = idToName.get(entityId);
    String modeName = resolveMode(entityMap.get(entityName), List.of(modeIndex));

    return new EntityPlacement(type, x, y, modeName);
  }

  // Methods to convert from multiple config files to a singular config model

  private MetaDataRecord extractMetaData(GameConfigRecord gameConfig) {
    return new MetaDataRecord(
        gameConfig.metadata().gameTitle(),
        gameConfig.metadata().author(),
        gameConfig.metadata().gameDescription());
  }

  private SettingsRecord createGameSettings(GameConfigRecord gameConfig) {
    SettingsRecord baseSettings = gameConfig.settings();

    return new SettingsRecord(
        baseSettings.gameSpeed(),
        baseSettings.startingLives(),
        baseSettings.initialScore(),
        baseSettings.winCondition(),
        baseSettings.loseCondition(),
        baseSettings.cheatTypes(),
        baseSettings.isMultiplayer()
    );
  }


  private List<CollisionRule> convertToCollisionRules(GameConfigRecord gameConfig) {
    List<CollisionRule> collisionRules = new ArrayList<>();

    for (CollisionConfigRecord collision : gameConfig.collisions()) {
      EntityConfigRecord entityA = entityMap.get(collision.entityA());
      EntityConfigRecord entityB = entityMap.get(collision.entityB());

      if (entityA == null) {
        LoggingManager.LOGGER.warn(
            "Unable to find entityA in the configuration file for the collision strategy.");
      }
      if (entityB == null) {
        LoggingManager.LOGGER.warn(
            "Unable to find entityB in the configuration file for the collision strategy.");
      }

      String modeA = collision.modeA();
      String modeB = collision.modeB();

      collisionRules.add(new CollisionRule(
          entityA.name(), modeA, entityB.name(), modeB,
          collision.eventsA(), collision.eventsB()));
    }

    return collisionRules;
  }

  private String resolveMode(EntityConfigRecord entity, List<Integer> modeList) {
    if (modeList == null || modeList.isEmpty()) {
      return "Any";   // default behave if no specified mode
    }
    int modeIdx = modeList.getFirst();
    if (entity == null || entity.modes() == null || modeIdx >= entity.modes().size()) {
      LoggingManager.LOGGER.warn("Unable to resolve mode. For config: {}", entity);
      return "Any";
    }
    return entity.modes().get(modeIdx).name();
  }


  private List<EntityTypeRecord> createEntityTypes() {
    List<EntityTypeRecord> entityTypes = new ArrayList<>();
    entityTypeMap.clear(); // Clear existing mappings if needed

    for (Map.Entry<String, EntityConfigRecord> entry : entityMap.entrySet()) {
      String key = entry.getKey();
      EntityConfigRecord entity = entry.getValue();
      Map<String, ModeConfigRecord> modes = new HashMap<>();

      // create modes
      for (ModeConfigRecord mode : entity.modes()) {
        ModeConfigRecord modeConfig = new ModeConfigRecord(mode.name(), mode.entityProperties(),
            mode.controlConfig(),
            mode.image(), mode.movementSpeed());
        modes.put(mode.name(), modeConfig);
      }

      EntityTypeRecord entityType = getEntityType(entity, modes);
      entityTypes.add(entityType);
      entityTypeMap.put(key, entityType);
    }

    return entityTypes;
  }


  private static EntityTypeRecord getEntityType(EntityConfigRecord entity,
      Map<String, ModeConfigRecord> modes) {

    return new EntityTypeRecord(
        entity.name(),
        modes,
        entity.entityProperties().blocks()
    );

  }

  // ---- Methods for loading Game Config ----

  /**
   * Loads the game configuration from a JSON file at the specified filepath.
   *
   * @param filepath the path to the JSON configuration file
   * @return a {@code GameConfigRecord} containing the parsed game configuration
   * @throws ConfigException if there is an error reading or parsing the configuration file
   */
  public GameConfigRecord loadGameConfig(String filepath) throws ConfigException {
    try {
      JsonNode root = mapper.readTree(new File(filepath).toURI().toURL());

      MetadataRecord metadata = parseMetadata(root);
      SettingsRecord defaultSettings = parseDefaultSettings(root);
      List<LevelRecord> levels = parseLevels(root);
      List<CollisionConfigRecord> collisions = parseCollisions(root);
      JsonNode currentLevelNode = root.get("currentLevelIndex");
      int currentLevelIndex = currentLevelNode != null ? currentLevelNode.asInt() : 0;

      Map<String, Double> respawnableEntities = parseRespawnableEntities(root);

      return new GameConfigRecord(
          metadata,
          defaultSettings,
          levels,
          collisions,
          getFolderPath(filepath),
          currentLevelIndex,
          respawnableEntities
      );

    } catch (IOException e) {
      throw new ConfigException("Failed to parse config file: " + filepath, e);
    }
  }

  private Map<String, Double> parseRespawnableEntities(JsonNode root) {
    Map<String, Double> respawnableEntities = new HashMap<>();
    JsonNode respawnNode = root.get("respawnableEntities");
    if (respawnNode != null && respawnNode.isObject()) {
      Iterator<Map.Entry<String, JsonNode>> fields = respawnNode.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> entry = fields.next();
        respawnableEntities.put(entry.getKey(), entry.getValue().asDouble());
      }
    }
    return respawnableEntities;
  }


  private MetadataRecord parseMetadata(JsonNode root) throws JsonProcessingException {
    return mapper.treeToValue(root.get("metadata"), MetadataRecord.class);
  }

  private SettingsRecord parseDefaultSettings(JsonNode root) throws JsonProcessingException {
    return mapper.treeToValue(root.get("defaultSettings"), SettingsRecord.class);
  }

  private List<LevelRecord> parseLevels(JsonNode root) {
    List<LevelRecord> levels = new ArrayList<>();
    for (JsonNode levelNode : root.get("levels")) {
      String levelMap = levelNode.get("levelMap").asText();
      levels.add(new LevelRecord(levelMap));
    }
    return levels;
  }

  private List<CollisionConfigRecord> parseCollisions(JsonNode root)
      throws JsonProcessingException {
    List<CollisionConfigRecord> collisions = new ArrayList<>();
    JsonNode collisionsNode = root.get("collisions");
    if (collisionsNode != null && collisionsNode.isArray()) {
      for (JsonNode collisionNode : collisionsNode) {
        collisions.add(mapper.treeToValue(collisionNode, CollisionConfigRecord.class));
      }
    }
    return collisions;
  }

  private String getFolderPath(String filepath) throws ConfigException {
    Pattern pattern = Pattern.compile("^(.*[\\\\/])");
    Matcher matcher = pattern.matcher(filepath);

    if (matcher.find()) {
      return matcher.group(1);
    }

    throw new ConfigException("Failed to getFolderPath: " + filepath);
  }

  // ---- Methods for loading Entities ----

  private Map<String, EntityConfigRecord> constructEntities(String folderPath)
      throws ConfigException {
    String entityFolderPath;

    try {
      entityFolderPath = folderPath + ConstantsManager.getMessage("Config", "ENTITIES_FOLDER");
    } catch (ConstantsManagerException e) {
      throw new ConfigException("Error in getting entities folder from constants:", e);
    }

    List<String> entities = getAvailableEntities(entityFolderPath);
    Map<String, EntityConfigRecord> entitiesMap = new HashMap<>();

    for (String entity : entities) {
      EntityConfigRecord entityConfig = loadEntityConfig(
          entityFolderPath + entity + JSON_IDENTIFIER);
      entitiesMap.put(entity, entityConfig); // map the entity file name to the entityConfig
    }

    return entitiesMap;
  }

  EntityConfigRecord loadEntityConfig(String filepath) throws ConfigException {
    try {
      JsonNode root = mapper.readTree(new File(filepath));

      JsonNode entityTypeNode = root.get(ENTITY_TYPE);
      EntityPropertiesRecord defaultProps = parseEntityProperties(entityTypeNode, filepath);
      List<ModeConfigRecord> modes = parseModes(root.get("modes"), defaultProps, filepath);

      return new EntityConfigRecord(
          defaultProps.name(),
          defaultProps,
          modes
      );
    } catch (IOException e) {
      throw new ConfigException("Failed to parse config file: " + filepath, e);
    }
  }

  private EntityPropertiesRecord parseEntityProperties(JsonNode entityTypeNode, String filepath)
      throws ConfigException {
    try {
      return mapper.treeToValue(entityTypeNode, EntityPropertiesRecord.class);
    } catch (Exception e) {
      throw new ConfigException("Failed to process entityType in json: " + filepath, e);
    }
  }

  private List<ModeConfigRecord> parseModes(JsonNode modesNode,
      EntityPropertiesRecord defaultProps,
      String filepath)
      throws ConfigException {
    try {
      List<ModeConfigRecord> modes = new ArrayList<>();
      if (modesNode == null) {
        throw new ConfigException("Failed to get modes from json: " + filepath);
      }
      for (JsonNode modeNode : modesNode) {
        String name = modeNode.get("name").asText();
        EntityPropertiesRecord overrideProps = mergeProperties(name, defaultProps, modeNode);
        ImageConfigRecord image = mapper.treeToValue(modeNode.get("image"),
            ImageConfigRecord.class);
        ControlConfigInterface controlConfig = mapper.treeToValue(modeNode.get("controlConfig"),
            ControlConfigInterface.class);
        Double speed = modeNode.get("movementSpeed").asDouble();
        modes.add(new ModeConfigRecord(name, overrideProps, controlConfig, image, speed));
      }
      return modes;
    } catch (JsonProcessingException e) {
      throw new ConfigException("Failed to process modes in json: " + filepath, e);
    }
  }

  private EntityPropertiesRecord mergeProperties(String modeName,
      EntityPropertiesRecord defaultProps,
      JsonNode modeNode) {
    final String BLOCKS = "blocks";

    List<String> blocks = modeNode.has(BLOCKS)
        ? mapper.convertValue(modeNode.get(BLOCKS),
        mapper.getTypeFactory().constructCollectionType(List.class, String.class))
        : defaultProps.blocks();

    return new EntityPropertiesRecord(
        modeName,
        blocks
    );
  }


  private static List<String> getAvailableEntities(String folderPath) {
    return FileUtility.getFileNamesInDirectory(folderPath, JSON_IDENTIFIER);
  }

}
