package com.modpackswork.ageofrobotics.core.power;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * The charge surviving a save and a load.
 *
 * <p>A block entity writes {@link EnergyBuffer#stored()} to its tag and reads it back with {@link
 * EnergyBuffer#setStored(int)}. There is no Minecraft here, so these tests stand in for the tag by
 * passing the single int through, which is exactly what the block entity does with it.
 */
class EnergyBufferPersistenceTest {

  private static final EnergySpec BANK = EnergySpec.storage(1_000_000, 5_000, 5_000);

  @Test
  void aPartialChargeSurvivesTheRoundTrip() {
    final EnergyBuffer saved = new EnergyBuffer(BANK, 0);
    saved.fill(437_219);

    final int written = saved.stored();
    final EnergyBuffer loaded = new EnergyBuffer(BANK);
    loaded.setStored(written);

    assertEquals(437_219, loaded.stored());
  }

  @Test
  void anEmptyBufferRoundTripsAsEmpty() {
    final EnergyBuffer loaded = new EnergyBuffer(BANK);
    loaded.setStored(new EnergyBuffer(BANK).stored());

    assertEquals(0, loaded.stored());
  }

  @Test
  void aFullBufferRoundTripsAsFull() {
    final EnergyBuffer saved = new EnergyBuffer(BANK);
    saved.fill(BANK.capacity());

    final EnergyBuffer loaded = new EnergyBuffer(BANK);
    loaded.setStored(saved.stored());

    assertEquals(BANK.capacity(), loaded.stored());
  }

  @Test
  void aSaveFromAnOlderLargerCapacityIsClampedRatherThanTrusted() {
    final EnergyBuffer loaded = new EnergyBuffer(EnergySpec.storage(1_000, 100, 100));

    loaded.setStored(50_000);

    assertEquals(1_000, loaded.stored());
  }

  @Test
  void aCorruptNegativeSaveIsClampedToEmpty() {
    final EnergyBuffer loaded = new EnergyBuffer(BANK, 900);

    loaded.setStored(-1);

    assertEquals(0, loaded.stored());
  }

  @Test
  void theChargeRoundTripsRepeatedlyWithoutDrift() {
    EnergyBuffer buffer = new EnergyBuffer(BANK, 12_345);

    for (int save = 0; save < 5; save++) {
      final EnergyBuffer next = new EnergyBuffer(BANK);
      next.setStored(buffer.stored());
      buffer = next;
    }

    assertEquals(12_345, buffer.stored());
  }
}
