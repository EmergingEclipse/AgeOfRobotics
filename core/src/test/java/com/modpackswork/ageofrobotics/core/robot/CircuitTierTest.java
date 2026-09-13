package com.modpackswork.ageofrobotics.core.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Guards the circuit grade that gates each robot tier (issue #15). */
class CircuitTierTest {

  @Test
  void basicCircuitGatesTierOneAndTierTwo() {
    assertEquals(Optional.of(CircuitTier.BASIC), CircuitTier.requiredFor(RobotTier.T1));
    assertEquals(Optional.of(CircuitTier.BASIC), CircuitTier.requiredFor(RobotTier.T2));
  }

  @Test
  void basicCircuitDoesNotGateTiersThatNeedDimensionMaterials() {
    assertFalse(CircuitTier.BASIC.gates(RobotTier.T3), "T3 is gated by the Advanced circuit (#22)");
    for (final RobotTier tier : new RobotTier[] {RobotTier.T3, RobotTier.T4}) {
      assertEquals(
          Optional.empty(),
          CircuitTier.requiredFor(tier),
          tier + " has no circuit grade until the Advanced circuit lands (#22)");
    }
    for (final RobotTier tier : new RobotTier[] {RobotTier.T5, RobotTier.T6}) {
      assertEquals(
          Optional.empty(),
          CircuitTier.requiredFor(tier),
          tier + " has no circuit grade until the Elite circuit lands (#40)");
    }
  }

  @Test
  void basicCircuitGatesEveryOverworldOnlyTier() {
    assertTrue(CircuitTier.BASIC.gates(RobotTier.T1));
    assertTrue(CircuitTier.BASIC.gates(RobotTier.T2));
    assertEquals(2, CircuitTier.BASIC.gatedTiers().size());
  }

  @Test
  void basicCircuitCarriesTheRegistryIdTheModAndItsRecipeShare() {
    assertEquals("basic_electrical_circuit", CircuitTier.BASIC.itemPath());
    assertEquals("ageofrobotics:basic_electrical_circuit", CircuitTier.BASIC.itemId());
  }

  @Test
  void noTwoCircuitGradesClaimTheSameRobotTier() {
    final Map<RobotTier, CircuitTier> claimed = new EnumMap<>(RobotTier.class);
    for (final CircuitTier circuit : CircuitTier.values()) {
      for (final RobotTier tier : circuit.gatedTiers()) {
        final CircuitTier previous = claimed.put(tier, circuit);
        assertEquals(null, previous, tier + " is claimed by both " + previous + " and " + circuit);
      }
    }
  }
}
