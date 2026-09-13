package com.modpackswork.ageofrobotics.core.robot;

/**
 * A work attachment for the hands slot.
 *
 * @param type which tool the head is
 * @param stats the work stats it contributes; combat stats are rejected
 */
public record ToolHead(ToolHeadType type, StatBlock stats) implements HandAttachment {

  /** Validates that a tool-head declares only tool stats. */
  public ToolHead {
    HandAttachment.validate("a tool-head", HandAttachment.TOOL_STATS, stats);
  }
}
