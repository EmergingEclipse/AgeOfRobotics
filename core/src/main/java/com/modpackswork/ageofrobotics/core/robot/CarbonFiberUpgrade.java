package com.modpackswork.ageofrobotics.core.robot;

/**
 * The optional universal upgrade slot every part carries (design doc §4.4).
 *
 * <p>An upgrade contributes flat offsets layered on top of the part's tier-scaled stats, so the
 * same upgrade is proportionally worth more on a low-tier part than a high-tier one.
 *
 * @param flatModifiers the offsets this upgrade adds
 */
public record CarbonFiberUpgrade(StatBlock flatModifiers) {

  /** An empty upgrade slot. */
  public static final CarbonFiberUpgrade NONE = new CarbonFiberUpgrade(StatBlock.empty());

  /**
   * Creates an upgrade contributing the given offsets.
   *
   * @param flatModifiers the offsets to add to the part's stats
   * @return the upgrade
   */
  public static CarbonFiberUpgrade of(StatBlock flatModifiers) {
    return new CarbonFiberUpgrade(flatModifiers);
  }
}
