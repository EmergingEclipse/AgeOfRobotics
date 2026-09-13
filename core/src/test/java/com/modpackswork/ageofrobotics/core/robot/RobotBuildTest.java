package com.modpackswork.ageofrobotics.core.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Assembling parts into a robot and deriving whole-robot stats (design doc §4.3, §4.8). */
class RobotBuildTest {

  private static final double EPSILON = 1.0e-9;

  private static RobotBuild.Builder structural(RobotTier tier) {
    return RobotBuild.builder()
        .install(PartCatalog.defaultPart(PartSlot.HEAD, tier))
        .install(PartCatalog.defaultPart(PartSlot.TORSO, tier))
        .install(PartCatalog.defaultPart(PartSlot.ARMS, tier))
        .install(PartCatalog.defaultPart(PartSlot.LEGS, tier));
  }

  @Test
  void aRobotMissingStructuralPartsReportsExactlyWhichOnesAreMissing() {
    final RobotBuild build =
        RobotBuild.builder().install(PartCatalog.defaultPart(PartSlot.TORSO, RobotTier.T1)).build();

    assertFalse(build.isComplete());
    assertEquals(
        List.of(PartSlot.HEAD, PartSlot.ARMS, PartSlot.LEGS), build.missingRequiredSlots());
  }

  @Test
  void handsBootsAndPersonalityChipAreOptional() {
    final RobotBuild build = structural(RobotTier.T1).build();

    assertTrue(build.isComplete(), "a robot can run without hands, boots or a personality chip");
    assertEquals(List.of(), build.missingRequiredSlots());
    assertEquals(Optional.empty(), build.behaviorMode());
  }

  @Test
  void installingTwoPartsInTheSameSlotIsRejected() {
    final RobotBuild.Builder builder = structural(RobotTier.T1);

    assertThrows(
        IllegalStateException.class,
        () -> builder.install(PartCatalog.defaultPart(PartSlot.TORSO, RobotTier.T2)));
  }

  @Test
  void totalStatsComeFromTheSlotThatOwnsThem() {
    final RobotBuild build = structural(RobotTier.T1).build();
    final RobotPart torso = PartCatalog.defaultPart(PartSlot.TORSO, RobotTier.T1);
    final RobotPart arms = PartCatalog.defaultPart(PartSlot.ARMS, RobotTier.T1);

    assertEquals(
        torso.effectiveStats().get(RobotStat.MAX_HP),
        build.totalStats().get(RobotStat.MAX_HP),
        EPSILON);
    assertEquals(
        arms.effectiveStats().get(RobotStat.STRENGTH),
        build.totalStats().get(RobotStat.STRENGTH),
        EPSILON);
    assertEquals(
        0.0, build.totalStats().get(RobotStat.ATTACK_DAMAGE), EPSILON, "no hands equipped");
  }

  @Test
  void bootsAddTheirSpeedModifierToTheLegsMovementSpeed() {
    final RobotBuild bare = structural(RobotTier.T1).build();
    final RobotBuild booted =
        structural(RobotTier.T1)
            .install(PartCatalog.defaultPart(PartSlot.BOOTS, RobotTier.T1))
            .build();

    final double bootsModifier =
        PartCatalog.defaultPart(PartSlot.BOOTS, RobotTier.T1)
            .effectiveStats()
            .get(RobotStat.SPEED_MODIFIER);

    assertEquals(bare.movementSpeed() + bootsModifier, booted.movementSpeed(), EPSILON);
    assertTrue(booted.movementSpeed() > bare.movementSpeed());
  }

