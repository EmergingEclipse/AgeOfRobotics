# Robot tool heads (Hands)

Finalized roster and tier stat table for the work attachments that fit a robot's Hands slot.
This is the design output of issue #48. Issue #17 implements the tier 1 and tier 2 rows, and the
farming, building and hauling jobs in issue #26 consume the rest. Weapon heads are a separate
design pass in issue #49 and are out of scope here.

Every stat name in this document is an existing constant of
`com.modpackswork.ageofrobotics.core.robot.RobotStat`, and every tier name is a constant of
`RobotTier`. The tables below are parsed and checked against those enums by
`core/src/test/java/com/modpackswork/ageofrobotics/core/robot/ToolHeadSpecTest.java`, so a change
here that the code does not support fails the build.

Tier names are used rather than metal names on purpose. The issues and the older design notes
disagree about which metal sits at tier 5 and which at tier 6, and nothing in this spec depends on
the answer.

## How a tool head exists in the code

- The Hands slot is `PartSlot.HANDS`. It is optional for assembly, so a robot may run with empty
  hands.
- A fitted head is a `HandAttachment`, either a `ToolHead` or a `WeaponHead`, and the two can never
  be fitted at the same time. `HandAttachment.TOOL_STATS` fixes the stats a tool head may declare,
  so a pickaxe cannot quietly grant attack damage.
- **A tool head carries no tier of its own.** `ToolHead` has no tier field. The tier in the table
  below is the tier of the `HandsPart` the head is fitted to, and `HandsPart.effectiveStats()` is
  what turns a head plus a tier into the numbers a job runs on. One pickaxe head item therefore
  works in tier 1 hands and in tier 6 hands, and performs differently in each.
- That also means there are four head items in total, not twenty four. The 36 part items the older
  design notes list are the six slots crossed with the six tiers, and the Hands entries in that
  list are the hands themselves.

## The roster

