package com.modpackswork.ageofrobotics.core.testsupport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A tiny JSON reader for the structural tests that read the mod's resources out of the repo.
 *
 * <p>The {@code core} module is deliberately dependency-free, and the mod's data pack files are the
 * only JSON these tests ever see, so a hundred lines of reader is a better trade than a new
 * dependency on the test classpath. Objects come back as {@link LinkedHashMap}, arrays as {@link
 * ArrayList}, numbers as {@link Double} and everything else as the obvious Java type.
 *
 * <p>It is strict on purpose: anything the reader cannot parse throws, which is what makes "the
 * recipe file parses" a real assertion rather than a substring check.
 */
public final class Json {

  private final String text;
  private int pos;

  private Json(String text) {
    this.text = text;
  }

  /**
   * Parses a JSON document.
   *
   * @param text the document to read
   * @return the parsed value
   * @throws IllegalArgumentException if the document is not well-formed JSON
   */
  public static Object parse(String text) {
    final Json reader = new Json(text);
    reader.skipWhitespace();
    final Object value = reader.readValue();
    reader.skipWhitespace();
    if (reader.pos != text.length()) {
      throw new IllegalArgumentException("trailing content at offset " + reader.pos);
    }
    return value;
  }

  /**
   * Parses a JSON document that must be an object.
   *
   * @param text the document to read
   * @return the parsed object
   * @throws IllegalArgumentException if the document is not a well-formed JSON object
   */
  public static Map<String, Object> parseObject(String text) {
    return asObject(parse(text));
  }

  /**
   * Narrows a parsed value to an object.
   *
   * @param value the value to narrow
   * @return the value as a map
   * @throws IllegalArgumentException if it is not an object
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> asObject(Object value) {
    if (!(value instanceof Map)) {
      throw new IllegalArgumentException("expected a JSON object but got " + value);
    }
    return (Map<String, Object>) value;
  }

  /**
   * Narrows a parsed value to an array.
   *
   * @param value the value to narrow
   * @return the value as a list
   * @throws IllegalArgumentException if it is not an array
   */
  @SuppressWarnings("unchecked")
  public static List<Object> asList(Object value) {
    if (!(value instanceof List)) {
      throw new IllegalArgumentException("expected a JSON array but got " + value);
    }
    return (List<Object>) value;
  }

  /**
   * Narrows a parsed value to a string.
   *
   * @param value the value to narrow
   * @return the value as a string
   * @throws IllegalArgumentException if it is not a string
   */
  public static String asString(Object value) {
    if (!(value instanceof String)) {
      throw new IllegalArgumentException("expected a JSON string but got " + value);
    }
    return (String) value;
  }

  private Object readValue() {
    final char next = peek();
    switch (next) {
      case '{':
        return readObject();
      case '[':
        return readArray();
      case '"':
        return readString();
      case 't':
        expect("true");
        return Boolean.TRUE;
      case 'f':
        expect("false");
        return Boolean.FALSE;
      case 'n':
        expect("null");
        return null;
      default:
        return readNumber();
    }
  }

  private Map<String, Object> readObject() {
    final Map<String, Object> members = new LinkedHashMap<>();
    take('{');
    skipWhitespace();
    if (peek() == '}') {
      take('}');
      return members;
    }
    while (true) {
      skipWhitespace();
      final String name = readString();
      skipWhitespace();
      take(':');
      skipWhitespace();
      if (members.containsKey(name)) {
        throw new IllegalArgumentException("duplicate member " + name);
      }
      members.put(name, readValue());
      skipWhitespace();
      if (peek() == ',') {
        take(',');
        continue;
      }
      take('}');
      return members;
    }
  }

  private List<Object> readArray() {
    final List<Object> elements = new ArrayList<>();
    take('[');
    skipWhitespace();
    if (peek() == ']') {
      take(']');
      return elements;
    }
    while (true) {
      skipWhitespace();
      elements.add(readValue());
      skipWhitespace();
      if (peek() == ',') {
        take(',');
        continue;
      }
      take(']');
      return elements;
    }
  }

  private String readString() {
    final StringBuilder out = new StringBuilder();
    take('"');
    while (true) {
      final char next = next();
      if (next == '"') {
        return out.toString();
      }
      if (next != '\\') {
        out.append(next);
        continue;
      }
      out.append(readEscape());
    }
  }

  private char readEscape() {
    final char escaped = next();
    switch (escaped) {
      case '"':
      case '\\':
      case '/':
        return escaped;
      case 'b':
        return '\b';
      case 'f':
        return '\f';
      case 'n':
        return '\n';
      case 'r':
        return '\r';
      case 't':
        return '\t';
      case 'u':
        final String hex = text.substring(pos, pos + 4);
        pos += 4;
        return (char) Integer.parseInt(hex, 16);
      default:
        throw new IllegalArgumentException("unknown escape \\" + escaped);
    }
  }

  private Double readNumber() {
    final int start = pos;
    while (pos < text.length() && "+-.eE0123456789".indexOf(text.charAt(pos)) >= 0) {
      pos++;
    }
    if (start == pos) {
      throw new IllegalArgumentException("expected a value at offset " + pos);
    }
    return Double.valueOf(text.substring(start, pos));
  }

  private void expect(String literal) {
    if (!text.startsWith(literal, pos)) {
      throw new IllegalArgumentException("expected " + literal + " at offset " + pos);
    }
    pos += literal.length();
  }

  private void skipWhitespace() {
    while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
      pos++;
    }
  }

  private char peek() {
    if (pos >= text.length()) {
      throw new IllegalArgumentException("unexpected end of document");
    }
    return text.charAt(pos);
  }

  private char next() {
    final char current = peek();
    pos++;
    return current;
  }

  private void take(char expected) {
    final char actual = next();
    if (actual != expected) {
      throw new IllegalArgumentException(
          "expected " + expected + " but found " + actual + " at offset " + (pos - 1));
    }
  }
}