  @Test
  void traversalTakesTheBestCapabilityOfLegsAndBoots() {
    final StatPart weakLegs =
        new StatPart(
            PartSlot.LEGS,
            RobotTier.T1,
            StatBlock.builder()
                .set(RobotStat.MOVEMENT_SPEED, 1.0)
                .set(RobotStat.TERRAIN_TRAVERSAL, 1.0)
                .build(),
            CarbonFiberUpgrade.NONE);
    final StatPart strongBoots =
        new StatPart(
            PartSlot.BOOTS,
            RobotTier.T1,
            StatBlock.builder()
                .set(RobotStat.FALL_PROTECTION, 1.0)
                .set(RobotStat.SPEED_MODIFIER, 0.5)
                .set(RobotStat.SPECIAL_TRAVERSAL, 1.0)
                .set(RobotStat.TERRAIN_TRAVERSAL, 4.0)
                .build(),
            CarbonFiberUpgrade.NONE);

    final RobotBuild build =
        RobotBuild.builder()
            .install(PartCatalog.defaultPart(PartSlot.HEAD, RobotTier.T1))
            .install(PartCatalog.defaultPart(PartSlot.TORSO, RobotTier.T1))
            .install(PartCatalog.defaultPart(PartSlot.ARMS, RobotTier.T1))
            .install(weakLegs)
            .install(strongBoots)
            .build();

    assertEquals(4.0, build.totalStats().get(RobotStat.TERRAIN_TRAVERSAL), EPSILON);
  }

  @Test
  void overallTierIsTheWeakestStructuralPart() {
    final RobotBuild build =
        RobotBuild.builder()
            .install(PartCatalog.defaultPart(PartSlot.HEAD, RobotTier.T6))
            .install(PartCatalog.defaultPart(PartSlot.TORSO, RobotTier.T6))
            .install(PartCatalog.defaultPart(PartSlot.ARMS, RobotTier.T2))
            .install(PartCatalog.defaultPart(PartSlot.LEGS, RobotTier.T6))
            .build();

    assertEquals(RobotTier.T2, build.overallTier());
  }

  @Test
  void workRangeIsDerivedFromOverallTierAndGrowsWithIt() {
    double previous = 0.0;
    for (final RobotTier tier : RobotTier.values()) {
      final RobotBuild build = structural(tier).build();
      final double range = build.workRange();

      assertEquals(range, build.totalStats().get(RobotStat.WORK_RANGE), EPSILON);
      assertTrue(range > previous, "work range must grow at " + tier);
      previous = range;
    }
  }

  @Test
  void workRangeIsUndefinedUntilTheRobotIsComplete() {
    final RobotBuild build =
        RobotBuild.builder().install(PartCatalog.defaultPart(PartSlot.TORSO, RobotTier.T1)).build();

    assertThrows(IllegalStateException.class, build::workRange);
  }

  @Test
  void moduleCapacityComesFromTheTorsoAndLimitsFunctionModules() {
    final RobotBuild build = structural(RobotTier.T1).build();
    final int capacity = build.moduleCapacity();

    assertTrue(capacity >= 1, "even a copper torso holds one module");
    assertEquals(
        (int)
            PartCatalog.defaultPart(PartSlot.TORSO, RobotTier.T1)
                .effectiveStats()
                .get(RobotStat.MODULE_CAPACITY),
        capacity);
    assertTrue(build.canHoldModules(capacity));
    assertFalse(build.canHoldModules(capacity + 1));
  }

  @Test
  void personalityChipSuppliesBehaviourModeAndRogueRiskModifier() {
    final RobotBuild build =
        structural(RobotTier.T1)
            .install(new PersonalityModulePart(RobotTier.T1, BehaviorMode.AGGRESSIVE))
            .build();

    assertEquals(Optional.of(BehaviorMode.AGGRESSIVE), build.behaviorMode());
    assertEquals(
        BehaviorMode.AGGRESSIVE.rogueRiskModifier(),
        build.totalStats().get(RobotStat.ROGUE_RISK_MODIFIER),
        EPSILON);
  }

  @Test
  void intelligenceComesFromTheHeadAndRisesWithItsTier() {
    final double copper = structural(RobotTier.T1).build().intelligence();
    final double titanium = structural(RobotTier.T6).build().intelligence();

    assertTrue(titanium > copper, "a titanium head must be smarter than a copper one");
    assertEquals(
        PartCatalog.defaultPart(PartSlot.HEAD, RobotTier.T1)
            .effectiveStats()
            .get(RobotStat.INTELLIGENCE),
        copper,
        EPSILON);
  }

  @Test
  void installedPartsAreExposedReadOnly() {
    final RobotBuild build = structural(RobotTier.T1).build();

    assertThrows(UnsupportedOperationException.class, () -> build.parts().clear());
  }
}
