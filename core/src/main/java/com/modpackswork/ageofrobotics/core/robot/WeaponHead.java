package com.modpackswork.ageofrobotics.core.robot;

/**
 * A combat attachment for the hands slot.
 *
 * @param type which weapon the head is
 * @param stats the combat stats it contributes; work stats are rejected
 */
public record WeaponHead(WeaponHeadType type, StatBlock stats) implements HandAttachment {

  /** Validates that a weapon-head declares only combat stats. */
  public WeaponHead {
    HandAttachment.validate("a weapon-head", HandAttachment.WEAPON_STATS, stats);
  }
}
