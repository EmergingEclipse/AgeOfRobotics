# Age of Robotics — Design Document (v0.1 draft)

**Minecraft:** 1.21.1
**Loader:** NeoForge (recommended — see [Open Questions](#open-questions))
**Scope:** Large content mod. One new dimension, ~6 material tiers, 6 robot part slots, a full RF power chain, and an AI/autonomy system.
**Pitch:** Build, equip and program robots to mine, build and farm for you — but the smarter you make them, the harder they are to keep.

---

## 1. Core Loop

1. Bootstrap power (solar → burner/steam multiblock).
2. Build the **Robot Assembly Frame** and craft Tier 1–2 copper/iron robots.
3. Robots automate mining/building/farming, freeing the player to expand.
4. Hit the T3 wall: chassis above T2 need **Refined Scrap**, only found in the machine dimension.
5. Build the **Quantum Gate**, enter **The Scrapyard**.
6. Fight big robots and crawlers, harvest scrap and concentrated ore scrap, run RF-powered reclaimers for 3× ore.
7. Return with refined scrap → T3–T6 robots, better circuits, higher intelligence.
8. Manage the risk: high-intelligence robots go rogue without proper containment.

---

## 2. Materials & Tier Ladder

| Tier | Primary material | Character |
|---|---|---|
| T1 | **Copper** | Cheap, slow, low durability, low INT ceiling |
| T2 | **Iron** | Baseline workhorse |
| T3 | **Steel** | First tier gated behind Refined Scrap. Durable |
| T4 | **Aluminium** | Light alloy — speed and energy efficiency |
| T5 | **Titanium** | High durability + light. Combat-capable |
| T6 | **Tungsten–Carbon Fiber Composite** | Endgame. Max stats, max INT ceiling |

**Carbon Fiber** is a cross-cutting crafting component: required in every T4+ part, in advanced circuits, and in high-tier hands/arms. Produced from a coal/scrap-derived process (recipe chain TBD).

**Tungsten** doubles as the heat-resistant component in the burner/turbine multiblock, so players find it before T6.

**Gating rule:** every chassis part of **T3 or above** requires **Refined Scrap** in its recipe. This is the hard dimension gate.

### New Ingots / Materials
`copper` (vanilla) · `iron` (vanilla) · `steel` · `aluminium` · `titanium` · `tungsten` · `carbon_fiber` · `scrap` · `refined_scrap` · `concentrated_ore_scrap_*`

---

## 3. The Scrapyard (new dimension)

A dead machine world. Machines won and then rusted.

- **Ground/filler block:** Scrap Block (replaces stone as the dimension's base terrain block).
- **Deepslate analogue:** Compacted Scrap (lower Y layers).
- **Terrain:** wrecked world — broken plateaus, collapsed structures, exposed rebar, oil/coolant pools, half-buried machine hulks. No trees; sparse rusted metal spires.
- **Sky/atmosphere:** overcast rust-orange, no weather, dim ambient light.
- **Entry:** **Quantum Gate** only (no natural portal ignition).

### Ore Generation
Ore veins use **the exact same distribution, vein size and Y-ranges as the Overworld** (copy vanilla placed features, retarget the replaceable-block tag to the scrap blocks). Only the block appearance and drops change:

| Overworld ore | Scrapyard equivalent | Drops |
|---|---|---|
| Coal | Scrap-Bound Coal | Concentrated Coal Scrap |
| Iron | Scrap-Bound Iron | Concentrated Iron Scrap |
| Copper | Scrap-Bound Copper | Concentrated Copper Scrap |
| Gold, Redstone, Lapis, Diamond, Emerald, Quartz | same pattern | Concentrated `<X>` Scrap |
| — | Scrap-Bound Titanium / Tungsten / Bauxite | dimension-exclusive |

**Concentrated Ore Scrap** smelts/processes 1:1 into the normal ingot/item, so nothing is lost — it's a reskin of the vanilla economy in our style.

### Mobs
- **Crawler** (small): fast, low HP, swarms, spawns near wreckage. Drops scrap, wire, damaged circuits.
- **Sentinel / Hulk** (large, 2×3): slow, heavy armor, ranged or slam attack, mini-boss density near large wrecks. Drops refined scrap, salvaged parts, rare high-tier circuits.
- Both are hostile machines — immune to poison/wither, damaged extra by lightning/water (flavour hook, TBD).

---

## 4. Power (RF / NeoForge energy capability)

All numbers are first-pass targets for balancing.

| Block | Function | Output / Capacity |
|---|---|---|
| **Solar Panel** (T1–T3) | Passive RF in daylight | 5 / 20 / 80 RF/t |
| **Burner** | Consumes solid fuel → heat | Heat output, no RF directly |
| **Steam Chamber** | Water + heat → steam | Buffered steam |
| **Steam Turbine** | Steam → RF | ~200 RF/t at full steam |
| **Battery** (T1–T3) | RF storage + I/O | 100k / 1M / 10M RF |
| **Ore Reclaimer** | RF-powered ore processing | see below |

### Steam Multiblock
Burner → Steam Chamber → Steam Turbine, placed as a connected structure (burner below, chamber middle, turbine on top, plus tungsten casing). Water piped or bucketed into the chamber. Fully assembled structure forms one multiblock controller.

### Ore Reclaimer (RF-powered interactable)
- Requires RF to run; has an input slot, output slot, and internal energy buffer.
- Takes **Concentrated Ore Scrap** and outputs **3× the base ore yield**.
- Only accepts scrap-style inputs (keeps the 3× multiplier behind the dimension, not applied to vanilla ores).
- GUI: input · energy bar · progress arrow · output.

---

## 5. Robots

### Part Slots (6)
| Slot | Governs |
|---|---|
| **Head** | Intelligence, sensor/scan range, task complexity, target acquisition |
| **Torso** | Internal RF capacity, module slots, core durability |
| **Arms** | Work speed, reach, melee damage. Can be built as **tool arms** or **weapon arms** |
| **Hands** | Task capability — mining head, building manipulator, farming shears, or a weapon mount |
| **Legs** | Movement speed, step height, jump |
| **Boots** | Traction, fall damage reduction, terrain handling (hazard/lava resistance at high tiers) |

Each slot exists at **T1–T6**. Every part contributes to a stat block; the robot's final stats are the sum. Mixed-tier robots are legal and expected (cheap legs, expensive head).

### Stats
`Durability` · `Speed` · `Work Speed` · `Energy Capacity` · `Energy Draw` · `Reach` · `Intelligence` · `Attack` · `Task Slots`

### Non-chassis Components
- **Electrical Circuits** (T1–T6): required by every part; higher tiers gate higher-tier parts and raise the INT ceiling.
- **Personality Modules** (one per robot):
  - **Passive** — never attacks, flees threats, best work speed
  - **Neutral** — retaliates only, balanced
  - **Aggressive** — attacks hostiles on sight, higher attack, more energy draw
  - **Hunter** — actively seeks targets in a radius, tracks, highest attack, **highest rogue risk**

### Tasks
Mining (area/vein), Building (from a blueprint or schematic item), Farming (till/plant/harvest/replant), Hauling, Guarding. Task assignment is done via a handheld **Command Remote** or the robot's own GUI. Robots require RF to operate and return to a charging dock (or battery block) when low.

---

## 6. Intelligence & Containment

**Intelligence (INT)** is the mod's tension mechanic. INT comes from the head tier, circuit tier, and personality module. Higher INT unlocks better behaviours (longer task queues, pathfinding, autonomous re-tasking, learning shortcuts) — and raises **Rogue Risk**.

- Each robot has an **Obedience** value = containment measures − INT pressure.
- If Obedience drops below a threshold, the robot may: refuse orders → wander off → break containment → turn hostile (Hunter personality escalates fastest).
- **Containment measures** (each raises Obedience):
  - Restraint Bolt (installed item)
  - Tether Beacon (block — keeps robots inside a radius)
  - Compliance Module (torso module, costs a module slot)
  - Owner registration / signed control chip
  - Keeping the robot powered and tasked (idle high-INT robots drift)
- Design intent: T6 Hunter robots should be genuinely dangerous to run without full containment, and rewarding when you do.

---

## 7. Robot Assembly Frame (multiblock)

- **Footprint:** 3 blocks tall × 2 blocks wide × 2 deep (2×2×3).
- Player places the controller block and the structure validates; a robot model appears in the frame as it's built.
- **GUI:** 6 part slots (head/torso/arms/hands/legs/boots) + circuit slot + personality module slot + RF buffer + live stat readout showing the resulting robot's stats, INT and Rogue Risk **before** assembly.
- Consumes RF to assemble. Outputs a **Robot Entity** (spawned in front of the frame) or a packaged **Robot Item** for transport.
- Also used to **disassemble** a robot back into its parts and to **swap/upgrade** individual parts on an existing robot.

---

## 8. Quantum Gate

- Multiblock frame portal to The Scrapyard. Requires a T3+ circuit, a significant RF charge to prime, and the gate frame blocks (steel/tungsten casing + quantum core).
- Two-way; a return gate generates or can be built on the far side.
- The single hardest progression gate in the mod — everything T3+ is behind it.

---

## 9. Progression Summary

```
Copper tools ─► Solar/Burner power ─► Assembly Frame ─► T1/T2 robots (mine, farm)
      │
      └─► Steam multiblock + Battery ─► Quantum Gate ─► THE SCRAPYARD
                                                   │
                          Scrap + Concentrated Ore Scrap + Refined Scrap
                                                   │
                          Ore Reclaimer (3× ore) ──┴──► T3–T6 robots
                                                          │
                                              High INT ─► Containment problem
```

---

## 10. Block & Item Registry (first pass)

**Blocks:** scrap_block, compacted_scrap, scrap_bound_* (ores ×10+), machine_casing, solar_panel_t1–t3, burner, steam_chamber, steam_turbine, battery_t1–t3, ore_reclaimer, assembly_frame_controller, assembly_frame_casing, quantum_gate_frame, quantum_gate_core, tether_beacon, charging_dock

**Items:** scrap, refined_scrap, concentrated_*_scrap, steel/aluminium/titanium/tungsten ingots + nuggets + raw, carbon_fiber, circuit_t1–t6, personality_module ×4, restraint_bolt, compliance_module, command_remote, robot_head/torso/arms/hands/legs/boots × T1–T6 (36 part items), packaged_robot

**Entities:** robot (data-driven from parts), crawler, sentinel

---

## Open Questions

1. **Loader:** NeoForge or Forge for 1.21.1? NeoForge is the active 1.21.1 platform and has a built-in energy capability — recommending it unless there's a reason to target Forge.
2. **Tier ladder shape:** the table above is a single linear ladder. Alternative: split T4–T6 into a *light branch* (aluminium/titanium — speed, efficiency) and a *heavy branch* (tungsten/steel composite — toughness, combat), with carbon fiber joining them at T6. More interesting, more work.
3. **"Modpack" vs mod:** this document describes a single large mod. If the goal is a pack that also bundles existing mods (Create, Mekanism, etc.), the scope and the power system change — worth settling early.
4. **Robot count limits:** per-player cap, or soft-capped by RF cost and containment burden?
5. Do robots persist as entities when chunks unload, or convert to a chunk-loaded task ticket?
