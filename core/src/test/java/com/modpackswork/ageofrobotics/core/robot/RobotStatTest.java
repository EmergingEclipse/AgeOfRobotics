package com.modpackswork.ageofrobotics.core.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** The stat vocabulary itself: every slot contributes something, and ownership is consistent. */
class RobotStatTest {

  @Test
  void everySlotOwnsAtLeastOneStat() {
    for (final PartSlot slot : PartSlot.values()) {
      assertFalse(slot.stats().isEmpty(), slot + " must contribute at least one stat field");
    }
  }

  @Test
  void slotStatsAgreeWithStatOwnership() {
    for (final PartSlot slot : PartSlot.values()) {
      final Set<RobotStat> ownedByStat =
          Arrays.stream(RobotStat.values())
              .filter(stat -> stat.owner().orElse(null) == slot)
              .collect(Collectors.toCollection(() -> EnumSet.noneOf(RobotStat.class)));
      assertEquals(ownedByStat, slot.stats(), "slot/stat ownership disagrees for " + slot);
    }
  }

  @Test
  void workRangeIsTheOnlyWholeRobotDerivedStat() {
    final Set<RobotStat> derived =
        Arrays.stream(RobotStat.values())
            .filter(RobotStat::derived)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(RobotStat.class)));
    assertEquals(EnumSet.of(RobotStat.WORK_RANGE), derived);
  }

  @Test
  void capabilityStatsUseBestOfAggregationRatherThanSum() {
    assertEquals(Aggregation.BEST, RobotStat.TERRAIN_TRAVERSAL.aggregation());
    assertEquals(Aggregation.BEST, RobotStat.SPECIAL_TRAVERSAL.aggregation());
    assertEquals(Aggregation.SUM, RobotStat.MAX_HP.aggregation());
  }

  @Test
  void flagAndModifierStatsDoNotScaleWithTier() {
    assertFalse(RobotStat.SPECIAL_TRAVERSAL.scalesWithTier(), "a traversal flag has no magnitude");
    assertFalse(
        RobotStat.ROGUE_RISK_MODIFIER.scalesWithTier(),
        "rogue risk comes from the personality chip, not the part's tier");
    assertTrue(RobotStat.MAX_HP.scalesWithTier());
    assertTrue(RobotStat.INTELLIGENCE.scalesWithTier());
  }

  @Test
  void tiersRunFromCopperToTitaniumInAscendingOrder() {
    assertEquals(6, RobotTier.values().length);
    assertEquals("Copper", RobotTier.T1.material());
    assertEquals("Titanium", RobotTier.T6.material());
    for (int i = 1; i < RobotTier.values().length; i++) {
      final RobotTier lower = RobotTier.values()[i - 1];
      final RobotTier higher = RobotTier.values()[i];
      assertEquals(lower.level() + 1, higher.level());
      assertTrue(
          higher.magnitudeMultiplier() > lower.magnitudeMultiplier(),
          higher + " must out-scale " + lower);
    }
  }

  @Test
  void toolHeadsMayDeclareWorkAreaDurabilityAndTheTwoHarvestAbilities() {
    assertEquals(
        EnumSet.of(
            RobotStat.WORK_SPEED,
            RobotStat.AREA_OF_EFFECT,
            RobotStat.TOOL_DURABILITY,
            RobotStat.HARVEST_YIELD_BONUS,
            RobotStat.PRECISION_HARVEST),
        EnumSet.copyOf(HandAttachment.TOOL_STATS),
        "a tool-head needs somewhere to record its tier-gated harvest abilities (issue #48)");
  }

  @Test
  void handWorkCapabilitiesStepPerTierInsteadOfScalingSmoothly() {
    assertFalse(
        RobotStat.AREA_OF_EFFECT.scalesWithTier(),
        "an area of effect is a block count, and 1.6 blocks is not a shape");
    assertFalse(RobotStat.HARVEST_YIELD_BONUS.scalesWithTier(), "a yield bonus is tier-gated");
    assertFalse(RobotStat.PRECISION_HARVEST.scalesWithTier(), "precision harvest is a flag");

    assertEquals(PartSlot.HANDS, RobotStat.HARVEST_YIELD_BONUS.owner().orElseThrow());
    assertEquals(PartSlot.HANDS, RobotStat.PRECISION_HARVEST.owner().orElseThrow());
    assertEquals(Aggregation.BEST, RobotStat.HARVEST_YIELD_BONUS.aggregation());
    assertEquals(Aggregation.BEST, RobotStat.PRECISION_HARVEST.aggregation());
  }
}
