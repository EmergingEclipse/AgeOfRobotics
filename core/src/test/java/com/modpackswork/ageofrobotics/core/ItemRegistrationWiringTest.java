package com.modpackswork.ageofrobotics.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.modpackswork.ageofrobotics.core.robot.CircuitTier;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Guards that the item registry is actually reachable from the mod entrypoint (issue #15).
 *
 * <p>A {@code DeferredRegister} that is never handed the mod event bus compiles perfectly and
 * registers nothing, so {@code :mod:compileJava} cannot catch it and the {@code mod} module has no
 * test source set of its own. Reading the source text is coarse, but it is the only place this
 * failure mode can be caught automatically, and it is the mistake most likely to be made by the
 * later item tickets copying this pattern.
 */
class ItemRegistrationWiringTest {

  private static final Path REPO_ROOT = Path.of(System.getProperty("aor.repoRoot", "."));
  private static final Path MOD_SOURCES =
      REPO_ROOT.resolve("mod/src/main/java/com/modpackswork/ageofrobotics");

  private static String read(Path path) throws IOException {
    assertTrue(Files.isRegularFile(path), "expected source file to exist: " + path);
    return Files.readString(path, StandardCharsets.UTF_8);
  }

  @Test
  void entrypointHandsTheItemRegistryTheModEventBus() throws IOException {
    final String entrypoint = read(MOD_SOURCES.resolve("AgeOfRobotics.java"));

    assertTrue(
        entrypoint.contains("ModItems.register(modEventBus)"),
        "AgeOfRobotics must attach the item DeferredRegister to the mod event bus, or no item is"
            + " ever registered");
  }

  @Test
  void itemRegistrationTakesItsRegistryNameFromTheCoreModel() throws IOException {
    final String items = read(MOD_SOURCES.resolve("item/ModItems.java"));

    assertTrue(
        items.contains("DeferredRegister.createItems"),
        "items are registered through DeferredRegister.Items on NeoForge 21.1");
    assertTrue(
        items.contains("CircuitTier.BASIC.itemPath()"),
        "the registry name must come from CircuitTier so the core model, the recipe and the item"
            + " cannot drift apart");
    assertFalse(
        items.contains('"' + CircuitTier.BASIC.itemPath() + '"'),
        "the registry name is hardcoded here as well as in the core model; keep one source of"
            + " truth");
  }

  @Test
  void creativeTabContributionLivesInItsOwnFileSoItNeverConflicts() throws IOException {
    final String tab = read(MOD_SOURCES.resolve("item/ModCreativeTabEntries.java"));

    assertTrue(
        tab.contains("BuildCreativeModeTabContentsEvent"),
        "the item is added to a vanilla tab through the creative tab contents event");
    assertTrue(
        tab.contains("CreativeModeTabs."),
        "the contribution must target a vanilla creative tab; a mod tab is out of scope for #15");
  }
}
