package com.modpackswork.ageofrobotics.core.robot;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Every concrete stat field a robot part can contribute (design doc §4.3, §4.4, §4.6, §4.8).
 *
 * <p>Each stat records the slot that owns it, which slots may contribute to it, how multiple
 * contributions combine, and whether its magnitude scales with the part's tier. Flags and modifiers
 * do not scale: a pair of boots either walks on water or it does not.
 */
public enum RobotStat {
  /** Drives autonomy quality and the rogue-risk calculation (§4.6). */
  INTELLIGENCE(PartSlot.HEAD, Aggregation.SUM, true),
  /** How far the robot notices blocks, mobs and job targets. */
  SENSOR_RANGE(PartSlot.HEAD, Aggregation.SUM, true),
  /** How reliably the robot picks the right target for its assigned job. */
  TASK_RECOGNITION_ACCURACY(PartSlot.HEAD, Aggregation.SUM, true),

  /** Total hit points. */
  MAX_HP(PartSlot.TORSO, Aggregation.SUM, true),
  /** Damage resistance, the robot's effective armour. */
  DURABILITY(PartSlot.TORSO, Aggregation.SUM, true),
  /** How many function modules the frame can hold. */
  MODULE_CAPACITY(PartSlot.TORSO, Aggregation.SUM, true),

  /** Mining and attack power delivered through whatever the hands hold. */
  STRENGTH(PartSlot.ARMS, Aggregation.SUM, true),
  /** Inventory space for hauling jobs. */
  CARRY_CAPACITY(PartSlot.ARMS, Aggregation.SUM, true),

  /** Tool-head work speed. */
  WORK_SPEED(PartSlot.HANDS, Aggregation.SUM, true),
  /** Tool-head area of effect, in blocks. */
  AREA_OF_EFFECT(PartSlot.HANDS, Aggregation.BEST, true),
  /** Uses before the equipped head wears out. */
  TOOL_DURABILITY(PartSlot.HANDS, Aggregation.SUM, true),
  /** Weapon-head damage per hit. */
  ATTACK_DAMAGE(PartSlot.HANDS, Aggregation.SUM, true),
  /** Weapon-head attacks per second. */
  ATTACK_SPEED(PartSlot.HANDS, Aggregation.SUM, true),

  /** Base walking speed. */
  MOVEMENT_SPEED(PartSlot.LEGS, Aggregation.SUM, true),
  /**
   * Tier-gated traversal capability: jumping, stairs, climbing, swimming. Boots may raise it beyond
   * what the legs alone manage, so the best contribution wins rather than stacking.
   */
  TERRAIN_TRAVERSAL(
      PartSlot.LEGS, Aggregation.BEST, true, EnumSet.of(PartSlot.LEGS, PartSlot.BOOTS)),

  /** Fall damage reduction. */
  FALL_PROTECTION(PartSlot.BOOTS, Aggregation.SUM, true),
  /** Bonus added to {@link #MOVEMENT_SPEED}. */
  SPEED_MODIFIER(PartSlot.BOOTS, Aggregation.SUM, true),
  /** Flag for special movement such as frost-walking or water-walking. */
  SPECIAL_TRAVERSAL(PartSlot.BOOTS, Aggregation.BEST, false),

  /** Rogue-risk offset contributed by the installed behaviour chip. */
  ROGUE_RISK_MODIFIER(PartSlot.PERSONALITY_MODULE, Aggregation.SUM, false),

  /**
   * Size of the claimed area the robot can be assigned (§4.8). Derived from the robot's overall
   * tier rather than supplied by any single part.
   */
  WORK_RANGE(null, Aggregation.BEST, true);

  private final PartSlot owner;
  private final Aggregation aggregation;
  private final boolean scalesWithTier;
  private final Set<PartSlot> contributors;

  RobotStat(PartSlot owner, Aggregation aggregation, boolean scalesWithTier) {
    this(
        owner,
        aggregation,
        scalesWithTier,
        owner == null ? EnumSet.noneOf(PartSlot.class) : EnumSet.of(owner));
  }

  RobotStat(
      PartSlot owner, Aggregation aggregation, boolean scalesWithTier, Set<PartSlot> contributors) {
    this.owner = owner;
    this.aggregation = aggregation;
    this.scalesWithTier = scalesWithTier;
    this.contributors = Collections.unmodifiableSet(EnumSet.copyOf(contributors));
  }

  /**
   * Returns the slot that is the primary source of this stat.
   *
   * @return the owning slot, or empty for whole-robot derived stats
   */
  public Optional<PartSlot> owner() {
    return Optional.ofNullable(owner);
  }

  /**
   * Returns every slot allowed to declare a value for this stat.
   *
   * @return the contributing slots; empty for derived stats, which no part may declare
   */
  public Set<PartSlot> contributors() {
    return contributors;
  }

  /**
   * Returns how multiple contributions of this stat combine.
   *
   * @return the aggregation rule
   */
  public Aggregation aggregation() {
    return aggregation;
  }

  /**
   * Returns whether this stat's magnitude grows with the part's tier.
   *
   * @return false for flags and behaviour modifiers, true for magnitudes
   */
  public boolean scalesWithTier() {
    return scalesWithTier;
  }

  /**
   * Returns whether this stat is computed for the robot as a whole instead of coming from a part.
   *
   * @return true for derived stats such as {@link #WORK_RANGE}
   */
  public boolean derived() {
    return owner == null;
  }
}
