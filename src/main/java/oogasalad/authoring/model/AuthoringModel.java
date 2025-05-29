package oogasalad.authoring.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import oogasalad.engine.config.CollisionRule;
import oogasalad.engine.config.EntityPlacement;
import oogasalad.engine.config.JsonConfigBuilder;
import oogasalad.engine.config.JsonConfigSaver;
import oogasalad.engine.exceptions.ConfigException;
import oogasalad.engine.records.config.ModeConfigRecord;
import oogasalad.engine.records.config.model.SettingsRecord;
import oogasalad.engine.records.config.model.losecondition.LivesBasedConditionRecord;
import oogasalad.engine.records.config.model.wincondition.SurviveForTimeConditionRecord;
import oogasalad.engine.records.model.EntityTypeRecord;

/**
 * The central model for the Authoring Environment. Stores global game settings, entity templates,
 * and multiple level drafts.
 * <p>
 * Manages adding, updating, and retrieving entity types and level data, and synchronizes changes
 * across levels as needed.
 *
 * @author Will He, Angela Predolac
 */
public class AuthoringModel {

  private String gameTitle;
  private String author;
  private String gameDescription;
  private SettingsRecord defaultSettings;
  private List<CollisionRule> collisionRules;

  private Map<String, EntityTypeRecord> entityTypeMap;
  private List<LevelDraft> levels;
  private int currentLevelIndex;

  /**
   * Constructs a new AuthoringModel with default settings and no levels.
   */
  public AuthoringModel() {
    this.entityTypeMap = new LinkedHashMap<>();
    this.levels = new ArrayList<>();
    this.collisionRules = new ArrayList<>();
    // Create default settings with appropriate win condition and score strategy
    this.defaultSettings = new SettingsRecord(
        1.0,                                    // Default game speed
        3,                                      // Default starting lives
        0,                                      // Default initial score
        new SurviveForTimeConditionRecord(5),   // Default win condition - survive for 5 seconds
        new LivesBasedConditionRecord(),         // Default lose condition
        new HashSet<>(),
        false                                    // Default isMultiplayer
    );
  }


  /**
   * Returns the currently selected level being edited.
   *
   * @return the current LevelDraft
   */
  public LevelDraft getCurrentLevel() {
    return levels.get(currentLevelIndex);
  }

  /**
   * Adds a new level to the project and switches to it.
   *
   * @param level the LevelDraft to add
   */
  public void addLevel(LevelDraft level) {
    levels.add(level);
    currentLevelIndex = levels.size() - 1;
  }

  /**
   * Adds or replaces an entity type in the global entity type map. If a type with the same name
   * already exists, it is overwritten.
   *
   * @param type the EntityType to add
   */
  public void addEntityType(EntityTypeRecord type) {
    entityTypeMap.put(type.type(), type);
  }

  /**
   * Updates an existing entity type by name. If the name has changed, removes the old type and
   * replaces it with the new one. Also updates all entity placements in all levels to reference the
   * new type.
   *
   * @param oldTypeName the name of the type to replace
   * @param newType     the new EntityType data
   */
  public void updateEntityType(String oldTypeName, EntityTypeRecord newType) {
    if (!isValidUpdate(oldTypeName, newType)) {
      return;
    }

    updateEntityTypeMap(oldTypeName, newType);
    updateEntityPlacements(oldTypeName, newType);
  }

  private boolean isValidUpdate(String oldTypeName, EntityTypeRecord newType) {
    return oldTypeName != null && newType != null && entityTypeMap.containsKey(oldTypeName);
  }

  private void updateEntityTypeMap(String oldTypeName, EntityTypeRecord newType) {
    if (!oldTypeName.equals(newType.type())) {
      entityTypeMap.remove(oldTypeName);
    }
    addEntityType(newType);
  }

