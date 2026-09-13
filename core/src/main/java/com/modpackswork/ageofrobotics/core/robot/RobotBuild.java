package com.modpackswork.ageofrobotics.core.robot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A set of installed parts and the whole-robot stats they add up to.
 *
 * <p>Stats combine per {@link RobotStat#aggregation()}, so magnitudes stack while capabilities take
 * the best contribution. Two values are derived rather than supplied by any part: the robot's
 * overall tier, which is its weakest structural part, and its work range, which follows from that
 * tier (§4.8).
 */
public final class RobotBuild {

  private final Map<PartSlot, RobotPart> parts;

  private RobotBuild(Map<PartSlot, RobotPart> parts) {
    this.parts = Collections.unmodifiableMap(parts);
  }

  /**
   * Starts assembling a robot.
   *
   * @return a fresh builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the installed parts by slot.
   *
   * @return an unmodifiable view of the assembly
   */
  public Map<PartSlot, RobotPart> parts() {
    return parts;
  }

  /**
   * Returns the structural slots still empty, in slot order.
   *
   * @return the missing slots; empty when the robot is complete
   */
  public List<PartSlot> missingRequiredSlots() {
    final List<PartSlot> missing = new ArrayList<>();
    for (final PartSlot slot : PartSlot.values()) {
      if (slot.requiredForAssembly() && !parts.containsKey(slot)) {
        missing.add(slot);
      }
    }
    return Collections.unmodifiableList(missing);
  }

  /**
   * Returns whether every structural slot is filled.
   *
   * @return true if the robot can function
   */
  public boolean isComplete() {
    return missingRequiredSlots().isEmpty();
  }

  /**
   * Returns the robot's combined stats, including the derived work range once it is complete.
   *
   * @return the aggregated stat block
   */
  public StatBlock totalStats() {
    StatBlock total = StatBlock.empty();
    for (final RobotPart part : parts.values()) {
      total = total.merge(part.effectiveStats());
    }
    if (!isComplete()) {
      return total;
    }
    return total.merge(StatBlock.builder().set(RobotStat.WORK_RANGE, workRange()).build());
  }

  /**
   * Returns the tier the robot as a whole performs at, which is its weakest structural part.
   *
   * @return the overall tier
   * @throws IllegalStateException if the robot is missing structural parts
   */
  public RobotTier overallTier() {
    requireComplete();
    final List<RobotTier> structural = new ArrayList<>();
    parts.forEach(
        (slot, part) -> {
          if (slot.requiredForAssembly()) {
            structural.add(part.tier());
          }
        });
    return RobotTier.lowestOf(structural);
  }

  /**
   * Returns the claimed-area size this robot can be assigned.
   *
   * @return the work range in blocks
   * @throws IllegalStateException if the robot is missing structural parts
   */
  public double workRange() {
    return PartCatalog.workRangeFor(overallTier());
  }

  /**
   * Returns walking speed, which is the legs' speed plus whatever the boots add.
   *
   * @return the effective movement speed
   */
  public double movementSpeed() {
    final StatBlock stats = totalStats();
    return stats.get(RobotStat.MOVEMENT_SPEED) + stats.get(RobotStat.SPEED_MODIFIER);
  }

  /**
   * Returns the robot's intelligence, which comes from the head.
   *
   * @return the intelligence value
   */
  public double intelligence() {
    return totalStats().get(RobotStat.INTELLIGENCE);
  }

  /**
   * Returns how many function modules the torso can hold.
   *
   * @return the module capacity, rounded down
   */
  public int moduleCapacity() {
    return (int) totalStats().get(RobotStat.MODULE_CAPACITY);
  }

  /**
   * Returns whether this robot could hold the given number of function modules.
   *
   * @param moduleCount the number of modules to fit
   * @return true if the torso has room
   */
  public boolean canHoldModules(int moduleCount) {
    return moduleCount <= moduleCapacity();
  }

  /**
   * Returns the behaviour imposed by the installed personality module.
   *
   * @return the behaviour mode, or empty if no chip is installed
   */
  public Optional<BehaviorMode> behaviorMode() {
    final RobotPart chip = parts.get(PartSlot.PERSONALITY_MODULE);
    return chip instanceof PersonalityModulePart personality
        ? Optional.of(personality.mode())
        : Optional.empty();
  }

  private void requireComplete() {
    if (!isComplete()) {
      throw new IllegalStateException(
          "robot is missing structural parts: " + missingRequiredSlots());
    }
  }

  /** Assembles a {@link RobotBuild} one part at a time. */
  public static final class Builder {

    private final Map<PartSlot, RobotPart> parts = new EnumMap<>(PartSlot.class);

    private Builder() {}

    /**
     * Installs a part in the slot it declares.
     *
     * @param part the part to install
     * @return this builder
     * @throws IllegalStateException if that slot is already filled
     */
    public Builder install(RobotPart part) {
      final RobotPart existing = parts.putIfAbsent(part.slot(), part);
      if (existing != null) {
        throw new IllegalStateException(part.slot() + " is already filled by " + existing);
      }
      return this;
    }

    /**
     * Builds the assembly.
     *
     * @return the finished robot build, complete or not
     */
    public RobotBuild build() {
      final Map<PartSlot, RobotPart> snapshot = new EnumMap<>(PartSlot.class);
      snapshot.putAll(parts);
      return new RobotBuild(snapshot);
    }
  }
}
