/**
 * Minecraft-facing registration and behaviour for the RF power system.
 *
 * <p>Thin glue on top of {@code com.modpackswork.ageofrobotics.core.power}, which holds the rules.
 * {@link com.modpackswork.ageofrobotics.power.AbstractEnergyBlockEntity} is the base class every
 * power block entity extends, {@link com.modpackswork.ageofrobotics.power.BufferedEnergyStorage} is
 * the Forge Energy handler it hands to the world, and {@link
 * com.modpackswork.ageofrobotics.power.PowerCapabilities} is how each later ticket attaches its own
 * block entity type to the capability without touching shared code.
 *
 * <p>RF is carried by the Forge Energy capability alone. The mod ships no cable or conduit of its
 * own, so third party cables move power between our blocks. See {@code
 * docs/design/power-network.md}.
 */
package com.modpackswork.ageofrobotics.power;
