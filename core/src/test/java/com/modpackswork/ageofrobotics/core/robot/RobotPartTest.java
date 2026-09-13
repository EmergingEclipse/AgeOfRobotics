package com.modpackswork.ageofrobotics.core.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Per-tier magnitudes and the optional Carbon Fiber flat modifier (design doc §4.3, §4.4). */
class RobotPartTest {

  private static final double EPSILON = 1.0e-9;

  @Test
  void everySlotHasPlaceholderStatsForAllSixTiers() {
    for (final PartSlot slot : PartSlot.values()) {
      for (final RobotTier tier : RobotTier.values()) {
        final RobotPart part = PartCatalog.defaultPart(slot, tier);
        assertEquals(slot, part.slot());
        assertEquals(tier, part.tier());

        final StatBlock stats = part.effectiveStats();
        assertFalse(stats.values().isEmpty(), slot + " at " + tier + " defines no stats");
        for (final RobotStat stat : stats.values().keySet()) {
          assertTrue(stat.contributors().contains(slot), slot + " may not define " + stat);
          if (stat.scalesWithTier()) {
            assertTrue(
                stats.get(stat) > 0.0, slot + " at " + tier + " needs a magnitude for " + stat);
          }
        }
      }
    }
  }

  @Test
  void magnitudesRiseStrictlyFromTier1ToTier6() {
    for (final PartSlot slot : PartSlot.values()) {
      final StatBlock tier1 = PartCatalog.defaultPart(slot, RobotTier.T1).effectiveStats();
      for (final RobotStat stat : tier1.values().keySet()) {
        if (!stat.scalesWithTier()) {
          continue;
        }
        for (int i = 1; i < RobotTier.values().length; i++) {
          final double lower =
              PartCatalog.defaultPart(slot, RobotTier.values()[i - 1]).effectiveStats().get(stat);
          final double higher =
              PartCatalog.defaultPart(slot, RobotTier.values()[i]).effectiveStats().get(stat);
          assertTrue(
              higher > lower,
              stat + " on " + slot + " must grow from tier " + i + " to tier " + (i + 1));
        }
      }
    }
  }

  @Test
  void withoutAnUpgradeEffectiveStatsAreJustTheTierValue() {
    final StatPart torso =
        new StatPart(
            PartSlot.TORSO,
            RobotTier.T2,
            StatBlock.builder().set(RobotStat.MAX_HP, 10.0).build(),
            CarbonFiberUpgrade.NONE);

    assertEquals(
        10.0 * RobotTier.T2.magnitudeMultiplier(),
        torso.effectiveStats().get(RobotStat.MAX_HP),
        EPSILON);
  }

  @Test
  void carbonFiberUpgradeAddsAFlatBonusOnTopOfTheTierValue() {
    final StatBlock base = StatBlock.builder().set(RobotStat.MAX_HP, 10.0).build();
    final CarbonFiberUpgrade upgrade =
        CarbonFiberUpgrade.of(StatBlock.builder().set(RobotStat.MAX_HP, 4.0).build());

    final StatPart plain =
        new StatPart(PartSlot.TORSO, RobotTier.T3, base, CarbonFiberUpgrade.NONE);
    final StatPart upgraded = new StatPart(PartSlot.TORSO, RobotTier.T3, base, upgrade);

    assertEquals(
        plain.effectiveStats().get(RobotStat.MAX_HP) + 4.0,
        upgraded.effectiveStats().get(RobotStat.MAX_HP),
        EPSILON,
        "the upgrade is a flat modifier layered on the tier value, not another multiplier");
  }

  @Test
  void aPartRejectsStatsThatBelongToAnotherSlot() {
    final StatBlock wrongSlotStats = StatBlock.builder().set(RobotStat.MAX_HP, 10.0).build();

    assertThrows(
        IllegalArgumentException.class,
        () -> new StatPart(PartSlot.LEGS, RobotTier.T1, wrongSlotStats, CarbonFiberUpgrade.NONE),
        "MAX_HP belongs to the torso, not the legs");
  }

