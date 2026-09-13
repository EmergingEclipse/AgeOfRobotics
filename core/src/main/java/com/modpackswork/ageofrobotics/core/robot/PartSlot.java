package com.modpackswork.ageofrobotics.core.robot;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * The seven part slots a robot is assembled from (design doc §4.3).
 *
 * <p>Head, torso, arms and legs are structural: without them there is no working robot. Hands,
 * boots and the personality module are optional add-ons — a robot with no hands simply cannot do a
 * job that needs a tool, and one with no personality chip falls back to its default behaviour.
 */
public enum PartSlot {
  /** Sensing and decision-making: intelligence, sensor range, task recognition. */
  HEAD(true),
  /** The frame: hit points, durability and how many function modules fit. */
  TORSO(true),
  /** Strength and carry capacity; the attach point for {@link #HANDS}. */
  ARMS(true),
  /** Swappable tool-head or weapon-head. */
  HANDS(false),
  /** Movement speed and terrain traversal. */
  LEGS(true),
  /** Fall protection, a speed modifier and special traversal. */
  BOOTS(false),
  /** The behaviour chip slot. */
  PERSONALITY_MODULE(false);

  private final boolean requiredForAssembly;

  PartSlot(boolean requiredForAssembly) {
    this.requiredForAssembly = requiredForAssembly;
  }

  /**
   * Returns whether a robot is unusable without this slot filled.
   *
   * @return true for the structural slots
   */
  public boolean requiredForAssembly() {
    return requiredForAssembly;
  }

  /**
   * Returns the stats this slot owns, i.e. the fields a part in this slot is the primary source of.
   *
   * <p>Other slots may still contribute to a stat they do not own — boots contribute terrain
   * traversal, which the legs own. See {@link RobotStat#contributors()}.
   *
   * @return an immutable set of owned stats, never empty
   */
  public Set<RobotStat> stats() {
    return OwnedStats.BY_SLOT.get(this);
  }

  /** Lazily built so the two enums can reference each other without an initialisation cycle. */
  private static final class OwnedStats {
    private static final Map<PartSlot, Set<RobotStat>> BY_SLOT = index();

    private OwnedStats() {}

    private static Map<PartSlot, Set<RobotStat>> index() {
      final Map<PartSlot, Set<RobotStat>> bySlot = new EnumMap<>(PartSlot.class);
      for (final PartSlot slot : PartSlot.values()) {
        bySlot.put(slot, EnumSet.noneOf(RobotStat.class));
      }
      Arrays.stream(RobotStat.values())
          .forEach(stat -> stat.owner().ifPresent(slot -> bySlot.get(slot).add(stat)));
      bySlot.replaceAll((slot, stats) -> Collections.unmodifiableSet(stats));
      return Collections.unmodifiableMap(bySlot);
    }
  }
}
