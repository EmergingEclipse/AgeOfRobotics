package com.modpackswork.ageofrobotics.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.modpackswork.ageofrobotics.core.robot.CircuitTier;
import com.modpackswork.ageofrobotics.core.testsupport.Json;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Guards the resources backing the Basic Electrical Circuit item (issue #15).
 *
 * <p>Minecraft ignores a data pack or asset file at the wrong path without a word of complaint, and
 * 1.21 both renamed the data pack folders to their singular form and renamed a shaped recipe's
 * result field from {@code item} to {@code id}. Neither mistake shows up as a build failure, so it
 * is checked here instead.
 */
class BasicElectricalCircuitResourcesTest {

  private static final Path REPO_ROOT = Path.of(System.getProperty("aor.repoRoot", "."));
  private static final Path MOD_RESOURCES = REPO_ROOT.resolve("mod/src/main/resources");

  private static final String ITEM_PATH = CircuitTier.BASIC.itemPath();
  private static final String RECIPE_FILE =
      "data/" + ModMetadata.MOD_ID + "/recipe/" + ITEM_PATH + ".json";
  private static final String MODEL_FILE =
      "assets/" + ModMetadata.MOD_ID + "/models/item/" + ITEM_PATH + ".json";
  private static final String LANG_FILE = "assets/" + ModMetadata.MOD_ID + "/lang/en_us.json";

  /**
   * Vanilla materials that are not obtainable in the overworld.
   *
   * <p>The {@code minecraft:} namespace on its own does not prove a material is
   * overworld-obtainable because vanilla ships Nether and End materials under the same namespace,
   * so the obvious ones are named here. Issue #15 requires tiers 1 and 2 to stay reachable without
   * leaving the overworld.
   */
  private static final Set<String> NON_OVERWORLD_MATERIALS =
      Set.of(
          "ancient_debris",
          "basalt",
          "blackstone",
          "blaze",
          "chorus",
          "crying_obsidian",
          "ender_pearl",
          "elytra",
          "end_stone",
          "ghast",
          "gilded_blackstone",
          "glowstone",
          "magma",
          "nether",
          "netherite",
          "netherrack",
          "purpur",
          "quartz",
          "shulker",
          "soul_sand",
          "soul_soil",
          "wither");

  private static String read(String relativePath) throws IOException {
    final Path path = MOD_RESOURCES.resolve(relativePath);
    assertTrue(
        Files.isRegularFile(path),
        "expected resource to exist at the 1.21.1 path: " + relativePath);
    return Files.readString(path, StandardCharsets.UTF_8);
  }

  private static int readBigEndianInt(byte[] bytes, int offset) {
    int value = 0;
    for (int index = offset; index < offset + 4; index++) {
      value = (value << 8) | (bytes[index] & 0xFF);
    }
    return value;
  }

  private static Map<String, Object> recipe() throws IOException {
    return Json.parseObject(read(RECIPE_FILE));
  }

  private static List<String> ingredientIds(Map<String, Object> recipe) {
    return Json.asObject(recipe.get("key")).values().stream()
        .map(Json::asObject)
        .map(
            ingredient -> {
              final Object item = ingredient.get("item");
              return Json.asString(item != null ? item : ingredient.get("tag"));
            })
        .toList();
  }

  @Test
  void recipeIsAShapedCraftingRecipeAtTheSingularDataPackPath() throws IOException {
    final Map<String, Object> recipe = recipe();

    assertEquals(
        "minecraft:crafting_shaped",
        Json.asString(recipe.get("type")),
        "the circuit is crafted on a crafting table");
    assertFalse(
        Files.exists(MOD_RESOURCES.resolve("data/" + ModMetadata.MOD_ID + "/recipes")),
        "1.21 renamed the folder to the singular 'recipe'; a 'recipes' folder is ignored");
  }

  @Test
  void recipeResultIsTheRegisteredCircuitItemUnderTheFieldNameLoadedIn1211() throws IOException {
    final Map<String, Object> result = Json.asObject(recipe().get("result"));

    assertFalse(
        result.containsKey("item"),
        "1.21 renamed the shaped recipe result field from 'item' to 'id'");
    assertEquals(
        CircuitTier.BASIC.itemId(),
        Json.asString(result.get("id")),
        "the recipe must produce the item the core model names");
    assertEquals(
        1.0, result.get("count"), "one circuit per craft keeps the tier 1 gate meaningful");
  }

  @Test
  void everyRecipeIngredientIsVanillaAndOverworldObtainable() throws IOException {
    final List<String> ingredients = ingredientIds(recipe());

    assertFalse(ingredients.isEmpty(), "a shaped recipe needs at least one ingredient");
    for (final String ingredient : ingredients) {
      assertTrue(
          ingredient.startsWith("minecraft:"),
          "issue #15 forbids Machine Dimension materials in this recipe, so every ingredient must"
              + " be vanilla, but found: "
              + ingredient);
      final String name = ingredient.substring("minecraft:".length()).toLowerCase(Locale.ROOT);
      for (final String forbidden : NON_OVERWORLD_MATERIALS) {
        assertFalse(
            name.contains(forbidden),
            "tiers 1 and 2 are overworld-only, so the circuit may not need " + ingredient);
      }
    }
  }

  @Test
  void itemModelIsAFlatSpritePointingAtATextureThatIsActuallyOnDisk() throws IOException {
    final Map<String, Object> model = Json.parseObject(read(MODEL_FILE));

    assertEquals(
        "minecraft:item/generated",
        Json.asString(model.get("parent")),
        "a flat item sprite is rendered by the generated parent model");
    final String texture = Json.asString(Json.asObject(model.get("textures")).get("layer0"));
    assertEquals(ModMetadata.MOD_ID + ":item/" + ITEM_PATH, texture);

    final String[] namespaceAndPath = texture.split(":", 2);
    final Path file =
        MOD_RESOURCES.resolve(
            "assets/" + namespaceAndPath[0] + "/textures/" + namespaceAndPath[1] + ".png");
    assertTrue(
        Files.isRegularFile(file),
        "the model references a texture that does not exist, which renders as a missing sprite: "
            + file);
  }

  @Test
  void placeholderTextureIsASmallSixteenBySixteenPng() throws IOException {
    final Path file =
        MOD_RESOURCES.resolve(
            "assets/" + ModMetadata.MOD_ID + "/textures/item/" + ITEM_PATH + ".png");
    assertTrue(Files.isRegularFile(file), "expected a texture at " + file);
    final byte[] bytes = Files.readAllBytes(file);

    assertTrue(bytes.length > 16, "file is too short to be a PNG");
    assertEquals(
        (byte) 0x89, bytes[0], "texture must be a PNG; Minecraft loads no other image format");
    assertEquals("PNG", new String(bytes, 1, 3, StandardCharsets.US_ASCII));
    assertEquals("IHDR", new String(bytes, 12, 4, StandardCharsets.US_ASCII));
    assertEquals(16, readBigEndianInt(bytes, 16), "item sprites are 16 pixels wide");
    assertEquals(16, readBigEndianInt(bytes, 20), "item sprites are 16 pixels tall");
    assertTrue(
        bytes.length < 4096,
        "a 16x16 placeholder has no business being " + bytes.length + " bytes");
  }

  @Test
  void langFileTranslatesTheKeyMinecraftDerivesFromTheRegistryId() throws IOException {
    final Map<String, Object> lang = Json.parseObject(read(LANG_FILE));
    final String key = "item." + ModMetadata.MOD_ID + "." + ITEM_PATH;

    assertTrue(
        lang.containsKey(key),
        "without this key the item shows its raw translation key in game; expected " + key);
    assertEquals("Basic Electrical Circuit", Json.asString(lang.get(key)));
  }

  @Test
  void recipePatternOnlyUsesKeysItDeclares() throws IOException {
    final Map<String, Object> recipe = recipe();
    final Set<String> declared = Json.asObject(recipe.get("key")).keySet();
    final List<Object> pattern = Json.asList(recipe.get("pattern"));

    assertTrue(pattern.size() >= 1 && pattern.size() <= 3, "a crafting grid has one to three rows");
    for (final Object row : pattern) {
      final String cells = Json.asString(row);
      assertTrue(
          cells.length() == Json.asString(pattern.get(0)).length(),
          "every pattern row must be the same width: " + pattern);
      assertTrue(cells.length() <= 3, "a crafting grid row holds at most three cells: " + cells);
      for (final char cell : cells.toCharArray()) {
        assertTrue(
            cell == ' ' || declared.contains(String.valueOf(cell)),
            "pattern uses '" + cell + "' but only " + declared + " is declared in 'key'");
      }
    }
  }
}
