package com.modpackswork.ageofrobotics.core.robot;

/**
 * The hands slot, whose stats come from whatever head is currently attached.
 *
 * @param tier the material tier of the hands themselves
 * @param attachment the tool-head or weapon-head fitted
 * @param upgrade the installed upgrade, or {@link CarbonFiberUpgrade#NONE}
 */
public record HandsPart(RobotTier tier, HandAttachment attachment, CarbonFiberUpgrade upgrade)
    implements RobotPart {

  /** Validates that the upgrade targets hand stats. */
  public HandsPart {
    RobotPart.validateStatsFor(PartSlot.HANDS, upgrade.flatModifiers());
  }

  @Override
  public PartSlot slot() {
    return PartSlot.HANDS;
  }

  @Override
  public StatBlock baseStats() {
    return attachment.stats();
  }
}