  private void updateEntityPlacements(String oldTypeName, EntityTypeRecord newType) {

    for (LevelDraft level : levels) {
      for (EntityPlacement placement : level.getEntityPlacements()) {
        if (placement.getTypeString().equals(oldTypeName)) {
          placement.setType(newType.type());
          placement.setResolvedEntityType(newType);
        }
      }
    }
  }

  /**
   * Returns an unmodifiable collection of all entity types defined globally.
   *
   * @return the entity types
   */
  public Collection<EntityTypeRecord> getEntityTypes() {
    return Collections.unmodifiableCollection(entityTypeMap.values());
  }

  /**
   * Finds an entity type by its name.
   *
   * @param typeName the name of the entity type to look up
   * @return an Optional containing the EntityType if found; empty otherwise
   */
  public Optional<EntityTypeRecord> findEntityType(String typeName) {
    return Optional.ofNullable(entityTypeMap.get(typeName));
  }

  /**
   * Returns the list of all level drafts.
   *
   * @return the list of levels
   */
  public List<LevelDraft> getLevels() {
    return levels;
  }

  /**
   * Clears all entity types and levels from the model. Use with caution — this is a full reset.
   */
  public void clearAll() {
    gameTitle = null;
    author = null;
    gameDescription = null;
    entityTypeMap.clear();
    levels.clear();
    collisionRules.clear();
    currentLevelIndex = 0;
  }


  /**
   * Set current level index
   *
   * @param index Set value
   */
  public void setCurrentLevelIndex(int index) {
    this.currentLevelIndex = index;
  }

  /**
   * Gets the default game settings for the project
   *
   * @return the Settings object
   */
  public SettingsRecord getDefaultSettings() {
    return defaultSettings;
  }

  /**
   * Sets the default game settings for the project
   *
   * @param settings the new Settings to use
   */
  public void setDefaultSettings(SettingsRecord settings) {
    this.defaultSettings = settings;
  }

  /**
   * Returns the current title of the game.
   *
   * @return the game title as a String
   */
  public String getGameTitle() {
    return gameTitle;
  }

  /**
   * Sets the title of the game.
   *
   * @param gameTitle the new title of the game
   */
  public void setGameTitle(String gameTitle) {
    this.gameTitle = gameTitle;
  }

  /**
   * Returns the name of the game's author.
   *
   * @return the author name as a String
   */
  public String getAuthor() {
    return author;
  }

  /**
   * Sets the name of the game's author.
   *
   * @param author the name of the author
   */
  public void setAuthor(String author) {
    this.author = author;
  }

  /**
   * Returns the game's description.
   *
   * @return the game description as a String
   */
  public String getGameDescription() {
    return gameDescription;
  }

  /**
   * Sets the description for the game.
   *
   * @param gameDescription a summary or explanation of the game
   */
  public void setGameDescription(String gameDescription) {
    this.gameDescription = gameDescription;
  }

  /**
   * Returns the list of defined collision rules for the game.
   *
   * @return a list of CollisionRule objects
   */
  public List<CollisionRule> getCollisionRules() {
    return collisionRules;
  }

  /**
   * Sets the list of collision rules for the game.
   *
   * @param collisionRules a list of rules describing interactions between entities
   */
  public void setCollisionRules(List<CollisionRule> collisionRules) {
    this.collisionRules = collisionRules;
  }

  /**
   * Returns the mapping of entity type names to their EntityType configurations.
   *
   * @return a map from entity name (String) to EntityType
   */
  public Map<String, EntityTypeRecord> getEntityTypeMap() {
    return entityTypeMap;
  }

  /**
   * Sets the mapping of entity type names to their configurations.
   *
   * @param entityTypeMap a map from entity name to EntityType
   */
  public void setEntityTypeMap(Map<String, EntityTypeRecord> entityTypeMap) {
    this.entityTypeMap = entityTypeMap;
  }

  /**
   * Sets the list of level drafts in the game.
   *
   * @param levels the list of LevelDrafts to use
   */
  public void setLevels(List<LevelDraft> levels) {
    this.levels = levels;
  }

