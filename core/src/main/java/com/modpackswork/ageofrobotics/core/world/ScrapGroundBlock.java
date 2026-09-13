package com.modpackswork.ageofrobotics.core.world;

import com.modpackswork.ageofrobotics.core.ModMetadata;
import java.util.Optional;

/**
 * The terrain filler blocks of the Machine Dimension (issue #7).
 *
 * <p>Lives in {@code core} on purpose. Block registration, the resource guards in {@code
 * core/src/test} and the worldgen wiring in issue #6 all need the same list of registry paths, and
 * keeping that list in one loader-independent place means a new terrain variant is a single new
 * constant here rather than an edit in three places that can silently drift.
 *
 * <p>Only names and data pack facts belong here. Block hardness, sounds and map colours are
 * Minecraft types, so they stay next to the registration code in the {@code mod} module.
 */
public enum ScrapGroundBlock {

  /**
   * The surface and mid-depth filler, playing the role stone plays in the overworld. Deliberately
   * carries no tool tier tag so a player who arrives with a wooden pickaxe can still dig.
   */
  SCRAP_BLOCK("scrap_block", null),

  /**
   * The deep-layer filler, playing the role deepslate plays in the overworld. Gated at stone tier
   * because vanilla already puts solid metal blocks such as copper in {@code needs_stone_tool}.
   */
  COMPACTED_SCRAP("compacted_scrap", "needs_stone_tool");

  private final String path;
  private final String vanillaToolTierTag;

  ScrapGroundBlock(String path, String vanillaToolTierTag) {
    this.path = path;
    this.vanillaToolTierTag = vanillaToolTierTag;
  }

  /**
   * The registry path, which is also the file name of every resource this block needs.
   *
   * @return the lowercase snake_case registry path, e.g. {@code scrap_block}
   */
  public String path() {
    return path;
  }

  /**
   * The fully qualified block id.
   *
   * @return the namespaced id, e.g. {@code ageofrobotics:scrap_block}
   */
  public String blockId() {
    return ModMetadata.id(path);
  }

  /**
   * The lang file key Minecraft looks up for this block's name.
   *
   * @return the translation key, e.g. {@code block.ageofrobotics.scrap_block}
   */
  public String translationKey() {
    return "block." + ModMetadata.MOD_ID + "." + path;
  }

  /**
   * The {@code random_sequence} value a block loot table needs so drops are deterministic per block
   * rather than shared across the whole mod.
   *
   * @return the loot table's random sequence id, e.g. {@code ageofrobotics:blocks/scrap_block}
   */
  public String lootRandomSequence() {
    return ModMetadata.id("blocks/" + path);
  }

  /**
   * The vanilla mining tier tag this block must be listed in, if any.
   *
   * @return the bare vanilla block tag name such as {@code needs_stone_tool}, or empty when a
   *     wooden pickaxe is enough
   */
  public Optional<String> vanillaToolTierTag() {
    return Optional.ofNullable(vanillaToolTierTag);
  }
}
