package com.modpackswork.ageofrobotics.core.robot;

/**
 * The behaviour a personality module imposes on a robot (design doc §4.3).
 *
 * <p>The behaviour logic itself is a separate ticket; what this framework fixes is the set of modes
 * and the rogue-risk offset each one carries, so the intelligence and rogue-risk system has
 * something concrete to read.
 */
public enum BehaviorMode {
  /** Never fights back; the safest chip to install in a high-intelligence robot. */
  PASSIVE(-0.10),
  /** Retaliates when attacked. */
  NEUTRAL(0.0),
  /** Attacks hostiles on sight within sensor range. */
  AGGRESSIVE(0.10),
  /** Actively seeks targets out; the riskiest chip. */
  HUNTER(0.20);

  private final double rogueRiskModifier;

  BehaviorMode(double rogueRiskModifier) {
    this.rogueRiskModifier = rogueRiskModifier;
  }

  /**
   * Returns how much this chip shifts the robot's chance of going rogue.
   *
   * @return the offset, negative for calming chips
   */
  public double rogueRiskModifier() {
    return rogueRiskModifier;
  }
}