| Head      | Job it serves              | Primary work action                                  |
| --------- | -------------------------- | ---------------------------------------------------- |
| `PICKAXE` | Mining (issue #20)         | Break stone, ores and other pickaxe blocks           |
| `HOE`     | Farming (issue #26)        | Till, plant, harvest and replant crops               |
| `AXE`     | Wood gathering (issue #26) | Fell logs and clear leaves, replanting saplings      |
| `HAMMER`  | Building and demolition    | Place blocks from the robot's inventory, break walls |

Wood gathering is worth having. The axe is the cheapest head to specify, the job reuses the mining
job's claimed-area machinery with a different block filter, and a wood supply is what feeds the
charcoal and burner chain that powers everything else.

## Hauling gets no tool head, and that is a decision not an omission

Hauling runs with the Hands slot empty.

- Nothing on hauling's critical path is a Hands stat. A haul is bounded by how much the robot can
  hold, which is `CARRY_CAPACITY` on the Arms, and how fast it walks, which is `MOVEMENT_SPEED` on
  the Legs. Picking an item up and putting it down are container transactions, not timed work
  actions, so there is no `WORK_SPEED` term to scale.
- `PartSlot.HANDS.requiredForAssembly()` is already `false`, so a hauler with empty hands is a
  legal robot today and needs no new code.
- A "gripper" or "crate" head would have to raise carry capacity to be worth fitting, and carry
  capacity is owned by the Arms. Granting it from the Hands is exactly the double count this spec
  exists to prevent, and `HandAttachment.TOOL_STATS` rejects it.
- Leaving the slot empty is also the right economic signal. A hauler is the cheapest robot in the
  mod, which is what makes hauling the job players hand to their oldest tier 1 units.

A hauler may still be fitted with any head if the player wants it to do a second job, and it gains
nothing at hauling by doing so. If playtesting later shows hauling needs its own progression, the
right place for it is an Arms variant or a torso function module, not a head.

## What the stats mean, with units

`WORK_SPEED` is a vanilla destroy speed, the same scale as a held tool's tool speed, where
`seconds = block hardness * 1.5 / WORK_SPEED` for a block the robot is allowed to work. That makes
the numbers directly comparable to a wooden pickaxe at 2.0 and a diamond pickaxe at 8.0. A work
action that breaks nothing, such as tilling, planting or placing, costs a flat 2.0 work units, so
`actions per second = WORK_SPEED / 2`.

`AREA_OF_EFFECT` is how many blocks one work action covers, so the value is a block count and not a
radius. Only three patterns exist: 1 block, a 3 by 3 face at 9, and a 5 by 5 face at 25. Each head
reads the pattern in the way its job needs it: the pickaxe and the hammer work a face square to the
robot, the hoe works a square of ground, and the axe walks up to that many connected log blocks so
a whole tree comes down in one action. One action is charged at the time of the slowest block in
the pattern, and `TOOL_DURABILITY` is charged once per block actually worked, which is what keeps a
5 by 5 head from being free.

`TOOL_DURABILITY` is uses, not a percentage. A head at zero durability stops the job and reports
itself as worn instead of vanishing, and it is repaired or replaced at the Assembly Station, so a
robot can never silently destroy a player's head item.

`HARVEST_YIELD_BONUS` is the fortune equivalent, expressed as the expected number of extra drops
per worked block. It is calibrated against vanilla fortune, where fortune I on coal averages about
0.33 extra and fortune III averages about 1.0 extra.

`PRECISION_HARVEST` is the silk touch equivalent and is a flag, 0 or 1. When it is on, the head
recovers the block itself and the yield bonus does not apply, mirroring vanilla. It is a per job
toggle on the robot, not a second head, so a tier 6 mining robot picks either ore multiplication or
intact blocks for the job it has been given and never both on the same break.

All five are per head values. None of them are contributed by any other slot.

## Tier stat table

| Tool head | Robot tier | WORK_SPEED | AREA_OF_EFFECT | TOOL_DURABILITY | HARVEST_YIELD_BONUS | PRECISION_HARVEST |
| --------- | ---------- | ---------- | -------------- | --------------- | ------------------- | ----------------- |
| `PICKAXE` | T1         | 2.0        | 1              | 250             | 0                   | 0                 |
| `PICKAXE` | T2         | 3.2        | 1              | 400             | 0                   | 0                 |
| `PICKAXE` | T3         | 4.4        | 1              | 550             | 0                   | 0                 |
| `PICKAXE` | T4         | 5.6        | 9              | 700             | 0                   | 0                 |
| `PICKAXE` | T5         | 6.8        | 9              | 850             | 0.33                | 0                 |
| `PICKAXE` | T6         | 8.0        | 25             | 1000            | 1.0                 | 1                 |
| `HOE`     | T1         | 1.5        | 1              | 200             | 0                   | 0                 |
| `HOE`     | T2         | 2.4        | 9              | 320             | 0                   | 0                 |
| `HOE`     | T3         | 3.3        | 9              | 440             | 0                   | 0                 |
| `HOE`     | T4         | 4.2        | 25             | 560             | 0                   | 0                 |
| `HOE`     | T5         | 5.1        | 25             | 680             | 0.33                | 0                 |
| `HOE`     | T6         | 6.0        | 25             | 800             | 1.0                 | 0                 |
| `AXE`     | T1         | 2.5        | 1              | 300             | 0                   | 0                 |
| `AXE`     | T2         | 4.0        | 1              | 480             | 0                   | 0                 |
| `AXE`     | T3         | 5.5        | 9              | 660             | 0                   | 0                 |
| `AXE`     | T4         | 7.0        | 9              | 840             | 0                   | 0                 |
| `AXE`     | T5         | 8.5        | 25             | 1020            | 0.33                | 0                 |
| `AXE`     | T6         | 10.0       | 25             | 1200            | 1.0                 | 1                 |
| `HAMMER`  | T1         | 1.25       | 1              | 350             | 0                   | 0                 |
| `HAMMER`  | T2         | 2.0        | 1              | 560             | 0                   | 0                 |
| `HAMMER`  | T3         | 2.75       | 1              | 770             | 0                   | 0                 |
| `HAMMER`  | T4         | 3.5        | 1              | 980             | 0                   | 0                 |
| `HAMMER`  | T5         | 4.25       | 9              | 1190            | 0                   | 0                 |
| `HAMMER`  | T6         | 5.0        | 25             | 1400            | 0                   | 1                 |

Two of those columns are not free numbers. `WORK_SPEED` and `TOOL_DURABILITY` both scale with the
tier, so each head declares one tier 1 base value and the framework multiplies it by
`RobotTier.magnitudeMultiplier()`, which runs 1.0, 1.6, 2.2, 2.8, 3.4 and 4.0. The bases are:

| Head      | Base WORK_SPEED | Base TOOL_DURABILITY | Why                                                           |
| --------- | --------------- | -------------------- | ------------------------------------------------------------- |
| `PICKAXE` | 2.0             | 250                  | Anchored on vanilla, wooden pickaxe at T1 to diamond at T6    |
| `HOE`     | 1.5             | 200                  | Crops have no hardness, so throughput is bounded by growth    |
| `AXE`     | 2.5             | 300                  | Logs are soft and felling is the whole job, so it cycles fast |
| `HAMMER`  | 1.25            | 350                  | Placing blocks is the strongest automation, so it is slowest  |

The other three columns are stepped capabilities rather than magnitudes, which is why
`AREA_OF_EFFECT`, `HARVEST_YIELD_BONUS` and `PRECISION_HARVEST` are all declared as non scaling in
`RobotStat`. A head therefore declares them per tier directly, and nothing multiplies them.

## Tier gated abilities in words

- **Area of effect ladders differ on purpose.** Farming gets width first, at T2, because a crop
  block has no hardness and the harvest is capped by growth time rather than by tool throughput.
  Wood is next, at T3, because trees are cheap and a felled tree is the natural unit of work.
  Mining waits until T4, because breadth there is the difference between a tunnel and a quarry.
  Building waits until T5, because a hammer that fills a 5 by 5 face in one action is the most
  powerful automation in this document.
- **T5 is the yield tier.** The pickaxe, hoe and axe each gain `HARVEST_YIELD_BONUS` of 0.33 at T5
  and 1.0 at T6. For the axe the bonus applies to the secondary drops that vary in vanilla, which
  is saplings, apples and sticks, because a log always drops one log.
- **T6 is the precision tier.** The pickaxe, axe and hammer gain `PRECISION_HARVEST`. On a pickaxe
  it is ore blocks and fragile blocks recovered whole, on an axe it is intact leaves and beehives,
  and on a hammer it is demolition that gets glass, ice and pots back instead of nothing. The hoe
  never gains it, because there is no crop whose block form is worth recovering.
- **The hammer trades yield for reach and toughness.** It is the only head with no yield bonus at
  any tier, and in exchange it carries the largest durability pool, because a building job breaks
  and places far more blocks per assignment than a mining job does.

## Arms versus Hands, with nothing counted twice

The Arms are the attach point for the Hands, which is why the two are easy to confuse. The split is
this: **the Arms decide what the robot may work and how much it can carry away, the Hands decide how
fast, how wide, how long the head lasts and what extra drops come out.** No stat appears on both.

| Stat                  | Owning slot | What it decides                                             |
| --------------------- | ----------- | ----------------------------------------------------------- |
| `STRENGTH`            | `ARMS`      | The ceiling: the hardest block class the hands may work     |
| `CARRY_CAPACITY`      | `ARMS`      | The payload: inventory space for what the job produces      |
| `WORK_SPEED`          | `HANDS`     | The rate at which one work action completes                 |
| `AREA_OF_EFFECT`      | `HANDS`     | How many blocks one work action covers                      |
| `TOOL_DURABILITY`     | `HANDS`     | Uses before the fitted head is worn and the job stops       |
| `HARVEST_YIELD_BONUS` | `HANDS`     | Expected extra drops per worked block, the fortune equal    |
| `PRECISION_HARVEST`   | `HANDS`     | Whether the block itself is recovered, the silk touch equal |
| `ATTACK_DAMAGE`       | `HANDS`     | Weapon head damage, specified by issue #49 not here         |
| `ATTACK_SPEED`        | `HANDS`     | Weapon head swing rate, specified by issue #49 not here     |

Three consequences worth stating plainly, because they are what stops the double count:

1. **`STRENGTH` is not a term in the work rate.** Seconds per block is hardness times 1.5 divided by
   `WORK_SPEED` and nothing else. Stronger arms do not mine faster, they mine harder blocks. Speed
   is bought with hands, reach is bought with hands, and the arms are bought to unlock a material.
2. **No head may declare an arms stat, and the code enforces it.**
   `RobotPart.validateStatsFor(PartSlot.HANDS, ...)` rejects `STRENGTH` and `CARRY_CAPACITY` on
   anything in the Hands slot, and `HandAttachment.TOOL_STATS` narrows it further to the five work
   stats above.
3. **Mixed tiers are legal and meaningful.** `HandsPart` and the Arms part carry their own tiers, so
   tier 2 arms with tier 5 hands is a fast robot that still cannot touch obsidian, and tier 5 arms
   with tier 2 hands is a robot that can touch anything slowly. The robot's overall tier, which is
   what `RobotBuild.overallTier()` reports and what drives `WORK_RANGE`, is its weakest structural
   part, and the Hands slot is not structural so it never drags that number down.

### The strength ladder that sets the ceiling

`STRENGTH` is read as a harvest class, in the vanilla sense of which tool material is needed to drop
a block. The thresholds are 3.0 for stone class, 4.5 for iron, 8.0 for diamond and 12.0 for
netherite. Against the arms parts the framework currently builds, that gives:

| Arms tier | STRENGTH | Harvest ceiling | What that unlocks                            |
| --------- | -------- | --------------- | -------------------------------------------- |
| T1        | 3.0      | stone           | Stone, coal, copper, most building blocks    |
| T2        | 4.8      | iron            | Iron, lapis, quartz, the T3 material chain   |
| T3        | 6.6      | iron            | Same classes worked faster, not more classes |
| T4        | 8.4      | diamond         | Diamond, emerald, redstone, obsidian         |
| T5        | 10.2     | diamond         | Same classes worked faster, not more classes |
| T6        | 12.0     | netherite       | Ancient debris and everything below it       |

The `STRENGTH` column is the value of `PartCatalog.defaultPart(PartSlot.ARMS, tier)` today, and the
spec test asserts it, so retuning arms in issue #17 fails the build until this table is updated with
it. The ceiling widens on the even tiers only, which is deliberate: the odd tiers are speed and
durability tiers, so every tier is still worth crafting without the class ladder moving every step.

A robot asked to work a block above its ceiling skips the block and reports it, in the same way a
worn head stops the job. It never grinds forever on something it cannot break.

## The carbon fiber upgrade slot

The universal upgrade slot applies to tool heads exactly as it applies to every other part, with one
clarification that follows from the code: **the slot belongs to the Hands part, not to the head.**
`HandsPart` holds the `CarbonFiberUpgrade`, `ToolHead` does not, so swapping a pickaxe head for a hoe
head keeps the upgrade, and a player pays for it once per pair of hands rather than once per head.

One carbon fiber upgrade in a Hands part contributes these flat offsets:

| Hands upgrade stat | Flat offset | Effect                                                  |
| ------------------ | ----------- | ------------------------------------------------------- |
| `WORK_SPEED`       | 1.0         | Half again as fast on tier 1 hands, an eighth on tier 6 |
| `TOOL_DURABILITY`  | 100         | Forty percent more uses on tier 1 hands, ten on tier 6  |

The offsets are flat and are added after tier scaling, per `RobotPart.effectiveStats()`, so the
upgrade is proportionally worth most on the cheapest hands. That is the intended shape: it is a way
to keep early robots useful, not a way to skip a tier.

The upgrade may not grant `AREA_OF_EFFECT`, `HARVEST_YIELD_BONUS` or `PRECISION_HARVEST`. Those are
tier-gated capabilities, and buying a tier 6 ability with an upgrade on tier 1 hands would flatten
the whole ladder. `HandsPart` now rejects any upgrade that declares a non scaling hands stat, so the
rule is enforced rather than merely written down.

## Electrical circuit gating

Circuit gating applies to tool heads the same way it applies to every other part, and it lands on
the Hands part:

- **The Hands part is a part like any other.** Its recipe takes the tier metal plus the electrical
  circuit grade its tier band requires, refined scrap from tier 3 up, and carbon fiber as a crafting
  component from tier 4 up. Which circuit grade belongs to which tier band is owned by issues #15,
  #22 and #40, and this document deliberately takes no position on it.
- **The head itself needs no circuit.** A head carries no tier, no logic and no power draw. It is a
  shaped lump of metal that the hands drive, so it is craftable from common overworld metal at any
  point in the game and is not a progression gate.
- **Gating still works, because capability lives in the hands.** A fortune equivalent is reachable
  only through tier 5 hands, and tier 5 hands need the circuit grade that tier 5 needs. The head
  roster being available early costs nothing, in the same way owning a diamond pickaxe recipe costs
  nothing before you have diamonds.
- **One head is fitted at a time**, and a tool head and a weapon head can never be fitted together.
  A robot that must both mine and fight needs two robots or a trip to the Assembly Station. Issue
  #49 owns the weapon side of that rule.

## What issues #17 and #26 still have to add in code

The stat vocabulary is now complete for tool work, but two gaps remain, and they are gaps on purpose
so that this ticket stays a design ticket:

1. **A tier aware head factory.** `PartCatalog.toolHead(ToolHeadType)` takes no tier and returns one
   placeholder stat block. `WORK_SPEED` and `TOOL_DURABILITY` survive that, because the framework
   multiplies them by the tier, but `AREA_OF_EFFECT`, `HARVEST_YIELD_BONUS` and `PRECISION_HARVEST`
   are non scaling and have to be declared per tier. Issue #17 needs
   `toolHead(ToolHeadType type, RobotTier tier)` returning the rows of the table above.
2. **Job side plumbing**, which belongs to issues #20 and #26 rather than to the part framework: the
   harvest class check against the arms `STRENGTH`, resolving an `AREA_OF_EFFECT` block count into an
   actual pattern for the job at hand, charging `TOOL_DURABILITY` once per block worked, stopping a
   job on a worn head, and the per job toggle that chooses yield or precision at tier 6. That toggle
   is job assignment data and must not become a stat, or it would be double counted the moment two
   jobs share a robot.

No new `RobotStat` and no new `ToolHeadType` are needed beyond what is now in the code. Hauling in
particular needs nothing at all.

## Numbers to revisit in the balancing pass (issue #45)

These are the calls with the least evidence behind them, flagged rather than presented as settled:

- **The 5 by 5 hammer at tier 6.** Twenty five blocks placed per action is the single strongest
  number in this document, and it may want to stay at 9.
- **Durability scale.** 250 uses at tier 1 to 1000 at tier 6 sits between a vanilla iron and diamond
  tool. It is a guess about how much of a chore head replacement should be, and it interacts with
  whatever the Assembly Station charges to repair one.
- **The axe being the fastest head.** A base `WORK_SPEED` of 2.5 gives 10.0 at tier 6, which is
  faster than a vanilla diamond tool. It is justified by soft logs, but it makes wood farms very
  strong once area of effect arrives at tier 3.
- **Yield bonus values.** 0.33 and 1.0 copy vanilla fortune I and fortune III. Whether a robot should
  reach fortune III at all is a progression question, not a numbers question.
- **The even tier ceiling ladder.** Widening the harvest class only at tiers 2, 4 and 6 leaves tiers
  3 and 5 as pure rate upgrades, which is tidy on paper and may read as a dead tier in play.
