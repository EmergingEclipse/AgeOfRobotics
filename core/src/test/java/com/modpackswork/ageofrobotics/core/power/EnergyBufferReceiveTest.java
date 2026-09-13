package com.modpackswork.ageofrobotics.core.power;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Energy arriving from outside the block: capacity, throttling, simulation and junk input. */
class EnergyBufferReceiveTest {

  private static EnergyBuffer consumerBuffer() {
    return new EnergyBuffer(EnergySpec.consumer(1_000, 100));
  }

  @Test
  void aFreshBufferStartsEmpty() {
    final EnergyBuffer buffer = consumerBuffer();

    assertEquals(0, buffer.stored());
    assertEquals(1_000, buffer.capacity());
    assertEquals(1_000, buffer.space());
  }

  @Test
  void repeatedReceivesFillTheBufferToCapacityAndNoFurther() {
    final EnergyBuffer buffer = consumerBuffer();

    for (int call = 0; call < 20; call++) {
      buffer.receive(100, false);
    }

    assertEquals(1_000, buffer.stored());
    assertEquals(0, buffer.space());
  }

  @Test
  void aNearlyFullBufferAcceptsOnlyWhatStillFits() {
    final EnergyBuffer buffer = new EnergyBuffer(EnergySpec.consumer(1_000, 100), 970);

    final int accepted = buffer.receive(100, false);

    assertEquals(30, accepted);
    assertEquals(1_000, buffer.stored());
  }

  @Test
  void receiveIsThrottledByTheMaxReceiveRate() {
    final EnergyBuffer buffer = consumerBuffer();

    final int accepted = buffer.receive(100_000, false);

    assertEquals(100, accepted);
    assertEquals(100, buffer.stored());
  }

  @Test
  void simulatingAReceiveReportsTheAmountWithoutStoringIt() {
    final EnergyBuffer buffer = consumerBuffer();

    final int reported = buffer.receive(40, true);

    assertEquals(40, reported);
    assertEquals(0, buffer.stored());
  }

  @Test
  void simulatingAReceiveOnANearlyFullBufferReportsThePartialAmount() {
    final EnergyBuffer buffer = new EnergyBuffer(EnergySpec.consumer(1_000, 100), 970);

    assertEquals(30, buffer.receive(100, true));
    assertEquals(970, buffer.stored());
  }

  @Test
  void zeroAndNegativeReceivesMoveNothing() {
    final EnergyBuffer buffer = new EnergyBuffer(EnergySpec.consumer(1_000, 100), 500);

    assertEquals(0, buffer.receive(0, false));
    assertEquals(0, buffer.receive(-250, false));
    assertEquals(500, buffer.stored());
  }

  @Test
  void aStartingChargeAboveCapacityIsClampedRatherThanTrusted() {
    final EnergyBuffer buffer = new EnergyBuffer(EnergySpec.consumer(1_000, 100), 5_000);

    assertEquals(1_000, buffer.stored());
  }

  @Test
  void aNegativeStartingChargeIsClampedToEmpty() {
    final EnergyBuffer buffer = new EnergyBuffer(EnergySpec.consumer(1_000, 100), -7);

    assertEquals(0, buffer.stored());
  }
}
