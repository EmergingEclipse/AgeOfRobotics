package com.modpackswork.ageofrobotics.core.world;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Structural guards over the NeoForge registration glue for the scrap ground block set (issue #7).
 *
 * <p>The {@code mod} module has no test source set, so these read the source files instead of
 * loading Minecraft classes. They are deliberately coarse. The compiler already proves the code is
 * valid, and what these add is the thing the compiler cannot see: that a block added to {@link
 * ScrapGroundBlock} is actually registered, wired to the mod event bus and reachable in the
 * creative menu rather than only existing as resource files.
 */
class ScrapGroundBlockRegistrationTest {

  private static final Path REPO_ROOT = Path.of(System.getProperty("aor.repoRoot", "."));
  private static final Path MOD_JAVA =
      REPO_ROOT.resolve("mod/src/main/java/com/modpackswork/ageofrobotics");

  private static String read(Path path) throws IOException {
    assertTrue(Files.isRegularFile(path), "expected source file to exist: " + path);
    return Files.readString(path, StandardCharsets.UTF_8);
  }

  @ParameterizedTest
  @EnumSource(ScrapGroundBlock.class)
  void everyBlockInTheSetIsActuallyRegistered(ScrapGroundBlock block) throws IOException {
    final String source = read(MOD_JAVA.resolve("block/ModBlocks.java"));

    assertTrue(
        source.contains("ScrapGroundBlock." + block.name()),
        "adding a constant to ScrapGroundBlock is not enough; register "
            + block.blockId()
            + " in ModBlocks too");
  }

  @Test
  void blockRegistrationCoversBlocksAndTheirBlockItems() throws IOException {
    final String source = read(MOD_JAVA.resolve("block/ModBlocks.java"));

    assertTrue(
        source.contains("DeferredRegister.createBlocks"),
        "blocks must go through DeferredRegister.Blocks");
    assertTrue(
        source.contains("registerSimpleBlockItem"),
        "a block with no block item cannot be picked up, carried or placed");
    assertTrue(
        source.contains("requiresCorrectToolForDrops"),
        "terrain blocks must need the right tool or the pickaxe tags mean nothing");
  }

  @Test
  void entrypointWiresBlockRegistrationOntoTheModEventBus() throws IOException {
    final String source = read(MOD_JAVA.resolve("AgeOfRobotics.java"));

    assertTrue(
        source.contains("ModBlocks.register(modEventBus)"),
        "DeferredRegister does nothing until it is attached to the mod event bus");
  }

  @Test
  void creativeTabContributionLivesInTheBlockPackageAndUsesTheContentsEvent() throws IOException {
    final String source = read(MOD_JAVA.resolve("block/BlockCreativeTabEntries.java"));

    assertTrue(
        source.contains("BuildCreativeModeTabContentsEvent"),
        "contribute to an existing tab through the contents event rather than a new mod tab");
    assertTrue(
        source.contains("@EventBusSubscriber") && source.contains("@SubscribeEvent"),
        "the listener must be auto-registered; NeoForge 21.1 picks the bus from the event type");
    assertFalse(
        source.contains("EventBusSubscriber.Bus."),
        "bus() is deprecated for removal in NeoForge 21.1; let it infer the bus instead");
    assertTrue(
        source.contains("CreativeModeTabs."),
        "blocks must land in a vanilla creative tab so they are reachable without commands");
  }
}
