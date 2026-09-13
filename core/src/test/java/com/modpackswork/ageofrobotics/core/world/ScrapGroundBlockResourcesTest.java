package com.modpackswork.ageofrobotics.core.world;

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
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Structural guards for the resource pack side of the scrap ground block set (issue #7).
 *
 * <p>These blocks fill an entire dimension in issue #6, so a missing model or a texture reference
 * that points at nothing is not a cosmetic problem, it is a wall of purple and black checkerboard.
 * Everything here is driven off {@link ScrapGroundBlock}, so a terrain variant added for issue #6
 * or issue #35 is covered the moment its constant lands.
 */
class ScrapGroundBlockResourcesTest {

  private static final Path REPO_ROOT = Path.of(System.getProperty("aor.repoRoot", "."));
  private static final Path RESOURCES = REPO_ROOT.resolve("mod/src/main/resources");
  private static final String NAMESPACE = "ageofrobotics";

  private static Path resource(String relativePath) {
    final Path path = RESOURCES.resolve(relativePath);
    assertTrue(Files.isRegularFile(path), "expected resource to exist: " + path);
    return path;
  }

  private static JsonObject readJson(String relativePath) throws IOException {
    final Path path = resource(relativePath);
    final String text = Files.readString(path, StandardCharsets.UTF_8);
    try {
      return JsonParser.parseString(text).getAsJsonObject();
    } catch (JsonSyntaxException | IllegalStateException e) {
      throw new AssertionError("resource is not a JSON object: " + path, e);
    }
  }

  /** Turns {@code ageofrobotics:block/scrap_block} into the file it must resolve to. */
  private static String modelFile(String modelReference) {
    assertTrue(
        modelReference.startsWith(NAMESPACE + ":"),
        "our blocks must reference our own models, not another namespace: " + modelReference);
    return "assets/"
        + NAMESPACE
        + "/models/"
        + modelReference.substring(NAMESPACE.length() + 1)
        + ".json";
  }

  /** Turns {@code ageofrobotics:block/scrap_block} into the texture file it must resolve to. */
  private static String textureFile(String textureReference) {
    assertTrue(
        textureReference.startsWith(NAMESPACE + ":"),
        "our blocks must reference our own textures, not another namespace: " + textureReference);
    return "assets/"
        + NAMESPACE
        + "/textures/"
        + textureReference.substring(NAMESPACE.length() + 1)
        + ".png";
  }

  /**
   * Collects every {@code model} value in a blockstate file, covering both the single-object and
   * the weighted-array forms vanilla uses.
   */
  private static List<String> modelsNamedBy(JsonObject blockstate) {
    assertTrue(
        blockstate.has("variants"),
        "blockstate must use a variants map; a multipart blockstate needs a different guard");

    final List<String> models = new ArrayList<>();
    for (final Map.Entry<String, JsonElement> variant :
        blockstate.getAsJsonObject("variants").entrySet()) {
      final JsonElement value = variant.getValue();
      if (value.isJsonArray()) {
        final JsonArray weighted = value.getAsJsonArray();
        assertFalse(weighted.isEmpty(), "variant " + variant.getKey() + " lists no models");
        for (final JsonElement entry : weighted) {
          models.add(entry.getAsJsonObject().get("model").getAsString());
        }
      } else {
        models.add(value.getAsJsonObject().get("model").getAsString());
      }
    }
    assertFalse(models.isEmpty(), "blockstate names no models at all");
    return models;
  }

  @ParameterizedTest
  @EnumSource(ScrapGroundBlock.class)
  void blockstateParsesAndNamesModelsThatExist(ScrapGroundBlock block) throws IOException {
    final JsonObject blockstate =
        readJson("assets/" + NAMESPACE + "/blockstates/" + block.path() + ".json");

    for (final String model : modelsNamedBy(blockstate)) {
      assertTrue(
          Files.isRegularFile(RESOURCES.resolve(modelFile(model))),
          "blockstate for " + block.blockId() + " names a model with no file: " + model);
    }
  }

  @ParameterizedTest
  @EnumSource(ScrapGroundBlock.class)
  void blockModelResolvesToTexturesThatExistOnDisk(ScrapGroundBlock block) throws IOException {
    final String modelPath = "assets/" + NAMESPACE + "/models/block/" + block.path() + ".json";
    final JsonObject model = readJson(modelPath);

    assertTrue(model.has("parent"), "block model must inherit a vanilla parent: " + modelPath);
    assertTrue(model.has("textures"), "block model must declare textures: " + modelPath);

    final JsonObject textures = model.getAsJsonObject("textures");
    assertFalse(textures.entrySet().isEmpty(), "block model declares no textures: " + modelPath);

    for (final Map.Entry<String, JsonElement> texture : textures.entrySet()) {
      final String reference = texture.getValue().getAsString();
      if (reference.startsWith("#")) {
        continue;
      }
      assertTrue(
          Files.isRegularFile(RESOURCES.resolve(textureFile(reference))),
          "model "
              + modelPath
              + " points slot "
              + texture.getKey()
              + " at a texture with no PNG on disk: "
              + reference);
    }
  }

  @ParameterizedTest
  @EnumSource(ScrapGroundBlock.class)
  void itemModelExistsAndInheritsTheBlockModel(ScrapGroundBlock block) throws IOException {
    final String itemModelPath = "assets/" + NAMESPACE + "/models/item/" + block.path() + ".json";
    final JsonObject itemModel = readJson(itemModelPath);

    assertTrue(
        itemModel.has("parent"),
        "without a parent the held and inventory item renders as nothing: " + itemModelPath);
    final String parent = itemModel.get("parent").getAsString();
    assertTrue(
        Files.isRegularFile(RESOURCES.resolve(modelFile(parent))),
        "item model " + itemModelPath + " inherits a model with no file: " + parent);
  }

  @ParameterizedTest
  @EnumSource(ScrapGroundBlock.class)
  void langFileTranslatesEveryBlockUnderItsRegistryId(ScrapGroundBlock block) throws IOException {
    final String langPath = "assets/" + NAMESPACE + "/lang/en_us.json";
    final JsonObject lang = readJson(langPath);

    assertTrue(
        lang.has(block.translationKey()),
        "without this key the block shows its raw translation key in game: "
            + block.translationKey());

    final String name = lang.get(block.translationKey()).getAsString();
    assertFalse(name.isBlank(), "translation for " + block.translationKey() + " is blank");
    assertTrue(
        Character.isUpperCase(name.charAt(0)),
        "block names read as Title Case in vanilla: " + name);
  }

  @ParameterizedTest
  @EnumSource(ScrapGroundBlock.class)
  void texturesAreCommittedAndStaySmall(ScrapGroundBlock block) throws IOException {
    final Path texture =
        resource("assets/" + NAMESPACE + "/textures/block/" + block.path() + ".png");

    assertTrue(
        Files.size(texture) < 4096,
        "placeholder terrain textures should stay tiny; "
            + texture
            + " is "
            + Files.size(texture)
            + " bytes");
  }
}
