package oogasalad.networking.util;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Singleton utility class providing ObjectMapper instance to prevent recreating new mapper for
 * multiple classes.
 *
 * @author Austin Huang
 */
public class JsonUtils {
  private static ObjectMapper mapper = new ObjectMapper();

  private JsonUtils() {}

  /**
   * Returns the shared {@link ObjectMapper} instance.
   * <p>
   * This method always returns the same preconfigured instance and is safe for use
   * across threads and components.
   *
   * @return the singleton ObjectMapper instance
   */
  public static ObjectMapper getMapper() {
    return mapper;
  }
}
