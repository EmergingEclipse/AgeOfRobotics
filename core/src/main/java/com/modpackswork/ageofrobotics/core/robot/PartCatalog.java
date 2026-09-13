package com.modpackswork.ageofrobotics.core.robot;

/**
 * Placeholder part definitions, one per slot and tier.
 *
 * <p>The numbers here exist so the framework can be exercised end to end and so later tickets have
 * something to build against; they are relative, not balanced. Final values are set in the stat and
 * economy balancing pass.
 */
public final class PartCatalog {

  /** Claimed-area size a tier 1 robot can work, scaled by tier for higher ones (§4.8). */
  private static final double BASE_WORK_RANGE = 8.0;

  private PartCatalog() {}

  /**
   * Returns the claimed-area size a robot of the given overall tier can be assigned.
   *
   * @param tier the robot's overall tier
   * @return the work range in blocks
   */
  public static double workRangeFor(RobotTier tier) {
    return BASE_WORK_RANGE * tier.magnitudeMultiplier();
  }

  /**
   * Returns the stock part for a slot at a tier.
   *
   * @param slot the slot to build a part for
   * @param tier the material tier
   * @return a part occupying {@code slot} at {@code tier}
   */
  public static RobotPart defaultPart(PartSlot slot, RobotTier tier) {
    return switch (slot) {
      case HEAD ->
          statPart(
              slot,
              tier,
              StatBlock.builder()
                  .set(RobotStat.INTELLIGENCE, 10.0)
                  .set(RobotStat.SENSOR_RANGE, 8.0)
                  .set(RobotStat.TASK_RECOGNITION_ACCURACY, 0.2)
                  .build());
      case TORSO ->
          statPart(
              slot,
              tier,
              StatBlock.builder()
                  .set(RobotStat.MAX_HP, 20.0)
                  .set(RobotStat.DURABILITY, 2.0)
                  .set(RobotStat.MODULE_CAPACITY, 1.0)
                  .build());
      case ARMS ->
          statPart(
              slot,
              tier,
              StatBlock.builder()
                  .set(RobotStat.STRENGTH, 3.0)
                  .set(RobotStat.CARRY_CAPACITY, 4.0)
                  .build());
      case LEGS ->
          statPart(
              slot,
              tier,
              StatBlock.builder()
                  .set(RobotStat.MOVEMENT_SPEED, 2.0)
                  .set(RobotStat.TERRAIN_TRAVERSAL, 1.0)
                  .build());
      case BOOTS ->
          statPart(
              slot,
              tier,
              StatBlock.builder()
                  .set(RobotStat.FALL_PROTECTION, 1.0)
                  .set(RobotStat.SPEED_MODIFIER, 0.2)
                  .set(RobotStat.SPECIAL_TRAVERSAL, 0.0)
                  .build());
      case HANDS -> new HandsPart(tier, toolHead(ToolHeadType.PICKAXE), CarbonFiberUpgrade.NONE);
      case PERSONALITY_MODULE -> new PersonalityModulePart(tier, BehaviorMode.NEUTRAL);
    };
  }

  /**
   * Returns the stock tool-head of the given kind.
   *
   * @param type which tool to fit
   * @return a tool-head with placeholder work stats
   */
  public static ToolHead toolHead(ToolHeadType type) {
    return new ToolHead(
        type,
        StatBlock.builder()
            .set(RobotStat.WORK_SPEED, 1.0)
            .set(RobotStat.AREA_OF_EFFECT, 1.0)
            .set(RobotStat.TOOL_DURABILITY, 100.0)
            .build());
  }

  /**
   * Returns the stock weapon-head of the given kind.
   *
   * @param type which weapon to fit
   * @return a weapon-head with placeholder combat stats
   */
  public static WeaponHead weaponHead(WeaponHeadType type) {
    final double damage = type == WeaponHeadType.HUNTING_CLAW ? 2.0 : 4.0;
    final double speed = type == WeaponHeadType.HUNTING_CLAW ? 2.0 : 1.0;
    return new WeaponHead(
        type,
        StatBlock.builder()
            .set(RobotStat.ATTACK_DAMAGE, damage)
            .set(RobotStat.ATTACK_SPEED, speed)
            .set(RobotStat.TOOL_DURABILITY, 120.0)
            .build());
  }

  private static RobotPart statPart(PartSlot slot, RobotTier tier, StatBlock stats) {
    return new StatPart(slot, tier, stats, CarbonFiberUpgrade.NONE);
  }
}
