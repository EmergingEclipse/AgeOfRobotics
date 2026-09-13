package com.modpackswork.ageofrobotics.core.power;

import java.util.Objects;

/**
 * The fixed energy configuration of one kind of power block.
 *
 * <p>This is the tuning knob each power ticket declares once as a constant and hands to an {@link
 * EnergyBuffer}. It carries no live state, so two solar panels in the world share one spec.
 *
 * <p>Rates are per call, not per tick. The buffer has no clock of its own, so a block that ticks
 * decides how often it offers or asks for energy and the rate caps how much moves each time.
 *
 * <p>Prefer the {@link #producer(int, int)}, {@link #consumer(int, int)} and {@link #storage(int,
 * int, int)} factories over the canonical constructor. They set the unused side to zero for you and
 * make the role obvious at the call site.
 *
 * @param role what this block does with RF, which gates the direction energy may move
 * @param capacity the most RF the block can hold, always positive
 * @param maxReceiveRate the most RF one external receive call may take in, zero for a producer
 * @param maxExtractRate the most RF one external extract call may give out, zero for a consumer
 */
public record EnergySpec(EnergyRole role, int capacity, int maxReceiveRate, int maxExtractRate) {

  /**
   * Validates the configuration.
   *
   * @throws NullPointerException if the role is null
   * @throws IllegalArgumentException if the capacity is not positive, if a rate is negative, or if
   *     the rates contradict the role
   */
  public EnergySpec {
    Objects.requireNonNull(role, "role");
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must be positive but was " + capacity);
    }
    if (maxReceiveRate < 0) {
      throw new IllegalArgumentException(
          "maxReceiveRate may not be negative but was " + maxReceiveRate);
    }
    if (maxExtractRate < 0) {
      throw new IllegalArgumentException(
          "maxExtractRate may not be negative but was " + maxExtractRate);
    }
    if (role.acceptsEnergy() != (maxReceiveRate > 0)) {
      throw new IllegalArgumentException(
          role + " requires a positive maxReceiveRate exactly when it accepts energy");
    }
    if (role.providesEnergy() != (maxExtractRate > 0)) {
      throw new IllegalArgumentException(
          role + " requires a positive maxExtractRate exactly when it provides energy");
    }
  }

  /**
   * Describes a block that makes its own RF and hands it outward.
   *
   * @param capacity the internal buffer size
   * @param maxExtractRate the most RF one external extract call may give out
   * @return the spec, with the input side closed
   */
  public static EnergySpec producer(int capacity, int maxExtractRate) {
    return new EnergySpec(EnergyRole.PRODUCER, capacity, 0, maxExtractRate);
  }

  /**
   * Describes a block that spends RF on work.
   *
   * @param capacity the internal buffer size
   * @param maxReceiveRate the most RF one external receive call may take in
   * @return the spec, with the output side closed
   */
  public static EnergySpec consumer(int capacity, int maxReceiveRate) {
    return new EnergySpec(EnergyRole.CONSUMER, capacity, maxReceiveRate, 0);
  }

  /**
   * Describes a block that buffers RF for the base.
   *
   * @param capacity the internal buffer size
   * @param maxReceiveRate the most RF one external receive call may take in
   * @param maxExtractRate the most RF one external extract call may give out
   * @return the spec, with both sides open
   */
  public static EnergySpec storage(int capacity, int maxReceiveRate, int maxExtractRate) {
    return new EnergySpec(EnergyRole.STORAGE, capacity, maxReceiveRate, maxExtractRate);
  }
}
