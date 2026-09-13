package com.modpackswork.ageofrobotics.core.power;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Energy leaving the block: throttling, running dry, simulation and junk input. */
class EnergyBufferExtractTest {

  private static EnergyBuffer producerBuffer(int stored) {
    return new EnergyBuffer(EnergySpec.producer(1_000, 100), stored);
  }

  @Test
  void extractIsThrottledByTheMaxExtractRate() {
    final EnergyBuffer buffer = producerBuffer(1_000);

    final int given = buffer.extract(100_000, false);

    assertEquals(100, given);
    assertEquals(900, buffer.stored());
  }

  @Test
  void askingForMoreThanIsStoredGivesOnlyWhatIsThere() {
    final EnergyBuffer buffer = producerBuffer(30);

    final int given = buffer.extract(100, false);

    assertEquals(30, given);
    assertEquals(0, buffer.stored());
  }

  @Test
  void anEmptyBufferGivesNothing() {
    final EnergyBuffer buffer = producerBuffer(0);

    assertEquals(0, buffer.extract(100, false));
    assertEquals(0, buffer.stored());
  }

  @Test
  void simulatingAnExtractReportsTheAmountWithoutSpendingIt() {
    final EnergyBuffer buffer = producerBuffer(500);

    final int reported = buffer.extract(60, true);

    assertEquals(60, reported);
    assertEquals(500, buffer.stored());
  }

  @Test
  void simulatingAnExtractFromANearlyEmptyBufferReportsThePartialAmount() {
    final EnergyBuffer buffer = producerBuffer(12);

    assertEquals(12, buffer.extract(100, true));
    assertEquals(12, buffer.stored());
  }

  @Test
  void zeroAndNegativeExtractsMoveNothing() {
    final EnergyBuffer buffer = producerBuffer(500);

    assertEquals(0, buffer.extract(0, false));
    assertEquals(0, buffer.extract(-250, false));
    assertEquals(500, buffer.stored());
  }

  @Test
  void repeatedExtractsDrainTheBufferAndThenStop() {
    final EnergyBuffer buffer = producerBuffer(1_000);

    for (int call = 0; call < 20; call++) {
      buffer.extract(100, false);
    }

    assertEquals(0, buffer.stored());
  }
}
