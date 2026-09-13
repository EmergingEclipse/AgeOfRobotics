package com.modpackswork.ageofrobotics.core.power;

/**
 * What a block does with RF, which is what decides the direction each of its faces works in.
 *
 * <p>Every power block in the mod is exactly one of these. The role is a hard gate on the outside
 * world: it is checked before any transfer rate is, so a solar panel can never be charged by a
 * cable and an ore processor can never be drained by one. Internal movement of energy, such as a
 * generator banking what it just made, goes through {@link EnergyBuffer#fill(int)} and {@link
 * EnergyBuffer#drain(int)} instead and is not gated here.
 */
public enum EnergyRole {
  /** Makes its own RF and offers it outward only. Solar panels and steam turbines. */
  PRODUCER(false, true),
  /** Spends RF on work and takes it inward only. Processing machines and the assembly station. */
  CONSUMER(true, false),
  /** Buffers RF for the base and moves it both ways. Battery banks. */
  STORAGE(true, true);

  private final boolean acceptsEnergy;
  private final boolean providesEnergy;

  EnergyRole(boolean acceptsEnergy, boolean providesEnergy) {
    this.acceptsEnergy = acceptsEnergy;
    this.providesEnergy = providesEnergy;
  }

  /**
   * Returns whether a block in this role may be given energy from outside.
   *
   * @return true for consumers and storage, false for producers
   */
  public boolean acceptsEnergy() {
    return acceptsEnergy;
  }

  /**
   * Returns whether a block in this role may have energy taken from it from outside.
   *
   * @return true for producers and storage, false for consumers
   */
  public boolean providesEnergy() {
    return providesEnergy;
  }
}
