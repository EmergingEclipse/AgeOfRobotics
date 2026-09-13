package com.modpackswork.ageofrobotics.power;

import com.modpackswork.ageofrobotics.core.power.EnergyBuffer;
import java.util.Objects;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * The Forge Energy view of a core {@link EnergyBuffer}.
 *
 * <p>This is the object handed out through {@code Capabilities.EnergyStorage.BLOCK}, so it is what
 * third party cables, other mods' machines and our own neighbours actually talk to. It holds no
 * rules of its own: every decision about capacity, rate and direction is the buffer's, which is
 * where those rules can be unit tested.
 *
 * <p>Only the external transfer methods are exposed here, which is the point. A cable can fill a
 * battery bank and drain a turbine, but nothing outside the block can reach the internal generate
 * and consume paths.
 */
public final class BufferedEnergyStorage implements IEnergyStorage {

  private final EnergyBuffer buffer;
  private final Runnable onChanged;

  /**
   * Wraps a buffer.
   *
   * @param buffer the charge this view reads and writes
   * @param onChanged run after any transfer that actually moved energy, normally {@code
   *     BlockEntity::setChanged} so the chunk is saved
   */
  public BufferedEnergyStorage(EnergyBuffer buffer, Runnable onChanged) {
    this.buffer = Objects.requireNonNull(buffer, "buffer");
    this.onChanged = Objects.requireNonNull(onChanged, "onChanged");
  }

  @Override
  public int receiveEnergy(int toReceive, boolean simulate) {
    final int accepted = buffer.receive(toReceive, simulate);
    if (!simulate && accepted > 0) {
      onChanged.run();
    }
    return accepted;
  }

  @Override
  public int extractEnergy(int toExtract, boolean simulate) {
    final int given = buffer.extract(toExtract, simulate);
    if (!simulate && given > 0) {
      onChanged.run();
    }
    return given;
  }

  @Override
  public int getEnergyStored() {
    return buffer.stored();
  }

  @Override
  public int getMaxEnergyStored() {
    return buffer.capacity();
  }

  @Override
  public boolean canExtract() {
    return buffer.canExtract();
  }

  @Override
  public boolean canReceive() {
    return buffer.canReceive();
  }
}
