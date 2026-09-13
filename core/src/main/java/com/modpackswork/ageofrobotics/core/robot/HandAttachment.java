package com.modpackswork.ageofrobotics.core.robot;

import java.util.Set;

/**
 * What the hands are holding: a tool-head for work, or a weapon-head for combat.
 *
 * <p>Full per-head design lives in its own tickets; this framework only fixes which stats each kind
 * of head may carry, so a pickaxe can never quietly grant attack damage.
 */
public sealed interface HandAttachment permits ToolHead, WeaponHead {

  /** Stats a tool-head may declare. */
  Set<RobotStat> TOOL_STATS =
      Set.of(RobotStat.WORK_SPEED, RobotStat.AREA_OF_EFFECT, RobotStat.TOOL_DURABILITY);

  /** Stats a weapon-head may declare. */
  Set<RobotStat> WEAPON_STATS =
      Set.of(RobotStat.ATTACK_DAMAGE, RobotStat.ATTACK_SPEED, RobotStat.TOOL_DURABILITY);

  /**
   * Returns the stats this attachment contributes.
   *
   * @return the attachment's stat block
   */
  StatBlock stats();

  /**
   * Rejects stats outside the allowed set for a kind of head.
   *
   * @param kind a human-readable name for the head kind, used in the error message
   * @param allowed the stats this kind of head may declare
   * @param stats the declared stats
   * @throws IllegalArgumentException if a declared stat is not allowed
   */
  static void validate(String kind, Set<RobotStat> allowed, StatBlock stats) {
    for (final RobotStat stat : stats.values().keySet()) {
      if (!allowed.contains(stat)) {
        throw new IllegalArgumentException(kind + " may not declare " + stat);
      }
    }
  }
}
