package com.modpackswork.ageofrobotics.core.robot;

/** How a stat combines when more than one part contributes a value for it. */
public enum Aggregation {
  /** Values add up, e.g. two parts each adding hit points. */
  SUM,
  /**
   * The highest value wins, used for capabilities rather than magnitudes: boots that can climb do
   * not make legs that can climb climb twice as well.
   */
  BEST;

  /**
   * Combines two values for the same stat.
   *
   * @param left the value contributed so far
   * @param right the value being added
   * @return the combined value
   */
  public double combine(double left, double right) {
    return this == SUM ? left + right : Math.max(left, right);
  }
}
