package com.modpackswork.ageofrobotics.core.robot;

/**
 * A part whose whole contribution is a block of stats: head, torso, arms, legs or boots.
 *
 * @param slot the slot this part fills
 * @param tier the material tier
 * @param baseStats the unscaled stats, which must belong to {@code slot}
 * @param upgrade the installed upgrade, or {@link CarbonFiberUpgrade#NONE}
 */
public record StatPart(
    PartSlot slot, RobotTier tier, StatBlock baseStats, CarbonFiberUpgrade upgrade)
    implements RobotPart {

  /** Validates that the declared stats belong to the slot. */
  public StatPart {
    RobotPart.validateStatsFor(slot, baseStats);
    RobotPart.validateStatsFor(slot, upgrade.flatModifiers());
  }
}
