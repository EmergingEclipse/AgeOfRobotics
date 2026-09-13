/**
 * Modular robot parts and the stats they contribute (design doc §4.3, §4.4, §4.6, §4.8).
 *
 * <p>A robot is an assembly of {@link com.modpackswork.ageofrobotics.core.robot.RobotPart}s, one
 * per {@link com.modpackswork.ageofrobotics.core.robot.PartSlot}. Each part carries a {@link
 * com.modpackswork.ageofrobotics.core.robot.StatBlock} of base magnitudes, scaled by the part's
 * {@link com.modpackswork.ageofrobotics.core.robot.RobotTier} and then offset by an optional flat
 * {@link com.modpackswork.ageofrobotics.core.robot.CarbonFiberUpgrade}. {@link
 * com.modpackswork.ageofrobotics.core.robot.RobotBuild} combines the installed parts into
 * whole-robot stats.
 *
 * <p>Numbers here are deliberately placeholder values; they are tuned in the balancing pass.
 */
package com.modpackswork.ageofrobotics.core.robot;
