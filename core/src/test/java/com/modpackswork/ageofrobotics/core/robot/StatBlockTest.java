package com.modpackswork.ageofrobotics.core.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The immutable stat container and its aggregation rules. */
class StatBlockTest {

  private static final double EPSILON = 1.0e-9;

  @Test
  void missingStatsReadAsZero() {
    assertEquals(0.0, StatBlock.empty().get(RobotStat.MAX_HP), EPSILON);
  }

  @Test
  void mergeSumsSummableStatsAndKeepsTheBestCapability() {
    final StatBlock legs =
        StatBlock.builder()
            .set(RobotStat.MOVEMENT_SPEED, 4.0)
            .set(RobotStat.TERRAIN_TRAVERSAL, 2.0)
            .build();
    final StatBlock boots =
        StatBlock.builder()
            .set(RobotStat.MOVEMENT_SPEED, 1.0)
            .set(RobotStat.TERRAIN_TRAVERSAL, 5.0)
            .build();

    final StatBlock merged = legs.merge(boots);

    assertEquals(5.0, merged.get(RobotStat.MOVEMENT_SPEED), EPSILON);
    assertEquals(5.0, merged.get(RobotStat.TERRAIN_TRAVERSAL), EPSILON);
  }

  @Test
  void tierScalingLeavesFlagStatsAlone() {
    final StatBlock block =
        StatBlock.builder()
            .set(RobotStat.MAX_HP, 10.0)
            .set(RobotStat.SPECIAL_TRAVERSAL, 1.0)
            .build();

    final StatBlock scaled = block.scaled(2.5);

    assertEquals(25.0, scaled.get(RobotStat.MAX_HP), EPSILON);
    assertEquals(1.0, scaled.get(RobotStat.SPECIAL_TRAVERSAL), EPSILON);
  }

  @Test
  void flatModifiersAddOnTopOfEveryStatIncludingFlags() {
    final StatBlock block = StatBlock.builder().set(RobotStat.MAX_HP, 10.0).build();
    final StatBlock upgrade = StatBlock.builder().set(RobotStat.MAX_HP, 3.0).build();

    assertEquals(13.0, block.plusFlat(upgrade).get(RobotStat.MAX_HP), EPSILON);
  }

  @Test
  void blocksAreImmutableAndDoNotAliasTheirSource() {
    final Map<RobotStat, Double> source = new EnumMap<>(RobotStat.class);
    source.put(RobotStat.MAX_HP, 10.0);
    final StatBlock block = StatBlock.of(source);

    source.put(RobotStat.MAX_HP, 999.0);
    assertEquals(10.0, block.get(RobotStat.MAX_HP), EPSILON);
    assertThrows(
        UnsupportedOperationException.class, () -> block.values().put(RobotStat.MAX_HP, 999.0));
  }
}
