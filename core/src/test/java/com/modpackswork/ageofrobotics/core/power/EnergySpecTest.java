package com.modpackswork.ageofrobotics.core.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** The immutable energy configuration a machine declares once, and the role direction gates. */
class EnergySpecTest {

  @Test
  void aProducerGivesEnergyOutAndNeverTakesItIn() {
    assertFalse(EnergyRole.PRODUCER.acceptsEnergy());
    assertTrue(EnergyRole.PRODUCER.providesEnergy());
  }

  @Test
  void aConsumerTakesEnergyInAndNeverGivesItOut() {
    assertTrue(EnergyRole.CONSUMER.acceptsEnergy());
    assertFalse(EnergyRole.CONSUMER.providesEnergy());
  }

  @Test
  void storageMovesEnergyBothWays() {
    assertTrue(EnergyRole.STORAGE.acceptsEnergy());
    assertTrue(EnergyRole.STORAGE.providesEnergy());
  }

  @Test
  void theProducerFactoryClosesTheInputSide() {
    final EnergySpec spec = EnergySpec.producer(10_000, 80);

    assertEquals(EnergyRole.PRODUCER, spec.role());
    assertEquals(10_000, spec.capacity());
    assertEquals(0, spec.maxReceiveRate());
    assertEquals(80, spec.maxExtractRate());
  }

  @Test
  void theConsumerFactoryClosesTheOutputSide() {
    final EnergySpec spec = EnergySpec.consumer(4_000, 120);

    assertEquals(EnergyRole.CONSUMER, spec.role());
    assertEquals(120, spec.maxReceiveRate());
    assertEquals(0, spec.maxExtractRate());
  }

  @Test
  void theStorageFactoryOpensBothSidesIndependently() {
    final EnergySpec spec = EnergySpec.storage(1_000_000, 500, 2_000);

    assertEquals(EnergyRole.STORAGE, spec.role());
    assertEquals(500, spec.maxReceiveRate());
    assertEquals(2_000, spec.maxExtractRate());
  }

  @Test
  void capacityMustBePositive() {
    assertThrows(
        IllegalArgumentException.class, () -> new EnergySpec(EnergyRole.STORAGE, 0, 10, 10));
    assertThrows(
        IllegalArgumentException.class, () -> new EnergySpec(EnergyRole.STORAGE, -1, 10, 10));
  }

  @Test
  void transferRatesMayNotBeNegative() {
    assertThrows(
        IllegalArgumentException.class, () -> new EnergySpec(EnergyRole.STORAGE, 100, -1, 10));
    assertThrows(
        IllegalArgumentException.class, () -> new EnergySpec(EnergyRole.STORAGE, 100, 10, -1));
  }

  @Test
  void theRoleMustBeGiven() {
    assertThrows(NullPointerException.class, () -> new EnergySpec(null, 100, 10, 10));
  }

  @Test
  void aRoleMayNotContradictItsTransferRates() {
    assertThrows(
        IllegalArgumentException.class, () -> new EnergySpec(EnergyRole.PRODUCER, 100, 10, 10));
    assertThrows(
        IllegalArgumentException.class, () -> new EnergySpec(EnergyRole.PRODUCER, 100, 0, 0));
    assertThrows(
        IllegalArgumentException.class, () -> new EnergySpec(EnergyRole.CONSUMER, 100, 10, 10));
    assertThrows(
        IllegalArgumentException.class, () -> new EnergySpec(EnergyRole.CONSUMER, 100, 0, 0));
    assertThrows(
        IllegalArgumentException.class, () -> new EnergySpec(EnergyRole.STORAGE, 100, 0, 10));
    assertThrows(
        IllegalArgumentException.class, () -> new EnergySpec(EnergyRole.STORAGE, 100, 10, 0));
  }
}
