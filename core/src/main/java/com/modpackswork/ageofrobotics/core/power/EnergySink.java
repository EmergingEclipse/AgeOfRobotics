package com.modpackswork.ageofrobotics.core.power;

/**
 * Anything that will take RF off our hands.
 *
 * <p>The single method matches the receive half of the Forge Energy handler interface on purpose,
 * so the Minecraft side can hand a neighbour's handler straight in as a lambda without an adapter
 * class. That is what lets this package hold the push policy while staying free of Minecraft
 * imports.
 */
@FunctionalInterface
public interface EnergySink {

  /**
   * Offers RF to this sink.
   *
   * @param amount the RF being offered
   * @param simulate true to ask what would be accepted without transferring anything
   * @return the RF accepted, which is zero when the sink is full or refuses energy
   */
  int receiveEnergy(int amount, boolean simulate);
}