  /**
   * Returns the index of the currently selected level.
   *
   * @return the index of the current level
   */
  public int getCurrentLevelIndex() {
    return currentLevelIndex;
  }

  /**
   * Returns a mapping from entity type names to their list of available mode names.
   *
   * @return a map from entity name to a list of mode names
   */
  public Map<String, List<String>> getEntityTypeToModes() {
    Map<String, List<String>> result = new HashMap<>();
    for (EntityTypeRecord entity : entityTypeMap.values()) {
      Map<String, ModeConfigRecord> modeMap = entity.modes();
      result.put(entity.type(), new ArrayList<>(modeMap.keySet()));
    }
    return result;
  }

  /**
   * Saves the current authoring environment into a set of JSON configuration files within the
   * specified output folder. This method serializes the game metadata, default settings, levels,
   * and entity type definitions using a {@link JsonConfigBuilder} and {@link JsonConfigSaver}.
   * <p>
   * The output includes:
   * <ul>
   *   <li><b>gameConfig.json</b>: top-level metadata and game structure</li>
   *   <li><b>levelX.json</b>: layout files for each level (where X is the level number)</li>
   *   <li><b>[entity].json</b>: configuration files for each entity type</li>
   * </ul>
   *
   * @param outputFolder the folder to which all generated JSON files will be saved
   */
  public void saveGame(Path outputFolder) throws ConfigException {
    try {
      // Ensure the output directory exists before writing any files
      Files.createDirectories(outputFolder);
    } catch (IOException e) {
      throw new ConfigException("Failed to create output directory: " + outputFolder, e);
    }

    ObjectMapper mapper = new ObjectMapper();
    JsonConfigSaver saver = new JsonConfigSaver();
    JsonConfigBuilder builder = new JsonConfigBuilder();

    // GameConfig
    ObjectNode gameJson = builder.buildGameConfig(this, mapper);
    saver.saveGameConfig(gameJson, outputFolder);

    // EntityType -> ID mapping
    Map<String, Integer> entityToId = builder.assignIds(entityTypeMap);

    // Levels
    for (int i = 0; i < levels.size(); i++) {
      ObjectNode levelJson = builder.buildLevelConfig(levels.get(i), entityToId, mapper);
      if (levels.get(i).getBackgroundImage() != null) {
        saver.writeAsset(
            Paths.get(levels.get(i).getBackgroundImage().getPath()).toUri().toString(),
            outputFolder);
      }
      saver.saveLevel("level" + (i + 1), levelJson, outputFolder);
    }

    // EntityTypes
    for (EntityTypeRecord e : entityTypeMap.values()) {
      copyImagesToAssetFolder(outputFolder, e, saver);
      ObjectNode entityJson = builder.buildEntityTypeConfig(e, mapper);
      saver.saveEntityType(e.type(), entityJson, outputFolder);
    }
  }

  private static void copyImagesToAssetFolder(Path outputFolder, EntityTypeRecord e,
      JsonConfigSaver saver)
      throws ConfigException {
    for (ModeConfigRecord modeConfig : e.modes().values()) {
      saver.writeAsset(modeConfig.image().imagePath(), outputFolder);
    }
  }

  /**
   * Deletes an entity type and all its placements across all levels.
   *
   * @param typeName the name of the entity type to delete
   * @return true if the entity was found and deleted, false otherwise
   */
  public boolean deleteEntityType(String typeName) {
    if (!entityTypeMap.containsKey(typeName)) {
      return false;
    }

    // Remove the entity type from the map
    entityTypeMap.remove(typeName);

    // Remove all placements of this entity type from all levels
    for (LevelDraft level : levels) {
      List<EntityPlacement> placementsToRemove = level.getEntityPlacements().stream()
          .filter(p -> typeName.equals(p.getTypeString()))
          .toList();

      for (EntityPlacement placement : placementsToRemove) {
        level.removeEntityPlacement(placement);
      }
    }

    return true;
  }

}
