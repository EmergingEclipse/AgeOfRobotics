package com.modpackswork.ageofrobotics.core.robot;

import java.util.Collection;
import java.util.Comparator;

/**
 * The six material tiers a robot part can be built from.
 *
 * <p>Tier drives the magnitude of every scaling stat: a tier's {@link #magnitudeMultiplier()} is
 * applied to a part's base stats before any upgrade is layered on. Only the relative shape matters
 * at this stage — the exact curve is tuned in the balancing pass.
 */
public enum RobotTier {
  /** Copper — the entry tier, craftable as soon as the assembly station is running. */
  T1(1, "Copper"),
  /** Iron — first meaningful upgrade, still pre-dimension. */
  T2(2, "Iron"),
  /** Steel — gated behind refined scrap processing. */
  T3(3, "Steel"),
  /** Aluminum — first of the Machine Dimension metals. */
  T4(4, "Aluminum"),
  /** Tungsten — heavy-duty industrial tier. */
  T5(5, "Tungsten"),
  /** Titanium — the ceiling tier, carrying the maximum intelligence a robot may reach. */
  T6(6, "Titanium");

  private static final double GROWTH_PER_TIER = 0.6;

  private final int level;
  private final String material;

  RobotTier(int level, String material) {
    this.level = level;
    this.material = material;
  }

  /**
   * Returns the 1-based tier number, so {@code T1.level() == 1}.
   *
   * @return the tier number, 1 through 6
   */
  public int level() {
    return level;
  }

  /**
   * Returns the material this tier is crafted from, for display and recipe wiring.
   *
   * @return the material name, e.g. {@code "Copper"}
   */
  public String material() {
    return material;
  }

  /**
   * Returns the factor applied to a part's base magnitudes at this tier.
   *
   * @return a multiplier that is 1.0 at {@link #T1} and rises strictly with the tier
   */
  public double magnitudeMultiplier() {
    return 1.0 + GROWTH_PER_TIER * (level - 1);
  }

  /**
   * Returns the weakest tier in a group of parts, which is what a robot as a whole performs at.
   *
   * @param tiers the tiers to compare, must not be empty
   * @return the lowest tier present
   * @throws IllegalArgumentException if {@code tiers} is empty
   */
  public static RobotTier lowestOf(Collection<RobotTier> tiers) {
    return tiers.stream()
        .min(Comparator.comparingInt(RobotTier::level))
        .orElseThrow(() -> new IllegalArgumentException("no tiers given"));
  }
}