  @Test
  void bootsMayContributeTraversalEvenThoughLegsOwnTheStat() {
    final StatPart boots =
        new StatPart(
            PartSlot.BOOTS,
            RobotTier.T1,
            StatBlock.builder().set(RobotStat.TERRAIN_TRAVERSAL, 3.0).build(),
            CarbonFiberUpgrade.NONE);

    assertEquals(3.0, boots.effectiveStats().get(RobotStat.TERRAIN_TRAVERSAL), EPSILON);
  }

  @Test
  void handsCarryAToolHeadOrAWeaponHeadButNeverBoth() {
    final HandsPart mining =
        new HandsPart(
            RobotTier.T1, PartCatalog.toolHead(ToolHeadType.PICKAXE), CarbonFiberUpgrade.NONE);
    final HandsPart fighting =
        new HandsPart(
            RobotTier.T1, PartCatalog.weaponHead(WeaponHeadType.SWORD), CarbonFiberUpgrade.NONE);

    assertTrue(mining.effectiveStats().get(RobotStat.WORK_SPEED) > 0.0);
    assertEquals(0.0, mining.effectiveStats().get(RobotStat.ATTACK_DAMAGE), EPSILON);

    assertTrue(fighting.effectiveStats().get(RobotStat.ATTACK_DAMAGE) > 0.0);
    assertEquals(0.0, fighting.effectiveStats().get(RobotStat.WORK_SPEED), EPSILON);
  }

  @Test
  void aHandsUpgradeBoostsWorkRatesButCannotBuyATierGatedCapability() {
    final ToolHead pickaxe = PartCatalog.toolHead(ToolHeadType.PICKAXE);
    final CarbonFiberUpgrade allowed =
        CarbonFiberUpgrade.of(
            StatBlock.builder()
                .set(RobotStat.WORK_SPEED, 1.0)
                .set(RobotStat.TOOL_DURABILITY, 100.0)
                .build());

    final HandsPart upgraded = new HandsPart(RobotTier.T1, pickaxe, allowed);
    assertEquals(
        pickaxe.stats().get(RobotStat.WORK_SPEED) + 1.0,
        upgraded.effectiveStats().get(RobotStat.WORK_SPEED),
        EPSILON);

    for (final RobotStat gated :
        new RobotStat[] {
          RobotStat.AREA_OF_EFFECT, RobotStat.HARVEST_YIELD_BONUS, RobotStat.PRECISION_HARVEST
        }) {
      final CarbonFiberUpgrade forSale =
          CarbonFiberUpgrade.of(StatBlock.builder().set(gated, 1.0).build());
      assertThrows(
          IllegalArgumentException.class,
          () -> new HandsPart(RobotTier.T1, pickaxe, forSale),
          gated + " is gated by tier and must not be purchasable with an upgrade");
    }
  }

  @Test
  void aToolHeadCannotDeclareCombatStats() {
    final StatBlock combatStats = StatBlock.builder().set(RobotStat.ATTACK_DAMAGE, 5.0).build();

    assertThrows(IllegalArgumentException.class, () -> new ToolHead(ToolHeadType.AXE, combatStats));
  }

  @Test
  void personalityModuleContributesABehaviourModeNotAMagnitude() {
    final PersonalityModulePart chip = new PersonalityModulePart(RobotTier.T1, BehaviorMode.HUNTER);

    assertEquals(PartSlot.PERSONALITY_MODULE, chip.slot());
    assertEquals(BehaviorMode.HUNTER, chip.mode());
    assertEquals(
        BehaviorMode.HUNTER.rogueRiskModifier(),
        chip.effectiveStats().get(RobotStat.ROGUE_RISK_MODIFIER),
        EPSILON);
    assertTrue(
        BehaviorMode.PASSIVE.rogueRiskModifier() < BehaviorMode.HUNTER.rogueRiskModifier(),
        "a passive chip must be safer than a hunter chip");
  }
}
