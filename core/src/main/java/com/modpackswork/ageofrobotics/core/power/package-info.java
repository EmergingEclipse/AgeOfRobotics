/**
 * The RF power model shared by every machine in the mod (design doc §3.1, §6).
 *
 * <p>A power block is described by one {@link
 * com.modpackswork.ageofrobotics.core.power.EnergySpec}, which pairs a capacity and transfer rates
 * with an {@link com.modpackswork.ageofrobotics.core.power.EnergyRole} saying whether the block
 * produces, consumes or stores RF. The role is the direction gate: it decides on its own whether a
 * face accepts energy or gives it. Live charge lives in an {@link
 * com.modpackswork.ageofrobotics.core.power.EnergyBuffer}, which a block entity owns, persists and
 * exposes to the world.
 *
 * <p>Transfers between blocks follow the push convention in {@link
 * com.modpackswork.ageofrobotics.core.power.EnergyDistribution}. See {@code
 * docs/design/power-network.md} for why RF is carried by the Forge Energy capability alone rather
 * than by a conduit of our own.
 *
 * <p>Nothing here knows about Minecraft, which is what makes it testable. The Minecraft facing glue
 * is in {@code com.modpackswork.ageofrobotics.power} in the mod module.
 *
 * <p>Capacities and rates are supplied by each machine's own ticket and are placeholder values
 * until the balancing pass.
 */
package com.modpackswork.ageofrobotics.core.power;
