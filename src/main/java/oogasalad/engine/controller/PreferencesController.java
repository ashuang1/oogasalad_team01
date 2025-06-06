package oogasalad.engine.controller;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import oogasalad.engine.utility.LoggingManager;

/**
 * A controller to handle the saving and accessing of the user's preferences file. This utility
 * class was written originally for the cell society project.
 *
 * @author Owen Jennings
 */
public class PreferencesController {

  private static final Preferences preferences = Preferences.userNodeForPackage(
      PreferencesController.class);

  /**
   * Saves a preference value as a string.
   *
   * @param key   the preference key
   * @param value the value to save as a string
   */
  public static void setPreference(String key, String value) {
    preferences.put(key, value);
    try {
      preferences.flush(); // Ensure the preference is written to storage
    } catch (BackingStoreException e) {
      LoggingManager.LOGGER.warn("Error saving preferences: {}", e.getMessage(), e);
    }
  }

  /**
   * Retrieves a preference value as a string.
   *
   * @param key          the preference key
   * @param defaultValue the default value if the preference is not found
   * @return the retrieved preference value as a string
   */
  public static String getPreference(String key, String defaultValue) {
    return preferences.get(key, defaultValue);
  }
}

