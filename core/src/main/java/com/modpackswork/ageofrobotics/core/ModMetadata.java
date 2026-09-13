package com.modpackswork.ageofrobotics.core;

/**
 * Single source of truth for the mod's identity.
 *
 * <p>Lives in the loader-independent {@code core} module so gameplay code, the NeoForge entrypoint
 * and the build's structural tests all agree on one value. Keep {@link #MOD_ID} in sync with {@code
 * modId} in {@code gradle.properties} and {@code modId} in {@code neoforge.mods.toml} — {@code
 * ModMetadataTest} fails the build if they drift apart.
 */
public final class ModMetadata {

  /** Namespace used for every registry entry, resource path and data path this mod owns. */
  public static final String MOD_ID = "ageofrobotics";

  /** Human-readable mod name, as shown in the mod list. */
  public static final String MOD_NAME = "Age of Robotics";

  private ModMetadata() {}

  /**
   * Builds a namespaced id string for this mod, e.g. {@code ageofrobotics:scrap_ground}.
   *
   * @param path the registry path, lowercase
   * @return the fully qualified {@code modid:path} string
   */
  public static String id(String path) {
    return MOD_ID + ":" + path;
  }
}
