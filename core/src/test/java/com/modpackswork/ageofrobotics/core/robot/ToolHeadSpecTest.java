package com.modpackswork.ageofrobotics.core.robot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Keeps the tool-head design spec honest against the code it will be implemented in (issue #48).
 *
 * <p>A prose table that drifts out of sync with the stat framework is worse than no table, so the
 * markdown at {@code docs/design/tool-heads.md} is parsed here and checked against the real enums.
 */
class ToolHeadSpecTest {

  private static final Path REPO_ROOT = Path.of(System.getProperty("aor.repoRoot", "."));
  private static final String SPEC = "docs/design/tool-heads.md";
  private static final double EPSILON = 1.0e-6;

  /** Block counts one work action may cover: a single block, a 3x3 face, or a 5x5 face. */
  private static final Set<Double> AREA_PATTERNS = Set.of(1.0, 9.0, 25.0);

  /** Tier at and above which a harvest ability may switch on. */
  private static final int ABILITY_GATE_LEVEL = 5;

  private static String spec() throws IOException {
    final Path path = REPO_ROOT.resolve(SPEC);
    assertTrue(Files.isRegularFile(path), "expected the tool-head spec to exist at " + path);
    return Files.readString(path, StandardCharsets.UTF_8);
  }

  private static Table table(String firstHeaderCell) throws IOException {
    final List<String> lines = spec().lines().map(String::trim).toList();
    for (int i = 0; i < lines.size(); i++) {
      final List<String> cells = cells(lines.get(i));
      if (!cells.isEmpty() && cells.get(0).equals(firstHeaderCell)) {
        final List<List<String>> rows = new ArrayList<>();
        for (int row = i + 2; row < lines.size() && !cells(lines.get(row)).isEmpty(); row++) {
          rows.add(cells(lines.get(row)));
        }
        return new Table(cells, rows);
      }
    }
    throw new AssertionError(
        SPEC + " has no table whose first column is '" + firstHeaderCell + "'");
  }

  private static List<String> cells(String line) {
    if (!line.startsWith("|") || !line.endsWith("|") || line.length() < 2) {
      return List.of();
    }
    return Arrays.stream(line.substring(1, line.length() - 1).split("\\|", -1))
        .map(cell -> cell.replace("`", "").trim())
        .toList();
  }

  private static double number(String cell) {
    return Double.parseDouble(cell);
  }

  @Test
  void theRosterIsTheFourToolHeadsInTheEnumAndNothingElse() {
    assertEquals(
        EnumSet.of(ToolHeadType.PICKAXE, ToolHeadType.HOE, ToolHeadType.AXE, ToolHeadType.HAMMER),
        EnumSet.allOf(ToolHeadType.class),
        "the spec covers exactly these four heads; a new head needs a spec row before it is added");
  }

  @Test
  void theStatTableHasOneRowPerToolHeadPerTierWithNoGapsAndNoStrangers() throws IOException {
    final Table stats = table("Tool head");
    final Set<String> seen = new HashSet<>();

    for (final List<String> row : stats.rows()) {
      assertEquals(
          stats.headers().size(), row.size(), "row " + row + " does not fill every column");
      final ToolHeadType type = ToolHeadType.valueOf(row.get(0));
      final RobotTier tier = RobotTier.valueOf(row.get(1));
      assertTrue(seen.add(type + "/" + tier), "duplicate row for " + type + " at " + tier);
    }

    for (final ToolHeadType type : ToolHeadType.values()) {
      for (final RobotTier tier : RobotTier.values()) {
        assertTrue(seen.contains(type + "/" + tier), "no spec row for " + type + " at " + tier);
      }
    }
    assertEquals(
        ToolHeadType.values().length * RobotTier.values().length,
        stats.rows().size(),
        "the stat table must be exactly the roster crossed with the tier ladder");
  }

  @Test
  void everyStatColumnIsARealStatAToolHeadIsAllowedToDeclare() throws IOException {
    final Table stats = table("Tool head");

    assertEquals("Robot tier", stats.headers().get(1), "column 2 identifies the tier");
    final List<String> statColumns = stats.headers().subList(2, stats.headers().size());
    assertTrue(statColumns.size() >= 3, "the table must specify more than one stat");

    for (final String column : statColumns) {
      final RobotStat stat =
          assertDoesNotThrow(
              () -> RobotStat.valueOf(column), column + " is not a stat in RobotStat");
      assertTrue(
          HandAttachment.TOOL_STATS.contains(stat),
          stat + " is not a stat a tool-head may declare");
    }
    assertEquals(
        EnumSet.copyOf(statColumns.stream().map(RobotStat::valueOf).toList()),
        EnumSet.copyOf(HandAttachment.TOOL_STATS),
        "every stat a tool-head may declare needs a column, so nothing is left unspecified");
  }

  @Test
  void everyRowIsAStatBlockTheToolHeadRecordAccepts() throws IOException {
    final Table stats = table("Tool head");

    for (final List<String> row : stats.rows()) {
      final StatBlock.Builder block = StatBlock.builder();
      for (int column = 2; column < stats.headers().size(); column++) {
        final double value = number(row.get(column));
        assertTrue(value >= 0.0, row.get(0) + " " + row.get(1) + " has a negative stat");
        block.set(RobotStat.valueOf(stats.headers().get(column)), value);
      }
      final ToolHeadType type = ToolHeadType.valueOf(row.get(0));
      assertDoesNotThrow(
          () -> new ToolHead(type, block.build()), "row " + row + " is not a legal tool-head");
    }
  }

  @Test
  void workSpeedAndDurabilityFollowTheFrameworkTierCurve() throws IOException {
    for (final RobotStat stat : List.of(RobotStat.WORK_SPEED, RobotStat.TOOL_DURABILITY)) {
      assertTrue(stat.scalesWithTier(), stat + " is scaled by the tier multiplier in code");
      final Map<ToolHeadType, Map<RobotTier, Double>> values = column(stat);
      for (final ToolHeadType type : ToolHeadType.values()) {
        final double base = values.get(type).get(RobotTier.T1);
        assertTrue(base > 0.0, type + " needs a tier 1 " + stat);
        for (final RobotTier tier : RobotTier.values()) {
          assertEquals(
              base * tier.magnitudeMultiplier(),
              values.get(type).get(tier),
              EPSILON,
              type
                  + " "
                  + stat
                  + " at "
                  + tier
                  + " must be the tier 1 value times "
                  + tier.magnitudeMultiplier());
        }
      }
    }
  }

  @Test
  void areaOfEffectStartsAtOneBlockAndOnlyEverStepsUp() throws IOException {
    final Map<ToolHeadType, Map<RobotTier, Double>> areas = column(RobotStat.AREA_OF_EFFECT);

    for (final ToolHeadType type : ToolHeadType.values()) {
      assertEquals(1.0, areas.get(type).get(RobotTier.T1), EPSILON, type + " starts single-block");
      double previous = 0.0;
      for (final RobotTier tier : RobotTier.values()) {
        final double area = areas.get(type).get(tier);
        assertTrue(
            AREA_PATTERNS.contains(area),
            type + " at " + tier + " works " + area + " blocks, which is not a documented pattern");
        assertTrue(area >= previous, type + " area of effect shrinks at " + tier);
        previous = area;
      }
    }
  }

  @Test
  void harvestAbilitiesStayOffUntilTheTopTwoTiersAndNeverRegress() throws IOException {
    for (final RobotStat stat :
        List.of(RobotStat.HARVEST_YIELD_BONUS, RobotStat.PRECISION_HARVEST)) {
      final Map<ToolHeadType, Map<RobotTier, Double>> values = column(stat);
      for (final ToolHeadType type : ToolHeadType.values()) {
        double previous = 0.0;
        for (final RobotTier tier : RobotTier.values()) {
          final double value = values.get(type).get(tier);
          if (tier.level() < ABILITY_GATE_LEVEL) {
            assertEquals(0.0, value, EPSILON, stat + " must stay off for " + type + " at " + tier);
          }
          assertTrue(value >= previous, stat + " on " + type + " regresses at " + tier);
          previous = value;
        }
        if (stat == RobotStat.PRECISION_HARVEST) {
          final double top = values.get(type).get(RobotTier.T6);
          assertTrue(top == 0.0 || top == 1.0, stat + " is a flag, so " + type + " needs 0 or 1");
        }
      }
    }
  }

  @Test
  void theOwnershipTableAccountsForEveryArmsAndHandsStatExactlyOnce() throws IOException {
    final Table split = table("Stat");
    final Set<RobotStat> listed = EnumSet.noneOf(RobotStat.class);

    for (final List<String> row : split.rows()) {
      final RobotStat stat = RobotStat.valueOf(row.get(0));
      final PartSlot slot = PartSlot.valueOf(row.get(1));
      assertEquals(
          slot,
          stat.owner().orElseThrow(),
          stat + " is owned by " + stat.owner().orElse(null) + ", not by " + slot);
      assertEquals(
          Set.of(slot),
          stat.contributors(),
          stat + " must come from one slot only, or it is double counted");
      assertTrue(listed.add(stat), stat + " is listed twice in the ownership table");
    }

    final Set<RobotStat> expected = EnumSet.noneOf(RobotStat.class);
    expected.addAll(PartSlot.ARMS.stats());
    expected.addAll(PartSlot.HANDS.stats());
    assertEquals(
        expected,
        listed,
        "every arms and hands stat needs a row, so none is forgotten or claimed by both");
  }

  @Test
  void theStrengthLadderMatchesTheArmsPartsTheFrameworkBuildsAndNeverWeakens() throws IOException {
    final Table ladder = table("Arms tier");
    final List<String> harvestClasses = List.of("stone", "iron", "diamond", "netherite");

    assertEquals(
        RobotTier.values().length, ladder.rows().size(), "the ladder needs one row per tier");
    int previous = -1;
    for (int i = 0; i < ladder.rows().size(); i++) {
      final List<String> row = ladder.rows().get(i);
      final RobotTier tier = RobotTier.valueOf(row.get(0));
      assertEquals(RobotTier.values()[i], tier, "the ladder must run in tier order");
      assertEquals(
          PartCatalog.defaultPart(PartSlot.ARMS, tier).effectiveStats().get(RobotStat.STRENGTH),
          number(row.get(1)),
          EPSILON,
          "the STRENGTH column must match the arms part the framework builds at " + tier);

      final int harvestClass = harvestClasses.indexOf(row.get(2));
      assertTrue(harvestClass >= 0, row.get(2) + " is not one of " + harvestClasses);
      assertTrue(harvestClass >= previous, "the harvest ceiling drops at " + tier);
      previous = harvestClass;
    }
  }

  @Test
  void theCarbonFiberOffsetsAreOnesAHandsPartActuallyAccepts() throws IOException {
    final Table offsets = table("Hands upgrade stat");
    assertTrue(offsets.rows().size() >= 2, "the upgrade must be worth installing");

    final StatBlock.Builder declared = StatBlock.builder();
    final Set<RobotStat> listed = EnumSet.noneOf(RobotStat.class);
    for (final List<String> row : offsets.rows()) {
      final RobotStat stat = RobotStat.valueOf(row.get(0));
      final double offset = number(row.get(1));
      assertTrue(offset > 0.0, stat + " needs a positive offset or the upgrade does nothing");
      declared.set(stat, offset);
      listed.add(stat);
    }

    final CarbonFiberUpgrade carbonFiber = CarbonFiberUpgrade.of(declared.build());
    assertDoesNotThrow(
        () -> new HandsPart(RobotTier.T1, PartCatalog.toolHead(ToolHeadType.PICKAXE), carbonFiber),
        "the documented upgrade must be installable on a hands part");

    final Set<RobotStat> scalingToolStats = EnumSet.noneOf(RobotStat.class);
    HandAttachment.TOOL_STATS.stream()
        .filter(RobotStat::scalesWithTier)
        .forEach(scalingToolStats::add);
    assertEquals(
        scalingToolStats,
        listed,
        "the upgrade boosts every work rate and no tier-gated capability");
  }

  private static Map<ToolHeadType, Map<RobotTier, Double>> column(RobotStat stat)
      throws IOException {
    final Table stats = table("Tool head");
    final int index = stats.headers().indexOf(stat.name());
    assertTrue(index > 1, "the stat table has no " + stat + " column");

    final Map<ToolHeadType, Map<RobotTier, Double>> values = new EnumMap<>(ToolHeadType.class);
    for (final ToolHeadType type : ToolHeadType.values()) {
      values.put(type, new EnumMap<>(RobotTier.class));
    }
    for (final List<String> row : stats.rows()) {
      values
          .get(ToolHeadType.valueOf(row.get(0)))
          .put(RobotTier.valueOf(row.get(1)), number(row.get(index)));
    }
    return values;
  }

  /**
   * One parsed markdown table.
   *
   * @param headers the header cells, trimmed
   * @param rows the body rows, each already split into trimmed cells
   */
  private record Table(List<String> headers, List<List<String>> rows) {}
}
