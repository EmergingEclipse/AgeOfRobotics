package com.modpackswork.ageofrobotics.core.robot;

import com.modpackswork.ageofrobotics.core.ModMetadata;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * The grade of electrical circuit a robot part needs on top of its tier metal (design doc §4.4).
 *
 * <p>Circuits are the second half of every robot part recipe: a part is its tier metal plus the
 * circuit grade covering that tier. Modelling the mapping here rather than in the recipe JSON keeps
 * one machine-readable statement of which tiers are reachable without the Machine Dimension, which
 * is the rule issue #15 exists to protect.
 *
 * <p>Only {@link #BASIC} exists so far. The Advanced grade covering tiers 3 and 4 arrives with
 * issue #22 and the Elite grade covering tiers 5 and 6 with issue #40, at which point they are
 * added as further constants here; {@link #requiredFor(RobotTier)} returns empty for a tier no
 * grade covers yet rather than pretending a circuit exists.
 */
public enum CircuitTier {
  /**
   * Craftable from overworld materials only, and the gate on tiers 1 and 2, which are deliberately
   * reachable before the Machine Dimension.
   */
  BASIC("basic_electrical_circuit", RobotTier.T1, RobotTier.T2);

  private final String itemPath;
  private final Set<RobotTier> gatedTiers;

  CircuitTier(String itemPath, RobotTier... gatedTiers) {
    this.itemPath = itemPath;
    this.gatedTiers = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(gatedTiers)));
  }

  /**
   * Returns the registry path of the item backing this grade.
   *
   * <p>The {@code mod} module registers its item under this exact path, so the core model, the item
   * registration and the recipe JSON cannot drift apart.
   *
   * @return the lowercase registry path, e.g. {@code basic_electrical_circuit}
   */
  public String itemPath() {
    return itemPath;
  }

  /**
   * Returns the namespaced registry id of the item backing this grade.
   *
   * @return the id, e.g. {@code ageofrobotics:basic_electrical_circuit}
   */
  public String itemId() {
    return ModMetadata.id(itemPath);
  }

  /**
   * Returns the robot tiers whose parts are built with this circuit grade.
   *
   * @return an immutable, non-empty set of tiers
   */
  public Set<RobotTier> gatedTiers() {
    return gatedTiers;
  }

  /**
   * Returns whether parts at the given tier are built with this circuit grade.
   *
   * @param tier the robot tier to check
   * @return true if this grade is the one that tier's part recipes call for
   */
  public boolean gates(RobotTier tier) {
    return gatedTiers.contains(tier);
  }

  /**
   * Returns the circuit grade a part of the given tier is built with.
   *
   * @param tier the robot tier whose part recipes are being built
   * @return the grade covering {@code tier}, or empty if no implemented grade covers it yet
   */
  public static Optional<CircuitTier> requiredFor(RobotTier tier) {
    for (final CircuitTier circuit : values()) {
      if (circuit.gates(tier)) {
        return Optional.of(circuit);
      }
    }
    return Optional.empty();
  }
}
