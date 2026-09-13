package com.modpackswork.ageofrobotics.core.robot;

/**
 * One installed part of a robot.
 *
 * <p>A part's effective contribution is its base stats scaled by its tier, with the upgrade slot's
 * flat modifiers added on top (design doc §4.3, §4.4).
 */
public sealed interface RobotPart permits StatPart, HandsPart, PersonalityModulePart {

  /**
   * Returns the slot this part occupies.
   *
   * @return the slot
   */
  PartSlot slot();

  /**
   * Returns the material tier this part is built from.
   *
   * @return the tier
   */
  RobotTier tier();

  /**
   * Returns the part's unscaled stats.
   *
   * @return the base stat block
   */
  StatBlock baseStats();

  /**
   * Returns the upgrade installed in this part's universal slot.
   *
   * @return the upgrade, or {@link CarbonFiberUpgrade#NONE} if the slot is empty
   */
  CarbonFiberUpgrade upgrade();

  /**
   * Returns what this part actually contributes to the robot.
   *
   * @return the tier-scaled stats plus any flat upgrade modifiers
   */
  default StatBlock effectiveStats() {
    return baseStats().scaled(tier().magnitudeMultiplier()).plusFlat(upgrade().flatModifiers());
  }

  /**
   * Rejects stats the given slot is not allowed to declare.
   *
   * @param slot the slot the stats are declared for
   * @param stats the declared stats
   * @throws IllegalArgumentException if a stat is derived or belongs to an unrelated slot
   */
  static void validateStatsFor(PartSlot slot, StatBlock stats) {
    for (final RobotStat stat : stats.values().keySet()) {
      if (stat.derived()) {
        throw new IllegalArgumentException(
            stat + " is derived for the whole robot and cannot be declared by a part");
      }
      if (!stat.contributors().contains(slot)) {
        throw new IllegalArgumentException(
            slot + " may not contribute " + stat + "; contributors are " + stat.contributors());
      }
    }
  }
}
