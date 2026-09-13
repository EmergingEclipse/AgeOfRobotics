package com.modpackswork.ageofrobotics.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Structural tests for the mod scaffold (issue #4).
 *
 * <p>These guard against drift between the mod id / platform versions declared in code, in {@code
 * gradle.properties}, and in the NeoForge metadata files that ship inside the jar.
 */
class ModMetadataTest {

  private static final Path REPO_ROOT = Path.of(System.getProperty("aor.repoRoot", "."));
  private static final Path MOD_RESOURCES = REPO_ROOT.resolve("mod/src/main/resources");

  private static String read(Path relativeToRoot) throws IOException {
    final Path path = REPO_ROOT.resolve(relativeToRoot);
    assertTrue(Files.isRegularFile(path), "expected file to exist: " + path);
    return Files.readString(path, StandardCharsets.UTF_8);
  }

  private static Properties gradleProperties() throws IOException {
    final Properties properties = new Properties();
    try (var in = Files.newInputStream(REPO_ROOT.resolve("gradle.properties"))) {
      properties.load(in);
    }
    return properties;
  }

  private static String tomlValue(String toml, String key) {
    final Matcher matcher =
        Pattern.compile("(?m)^\s*" + Pattern.quote(key) + "\s*=\s*\"([^\"]*)\"").matcher(toml);
    assertTrue(matcher.find(), "expected key '" + key + "' in neoforge.mods.toml");
    return matcher.group(1);
  }

  @Test
  void modIdFollowsNeoForgeNamingRules() {
    assertTrue(
        ModMetadata.MOD_ID.matches("^[a-z][a-z0-9_]{1,63}$"),
        "mod id must be lowercase alphanumeric/underscore: " + ModMetadata.MOD_ID);
  }

  @Test
  void modIdMatchesGradleProperties() throws IOException {
    assertEquals(gradleProperties().getProperty("modId"), ModMetadata.MOD_ID);
  }

  @Test
  void modsTomlDeclaresTheSameModId() throws IOException {
    final String toml = read(Path.of("mod/src/main/resources/META-INF/neoforge.mods.toml"));
    assertEquals(ModMetadata.MOD_ID, tomlValue(toml, "modId"));
  }

  @Test
  void modsTomlTargetsTheNeoForgeLoaderAndDeclaredPlatformVersions() throws IOException {
    final String toml = read(Path.of("mod/src/main/resources/META-INF/neoforge.mods.toml"));
    final Properties properties = gradleProperties();

    assertEquals("javafml", tomlValue(toml, "modLoader"));
    assertTrue(
        toml.contains("[[dependencies." + ModMetadata.MOD_ID + "]]"),
        "mods.toml must declare dependencies for this mod id");
    assertTrue(
        toml.contains("modId = \"neoforge\""), "mods.toml must declare a neoforge dependency");
    assertTrue(
        toml.contains("modId = \"minecraft\""), "mods.toml must declare a minecraft dependency");
    assertTrue(
        toml.contains(properties.getProperty("minecraftVersion")),
        "mods.toml must pin the Minecraft version from gradle.properties");
    assertTrue(
        toml.contains(properties.getProperty("neoForgeVersion")),
        "mods.toml must pin the NeoForge version from gradle.properties");
  }

  @Test
  void packMcmetaUsesTheMinecraft1211PackFormat() throws IOException {
    final String mcmeta = read(Path.of("mod/src/main/resources/pack.mcmeta"));
    assertTrue(
        mcmeta.replaceAll("\s+", "").contains("\"pack_format\":34"),
        "1.21.1 resource packs use pack_format 34, got: " + mcmeta);
  }

  @Test
  void modEntrypointClassExists() {
    assertTrue(
        Files.isRegularFile(
            REPO_ROOT.resolve(
                "mod/src/main/java/com/modpackswork/ageofrobotics/AgeOfRobotics.java")),
        "mod entrypoint class must exist");
  }

  @Test
  void basePackageStructureExists() {
    for (final String feature : new String[] {"item", "block", "dimension", "robot", "power"}) {
      final Path modPackage =
          REPO_ROOT.resolve("mod/src/main/java/com/modpackswork/ageofrobotics").resolve(feature);
      assertTrue(
          Files.isDirectory(modPackage), "missing base package for feature area: " + modPackage);
      assertTrue(
          Files.isRegularFile(modPackage.resolve("package-info.java")),
          "each base package must be documented: " + modPackage.resolve("package-info.java"));
    }
  }

  @Test
  void modResourcesDirectoryIsWiredForTheDeclaredModId() {
    assertTrue(
        Files.isDirectory(MOD_RESOURCES.resolve("assets").resolve(ModMetadata.MOD_ID)),
        "assets/<modid> must exist");
    assertTrue(
        Files.isDirectory(MOD_RESOURCES.resolve("data").resolve(ModMetadata.MOD_ID)),
        "data/<modid> must exist");
  }
}
