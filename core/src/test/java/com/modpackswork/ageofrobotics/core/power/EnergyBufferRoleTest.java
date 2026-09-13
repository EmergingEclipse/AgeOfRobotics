package com.modpackswork.ageofrobotics.core.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The role gate on the outside world, and the internal paths that deliberately bypass it.
 *
 * <p>A generator has to bank what it just made and a machine has to spend what it holds, neither of
 * which is an external transfer, so neither goes through the gate.
 */
class EnergyBufferRoleTest {

  @Test
  void aProducerRefusesEnergyPushedAtIt() {
    final EnergyBuffer panel = new EnergyBuffer(EnergySpec.producer(1_000, 100));

    assertFalse(panel.canReceive());
    assertTrue(panel.canExtract());
    assertEquals(0, panel.receive(500, false));
    assertEquals(0, panel.receive(500, true));
    assertEquals(0, panel.stored());
  }

  @Test
  void aConsumerRefusesToBeDrained() {
    final EnergyBuffer machine = new EnergyBuffer(EnergySpec.consumer(1_000, 100), 800);

    assertTrue(machine.canReceive());
    assertFalse(machine.canExtract());
    assertEquals(0, machine.extract(500, false));
    assertEquals(0, machine.extract(500, true));
    assertEquals(800, machine.stored());
  }

  @Test
  void storageWorksInBothDirections() {
    final EnergyBuffer bank = new EnergyBuffer(EnergySpec.storage(1_000, 100, 250), 500);

    assertTrue(bank.canReceive());
    assertTrue(bank.canExtract());
    assertEquals(100, bank.receive(1_000, false));
    assertEquals(250, bank.extract(1_000, false));
    assertEquals(350, bank.stored());
  }

  @Test
  void aProducerCanStillBankWhatItGeneratedItself() {
    final EnergyBuffer panel = new EnergyBuffer(EnergySpec.producer(1_000, 100));

    final int banked = panel.fill(40);

    assertEquals(40, banked);
    assertEquals(40, panel.stored());
  }

  @Test
  void internalFillIgnoresTheReceiveRateButNotTheCapacity() {
    final EnergyBuffer panel = new EnergyBuffer(EnergySpec.producer(1_000, 100), 950);

    assertEquals(50, panel.fill(10_000));
    assertEquals(1_000, panel.stored());
  }

  @Test
  void aConsumerCanSpendWhatItHolds() {
    final EnergyBuffer machine = new EnergyBuffer(EnergySpec.consumer(1_000, 100), 800);

    final int spent = machine.drain(300);

    assertEquals(300, spent);
    assertEquals(500, machine.stored());
  }

  @Test
  void internalDrainIgnoresTheExtractRateButCannotOverdraw() {
    final EnergyBuffer machine = new EnergyBuffer(EnergySpec.consumer(1_000, 100), 80);

    assertEquals(80, machine.drain(10_000));
    assertEquals(0, machine.stored());
  }

  @Test
  void zeroAndNegativeInternalTransfersMoveNothing() {
    final EnergyBuffer bank = new EnergyBuffer(EnergySpec.storage(1_000, 100, 100), 400);

    assertEquals(0, bank.fill(0));
    assertEquals(0, bank.fill(-50));
    assertEquals(0, bank.drain(0));
    assertEquals(0, bank.drain(-50));
    assertEquals(400, bank.stored());
  }
}
