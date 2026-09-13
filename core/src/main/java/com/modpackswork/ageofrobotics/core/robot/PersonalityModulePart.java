package com.modpackswork.ageofrobotics.core.robot;

/**
 * The behaviour chip slot.
 *
 * <p>Unlike the other slots this one contributes a mode rather than a magnitude; the only number it
 * carries is its rogue-risk offset.
 *
 * @param tier the material tier of the chip
 * @param mode the behaviour it imposes
 */
public record PersonalityModulePart(RobotTier tier, BehaviorMode mode) implements RobotPart {

  @Override
  public PartSlot slot() {
    return PartSlot.PERSONALITY_MODULE;
  }

  @Override
  public StatBlock baseStats() {
    return StatBlock.builder().set(RobotStat.ROGUE_RISK_MODIFIER, mode.rogueRiskModifier()).build();
  }

  @Override
  public CarbonFiberUpgrade upgrade() {
    return CarbonFiberUpgrade.NONE;
  }
}
