package com.modpackswork.ageofrobotics.core.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

/** The push convention: a block with energy to spare offers it to its neighbours in order. */
class EnergyDistributionTest {

  /** A stand in for a neighbour's Forge Energy handler, so the policy can be tested without one. */
  private static final class RecordingSink implements EnergySink {

    private final int willAccept;
    private int accepted;
    private int offers;

    private RecordingSink(int willAccept) {
      this.willAccept = willAccept;
    }

    @Override
    public int receiveEnergy(int amount, boolean simulate) {
      offers++;
      final int taken = Math.min(amount, willAccept - accepted);
      if (!simulate) {
        accepted += taken;
      }
      return taken;
    }
  }

  @Test
  void aSingleNeighbourTakesTheWholeBudget() {
    final EnergyBuffer turbine = new EnergyBuffer(EnergySpec.producer(1_000, 100), 1_000);
    final RecordingSink cable = new RecordingSink(10_000);

    final int moved = EnergyDistribution.push(turbine, List.of(cable));

    assertEquals(100, moved);
    assertEquals(100, cable.accepted);
    assertEquals(900, turbine.stored());
  }

  @Test
  void onePushNeverGivesAwayMoreThanTheExtractRate() {
    final EnergyBuffer turbine = new EnergyBuffer(EnergySpec.producer(1_000, 100), 1_000);
    final RecordingSink first = new RecordingSink(10_000);
    final RecordingSink second = new RecordingSink(10_000);

    final int moved = EnergyDistribution.push(turbine, List.of(first, second));

    assertEquals(100, moved);
    assertEquals(100, first.accepted);
    assertEquals(0, second.accepted);
  }

  @Test
  void neighboursAreServedInOrderAndTheLeftoverGoesOnDown() {
    final EnergyBuffer turbine = new EnergyBuffer(EnergySpec.producer(1_000, 100), 1_000);
    final RecordingSink nearlyFull = new RecordingSink(30);
    final RecordingSink empty = new RecordingSink(10_000);

    final int moved = EnergyDistribution.push(turbine, List.of(nearlyFull, empty));

    assertEquals(100, moved);
    assertEquals(30, nearlyFull.accepted);
    assertEquals(70, empty.accepted);
    assertEquals(900, turbine.stored());
  }

  @Test
  void aNeighbourThatRefusesEverythingIsSimplySkipped() {
    final EnergyBuffer turbine = new EnergyBuffer(EnergySpec.producer(1_000, 100), 1_000);
    final RecordingSink refuses = new RecordingSink(0);
    final RecordingSink takes = new RecordingSink(10_000);

    final int moved = EnergyDistribution.push(turbine, List.of(refuses, takes));

    assertEquals(100, moved);
    assertEquals(100, takes.accepted);
  }

  @Test
  void pushingStopsOnceTheBudgetIsGoneWithoutOfferingToTheRest() {
    final EnergyBuffer turbine = new EnergyBuffer(EnergySpec.producer(1_000, 100), 1_000);
    final RecordingSink greedy = new RecordingSink(10_000);
    final RecordingSink untouched = new RecordingSink(10_000);

    EnergyDistribution.push(turbine, List.of(greedy, untouched));

    assertEquals(1, greedy.offers);
    assertEquals(0, untouched.offers);
  }

  @Test
  void aSourceWithLessThanItsRateGivesOnlyWhatItHas() {
    final EnergyBuffer turbine = new EnergyBuffer(EnergySpec.producer(1_000, 100), 25);
    final RecordingSink cable = new RecordingSink(10_000);

    assertEquals(25, EnergyDistribution.push(turbine, List.of(cable)));
    assertEquals(0, turbine.stored());
  }

  @Test
  void anEmptySourcePushesNothingAndBothersNobody() {
    final EnergyBuffer turbine = new EnergyBuffer(EnergySpec.producer(1_000, 100), 0);
    final RecordingSink cable = new RecordingSink(10_000);

    assertEquals(0, EnergyDistribution.push(turbine, List.of(cable)));
    assertEquals(0, cable.offers);
  }

  @Test
  void aConsumerNeverPushesItsWorkingChargeAway() {
    final EnergyBuffer machine = new EnergyBuffer(EnergySpec.consumer(1_000, 100), 900);
    final RecordingSink cable = new RecordingSink(10_000);

    assertEquals(0, EnergyDistribution.push(machine, List.of(cable)));
    assertEquals(0, cable.offers);
    assertEquals(900, machine.stored());
  }

  @Test
  void havingNoNeighboursIsNotAnError() {
    final EnergyBuffer turbine = new EnergyBuffer(EnergySpec.producer(1_000, 100), 1_000);

    assertEquals(0, EnergyDistribution.push(turbine, List.of()));
    assertEquals(1_000, turbine.stored());
  }

  @Test
  void theSourceAndTheNeighbourListAreBothRequired() {
    final EnergyBuffer turbine = new EnergyBuffer(EnergySpec.producer(1_000, 100), 1_000);

    assertThrows(NullPointerException.class, () -> EnergyDistribution.push(null, List.of()));
    assertThrows(NullPointerException.class, () -> EnergyDistribution.push(turbine, null));
  }
}
