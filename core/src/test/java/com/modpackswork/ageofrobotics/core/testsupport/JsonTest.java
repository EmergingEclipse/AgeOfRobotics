package com.modpackswork.ageofrobotics.core.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Guards the JSON reader the resource tests depend on.
 *
 * <p>A reader that quietly accepted malformed input would turn every "this file parses" assertion
 * into a no-op, so its own failure modes are worth pinning down.
 */
class JsonTest {

  @Test
  void readsNestedObjectsArraysAndScalars() {
    final Map<String, Object> parsed =
        Json.parseObject(
            """
            {
              "type": "minecraft:crafting_shaped",
              "pattern": ["CRC", "RIR"],
              "key": { "C": { "item": "minecraft:copper_ingot" } },
              "result": { "count": 1, "id": "ageofrobotics:thing" },
              "flag": true,
              "nothing": null
            }
            """);

    assertEquals("minecraft:crafting_shaped", Json.asString(parsed.get("type")));
    assertEquals(List.of("CRC", "RIR"), Json.asList(parsed.get("pattern")));
    assertEquals(
        "minecraft:copper_ingot",
        Json.asString(Json.asObject(Json.asObject(parsed.get("key")).get("C")).get("item")));
    assertEquals(1.0, Json.asObject(parsed.get("result")).get("count"));
    assertEquals(Boolean.TRUE, parsed.get("flag"));
    assertTrue(parsed.containsKey("nothing"));
    assertEquals(null, parsed.get("nothing"));
  }

  @Test
  void unescapesStringContent() {
    assertEquals(
        List.of("a\"b", "c\\d", "e\nf", "\u00a7g"),
        Json.asList(Json.parse("[\"a\\\"b\", \"c\\\\d\", \"e\\nf\", \"\\u00a7g\"]")));
  }

  @Test
  void rejectsMalformedDocumentsInsteadOfReturningSomethingPlausible() {
    for (final String malformed :
        new String[] {
          "{\"a\": 1,}", "{\"a\" 1}", "{\"a\": 1} trailing", "[1, 2", "{\"a\": 1, \"a\": 2}", "{"
        }) {
      assertThrows(
          IllegalArgumentException.class,
          () -> Json.parse(malformed),
          "reader must reject: " + malformed);
    }
  }

  @Test
  void rejectsValuesOfTheWrongShape() {
    assertThrows(IllegalArgumentException.class, () -> Json.parseObject("[]"));
    assertThrows(IllegalArgumentException.class, () -> Json.asList(Json.parse("{}")));
    assertThrows(IllegalArgumentException.class, () -> Json.asString(Json.parse("1")));
  }
}
