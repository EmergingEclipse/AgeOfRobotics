package com.modpackswork.ageofrobotics.core.robot;

/**
 * The hands slot, whose stats come from whatever head is currently attached.
 *
 * <p>The upgrade slot belongs to the hands, not to the head, so swapping heads keeps the upgrade.
 * It may raise a work or combat rate but never a tier-gated capability such as area of effect,
 * which would let a player buy a top-tier ability on tier 1 hands (tool-head spec).
 *
 * @param tier the material tier of the hands themselves
 * @param attachment the tool-head or weapon-head fitted
 * @param upgrade the installed upgrade, or {@link CarbonFiberUpgrade#NONE}
 */
public record HandsPart(RobotTier tier, HandAttachment attachment, CarbonFiberUpgrade upgrade)
    implements RobotPart {

  /** Validates that the upgrade targets hand stats and only the ones that are magnitudes. */
  public HandsPart {
    RobotPart.validateStatsFor(PartSlot.HANDS, upgrade.flatModifiers());
    for (final RobotStat stat : upgrade.flatModifiers().values().keySet()) {
      if (!stat.scalesWithTier()) {
        throw new IllegalArgumentException(
            stat + " is a tier-gated capability and cannot be granted by a hands upgrade");
      }
    }
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
