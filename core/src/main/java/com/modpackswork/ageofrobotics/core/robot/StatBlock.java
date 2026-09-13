package com.modpackswork.ageofrobotics.core.robot;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * An immutable set of stat values.
 *
 * <p>A stat that is not present reads as zero, so callers never have to null-check. Combining two
 * blocks respects each stat's {@link Aggregation}, which is what makes "boots grant better
 * traversal than legs" work without special-casing at every call site.
 */
public final class StatBlock {

  private static final StatBlock EMPTY = new StatBlock(new EnumMap<>(RobotStat.class));

  private final Map<RobotStat, Double> values;

  private static Map<RobotStat, Double> copyOf(Map<RobotStat, Double> source) {
    final Map<RobotStat, Double> copy = new EnumMap<>(RobotStat.class);
    copy.putAll(source);
    return copy;
  }

  private StatBlock(Map<RobotStat, Double> values) {
    this.values = Collections.unmodifiableMap(values);
  }

  /**
   * Returns the empty block, in which every stat reads as zero.
   *
   * @return the shared empty block
   */
  public static StatBlock empty() {
    return EMPTY;
  }

  /**
   * Copies the given values into a new block.
   *
   * @param values the stat values to copy; the caller may keep mutating its own map
   * @return an immutable block holding a snapshot of {@code values}
   */
  public static StatBlock of(Map<RobotStat, Double> values) {
    return new StatBlock(copyOf(values));
  }

  /**
   * Starts building a block stat by stat.
   *
   * @return a fresh builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the value of a stat.
   *
   * @param stat the stat to read
   * @return its value, or zero if this block does not define it
   */
  public double get(RobotStat stat) {
    return values.getOrDefault(stat, 0.0);
  }

  /**
   * Returns the stats this block defines.
   *
   * @return an unmodifiable view, which notably distinguishes "defined as zero" from "absent"
   */
  public Map<RobotStat, Double> values() {
    return values;
  }

  /**
   * Combines this block with another, applying each stat's aggregation rule.
   *
   * @param other the block to combine with
   * @return a new block; neither input is modified
   */
  public StatBlock merge(StatBlock other) {
    final Map<RobotStat, Double> combined = copyOf(values);
    other.values.forEach(
        (stat, value) ->
            combined.merge(stat, value, (left, right) -> stat.aggregation().combine(left, right)));
    return new StatBlock(combined);
  }

  /**
   * Scales the magnitudes in this block, leaving flags and behaviour modifiers untouched.
   *
   * @param factor the tier multiplier to apply
   * @return a new block; this one is not modified
   */
  public StatBlock scaled(double factor) {
    final Map<RobotStat, Double> scaled = new EnumMap<>(RobotStat.class);
    values.forEach(
        (stat, value) -> scaled.put(stat, stat.scalesWithTier() ? value * factor : value));
    return new StatBlock(scaled);
  }

  /**
   * Adds another block's values as flat offsets, ignoring aggregation rules.
   *
   * <p>This is how the Carbon Fiber upgrade is layered on top of a tier value (§4.4).
   *
   * @param flat the offsets to add
   * @return a new block; neither input is modified
   */
  public StatBlock plusFlat(StatBlock flat) {
    final Map<RobotStat, Double> sum = copyOf(values);
    flat.values.forEach((stat, value) -> sum.merge(stat, value, Double::sum));
    return new StatBlock(sum);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof StatBlock block && values.equals(block.values);
  }

  @Override
  public int hashCode() {
    return values.hashCode();
  }

  @Override
  public String toString() {
    return "StatBlock" + values;
  }

  /** Mutable builder for {@link StatBlock}. */
  public static final class Builder {

    private final Map<RobotStat, Double> values = new EnumMap<>(RobotStat.class);

    private Builder() {}

    /**
     * Sets a stat's value, replacing any previous value for it.
     *
     * @param stat the stat to set
     * @param value the value to record
     * @return this builder
     */
    public Builder set(RobotStat stat, double value) {
      values.put(stat, value);
      return this;
    }

    /**
     * Builds the immutable block.
     *
     * @return the finished block
     */
    public StatBlock build() {
      return new StatBlock(copyOf(values));
    }
  }
}
