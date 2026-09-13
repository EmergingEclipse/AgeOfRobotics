package com.modpackswork.ageofrobotics.core.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Guards the canonical description of the scrap ground block set (issue #7).
 *
 * <p>This enum is the single source of truth that block registration, the resource guards and the
 * worldgen work in issue #6 all read from, so adding a terrain variant means adding one constant
 * here rather than editing several parallel lists.
 */
class ScrapGroundBlockTest {

  @Test
  void theSetContainsTheBaseBlockAndTheDeepLayerFiller() {
    assertEquals(2, ScrapGroundBlock.values().length, "issue #7 ships exactly two blocks");
    assertEquals("scrap_block", ScrapGroundBlock.SCRAP_BLOCK.path());
    assertEquals("compacted_scrap", ScrapGroundBlock.COMPACTED_SCRAP.path());
  }

  @ParameterizedTest
  @EnumSource(ScrapGroundBlock.class)
  void pathsAreValidResourceLocationPaths(ScrapGroundBlock block) {
    assertTrue(
        block.path().matches("[a-z0-9_]+"),
        "registry paths must be lowercase snake_case or Minecraft refuses them: " + block.path());
    assertEquals(
        block.path().toLowerCase(Locale.ROOT), block.path(), "registry paths must be lowercase");
  }

  @ParameterizedTest
  @EnumSource(ScrapGroundBlock.class)
  void idsAndTranslationKeysAreNamespacedToThisMod(ScrapGroundBlock block) {
    assertEquals("ageofrobotics:" + block.path(), block.blockId());
    assertEquals("block.ageofrobotics." + block.path(), block.translationKey());
  }

  @Test
  void theBaseFillerIsMineableWithTheStartingPickaxeAndTheDeepLayerIsNot() {
    assertEquals(
        Optional.empty(),
        ScrapGroundBlock.SCRAP_BLOCK.vanillaToolTierTag(),
        "the dimension-wide filler must not be gated above the pickaxe a first visitor carries");
    assertEquals(
        Optional.of("needs_stone_tool"),
        ScrapGroundBlock.COMPACTED_SCRAP.vanillaToolTierTag(),
        "the deep layer is the tougher tier step, matching how vanilla gates metal blocks");
  }

  @ParameterizedTest
  @EnumSource(ScrapGroundBlock.class)
  void toolTierTagsNameVanillaTagsRatherThanFullResourceLocations(ScrapGroundBlock block) {
    block
        .vanillaToolTierTag()
        .ifPresent(
            tag -> {
              assertFalse(
                  tag.contains(":"), "tool tier tags live in the minecraft namespace: " + tag);
              assertTrue(tag.startsWith("needs_"), "expected a needs_*_tool vanilla tag: " + tag);
            });
  }
}
