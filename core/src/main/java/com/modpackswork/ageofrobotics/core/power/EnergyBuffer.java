package com.modpackswork.ageofrobotics.core.power;

import java.util.Objects;

/**
 * The live RF charge of one power block.
 *
 * <p>This is the only mutable piece of the power model and the only piece that needs testing, which
 * is why it lives here rather than in a block entity. A block entity owns one of these, persists
 * {@link #stored()} across save and load, and exposes it to the world through the Forge Energy
 * capability.
 *
 * <p>Not thread safe, and deliberately so. Block entities tick on the server thread.
 */
public final class EnergyBuffer {

  private final EnergySpec spec;
  private int stored;

  /**
   * Creates an empty buffer.
   *
   * @param spec the configuration this buffer follows
   */
  public EnergyBuffer(EnergySpec spec) {
    this(spec, 0);
  }

  /**
   * Creates a buffer holding a starting charge, which is how a loaded block restores itself.
   *
   * @param spec the configuration this buffer follows
   * @param stored the starting charge, clamped into the range zero to capacity
   */
  public EnergyBuffer(EnergySpec spec, int stored) {
    this.spec = Objects.requireNonNull(spec, "spec");
    this.stored = clamp(stored);
  }

  private int clamp(int value) {
    return Math.max(0, Math.min(spec.capacity(), value));
  }

  /**
   * Returns the configuration this buffer follows.
   *
   * @return the spec
   */
  public EnergySpec spec() {
    return spec;
  }

  /**
   * Returns the charge currently held.
   *
   * @return the stored RF, never negative and never above capacity
   */
  public int stored() {
    return stored;
  }

  /**
   * Replaces the charge outright, for restoring a block from its saved tag.
   *
   * <p>Nothing else should call this. Every gameplay path goes through {@link #receive(int,
   * boolean)}, {@link #extract(int, boolean)}, {@link #fill(int)} or {@link #drain(int)}. The value
   * is clamped, so a save written when the block held more than the current capacity loads as full
   * rather than corrupting the buffer.
   *
   * @param value the charge to restore, clamped into the range zero to capacity
   */
  public void setStored(int value) {
    this.stored = clamp(value);
  }

  /**
   * Returns the most RF this buffer can hold.
   *
   * @return the capacity
   */
  public int capacity() {
    return spec.capacity();
  }

  /**
   * Returns how much more RF would fit.
   *
   * @return capacity minus stored
   */
  public int space() {
    return spec.capacity() - stored;
  }

  /**
   * Takes energy offered by a neighbour or a cable.
   *
   * <p>The amount moved is the smallest of what was offered, the remaining space and the spec's max
   * receive rate. A role that does not accept energy takes nothing.
   *
   * @param amount the RF being offered; zero and negative amounts move nothing
   * @param simulate true to report what would move without moving it
   * @return the RF accepted, which is what the caller should deduct from its own buffer
   */
  public int receive(int amount, boolean simulate) {
    if (!canReceive() || amount <= 0) {
      return 0;
    }
    final int accepted = Math.min(amount, Math.min(spec.maxReceiveRate(), space()));
    if (!simulate) {
      stored += accepted;
    }
    return accepted;
  }

  /**
   * Gives energy to a neighbour or a cable that asked for it.
   *
   * <p>The amount moved is the smallest of what was asked for, what is stored and the spec's max
   * extract rate. A role that does not provide energy gives nothing.
   *
   * @param amount the RF being asked for; zero and negative amounts move nothing
   * @param simulate true to report what would move without moving it
   * @return the RF given, which is what the caller may add to its own buffer
   */
  public int extract(int amount, boolean simulate) {
    if (!canExtract() || amount <= 0) {
      return 0;
    }
    final int given = Math.min(amount, Math.min(spec.maxExtractRate(), stored));
    if (!simulate) {
      stored -= given;
    }
    return given;
  }

  /**
   * Banks energy the block produced itself.
   *
   * <p>This is the internal path and is not gated by the role or the receive rate, because a solar
   * panel that refuses external energy still has to store its own output. Only the capacity
   * applies.
   *
   * @param amount the RF to bank; zero and negative amounts move nothing
   * @return the RF actually banked, which is less than asked for once the buffer is full
   */
  public int fill(int amount) {
    if (amount <= 0) {
      return 0;
    }
    final int banked = Math.min(amount, space());
    stored += banked;
    return banked;
  }

  /**
   * Spends energy on the block's own work.
   *
   * <p>This is the internal path and is not gated by the role or the extract rate, because a
   * machine that refuses to be drained by a cable still has to pay for its own operation. Callers
   * that need to know whether the block can afford an operation before starting it should compare
   * the cost against {@link #stored()}.
   *
   * @param amount the RF to spend; zero and negative amounts move nothing
   * @return the RF actually spent, which is less than asked for once the buffer runs dry
   */
  public int drain(int amount) {
    if (amount <= 0) {
      return 0;
    }
    final int spent = Math.min(amount, stored);
    stored -= spent;
    return spent;
  }

  /**
   * Returns whether this buffer accepts energy from outside at all.
   *
   * @return true when the role accepts energy and the receive rate is positive
   */
  public boolean canReceive() {
    return spec.role().acceptsEnergy() && spec.maxReceiveRate() > 0;
  }

  /**
   * Returns whether this buffer gives energy to the outside at all.
   *
   * @return true when the role provides energy and the extract rate is positive
   */
  public boolean canExtract() {
    return spec.role().providesEnergy() && spec.maxExtractRate() > 0;
  }
}
