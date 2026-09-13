package com.modpackswork.ageofrobotics.core.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Structural guards for the data pack side of the scrap ground block set (issue #7).
 *
 * <p>Two failure modes this catches are invisible at build time and only show up in a running
 * world. A block that generates across a whole dimension but has no loot table drops nothing when
 * mined, and a block missing from {@code mineable/pickaxe} is either punchable by hand or, with
 * {@code requiresCorrectToolForDrops}, unminable with any tool.
 *
 * <p>The third guard is the 1.21.1 rename trap. Data pack folders went singular in 1.21, so a file
 * written at {@code loot_tables/} or {@code tags/blocks/} is loaded by nothing and reports no
 * error.
 */
class ScrapGroundBlockDataPackTest {

  private static final Path REPO_ROOT = Path.of(System.getProperty("aor.repoRoot", "."));
  private static final Path RESOURCES = REPO_ROOT.resolve("mod/src/main/resources");
  private static final Path DATA = RESOURCES.resolve("data");

  /** Folder names that were renamed to their singular form in Minecraft 1.21. */
  private static final Set<String> PRE_1211_PLURAL_FOLDERS =
      Set.of(
          "loot_tables",
          "recipes",
          "advancements",
          "blocks",
          "items",
          "predicates",
          "structures",
          "item_modifiers",
          "functions",
          "entity_types",
          "fluids",
          "game_events");

  private static JsonObject readJson(Path path) throws IOException {
    assertTrue(Files.isRegularFile(path), "expected data pack file to exist: " + path);
    try {
      return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8))
          .getAsJsonObject();
    } catch (JsonSyntaxException | IllegalStateException e) {
      throw new AssertionError("data pack file is not a JSON object: " + path, e);
    }
  }

  /** Every {@code minecraft:item} entry name in a loot table, however deeply nested. */
  private static List<String> itemEntryNames(JsonElement element) {
    final List<String> names = new ArrayList<>();
    if (element.isJsonArray()) {
      for (final JsonElement child : element.getAsJsonArray()) {
        names.addAll(itemEntryNames(child));
      }
      return names;
    }
    if (!element.isJsonObject()) {
      return names;
    }

    final JsonObject object = element.getAsJsonObject();
    if (object.has("type")
        && object.get("type").isJsonPrimitive()
        && "minecraft:item".equals(object.get("type").getAsString())
        && object.has("name")) {
      names.add(object.get("name").getAsString());
    }
    for (final String nested : new String[] {"pools", "entries", "children"}) {
      if (object.has(nested)) {
        names.addAll(itemEntryNames(object.get(nested)));
      }
    }
    return names;
  }

  private static List<String> tagValues(Path tagFile) throws IOException {
    final JsonObject tag = readJson(tagFile);

    assertTrue(
        tag.has("replace"), "a tag file added to a vanilla tag must state replace: " + tagFile);
    assertFalse(
        tag.get("replace").getAsBoolean(),
        "replace must be false or this file wipes the vanilla tag: " + tagFile);

    final JsonArray values = tag.getAsJsonArray("values");
    final List<String> ids = new ArrayList<>();
    for (final JsonElement value : values) {
      ids.add(value.getAsString());
    }
    return ids;
  }

  @ParameterizedTest
  @EnumSource(ScrapGroundBlock.class)
  void lootTableSitsAtTheSingularPathAndDropsTheBlockItself(ScrapGroundBlock block)
      throws IOException {
    final Path lootTable =
        DATA.resolve("ageofrobotics/loot_table/blocks/" + block.path() + ".json");
    final JsonObject table = readJson(lootTable);

    assertEquals(
        "minecraft:block",
        table.get("type").getAsString(),
        "a block loot table must declare the block type: " + lootTable);
    assertTrue(
        itemEntryNames(table).contains(block.blockId()),
        "loot table must drop the block itself or mining it across the dimension yields nothing: "
            + lootTable);
    assertEquals(
        block.lootRandomSequence(),
        table.get("random_sequence").getAsString(),
        "each block needs its own random_sequence: " + lootTable);
    assertTrue(
        table.toString().contains("minecraft:survives_explosion"),
        "drops should be destroyed by explosions the way vanilla terrain is: " + lootTable);
  }

  @ParameterizedTest
  @EnumSource(ScrapGroundBlock.class)
  void blockIsListedInTheVanillaPickaxeMineableTag(ScrapGroundBlock block) throws IOException {
    final Path pickaxe = DATA.resolve("minecraft/tags/block/mineable/pickaxe.json");

    assertTrue(
        tagValues(pickaxe).contains(block.blockId()),
        "every block in the set must be pickaxe mineable: " + block.blockId());
  }

  @ParameterizedTest
  @EnumSource(ScrapGroundBlock.class)
  void toolTierTagListsTheBlockWhenTheSetRequiresOne(ScrapGroundBlock block) throws IOException {
    final var tierTag = block.vanillaToolTierTag();
    if (tierTag.isEmpty()) {
      return;
    }

    final Path tagFile = DATA.resolve("minecraft/tags/block/" + tierTag.get() + ".json");
    assertTrue(
        tagValues(tagFile).contains(block.blockId()),
        block.blockId() + " claims tier " + tierTag.get() + " but is not listed in that tag");
  }

  @Test
  void dataPackDoesNotUseThePre1211PluralFolderNames() throws IOException {
    if (!Files.isDirectory(DATA)) {
      return;
    }

    try (Stream<Path> tree = Files.walk(DATA)) {
      final List<String> offenders =
          tree.filter(Files::isDirectory)
              .filter(
                  dir ->
                      PRE_1211_PLURAL_FOLDERS.contains(
                          dir.getFileName().toString().toLowerCase(Locale.ROOT)))
              // data/<ns>/loot_table/blocks is the one legitimate plural: it is a free-form
              // subfolder inside loot_table, not a registry folder name.
              .filter(dir -> !dir.getParent().getFileName().toString().equals("loot_table"))
              .map(dir -> DATA.relativize(dir).toString())
              .toList();

      assertTrue(
          offenders.isEmpty(),
          "Minecraft 1.21 renamed these data pack folders to their singular form, so these are "
              + "silently ignored: "
              + offenders);
    }
  }
}
