# RF power network

How Age of Robotics moves RF between blocks, and why it moves it that way.

This document answers the second requirement of issue #9, which asks us to decide and write down how
RF is physically transported between blocks.

## The decision

Age of Robotics ships no cable, conduit or wire of its own. Every power block exposes its stored RF
through the standard Forge Energy capability and nothing else. Transport is somebody else's job.

Concretely, each power block entity extends `AbstractEnergyBlockEntity` and registers itself against
`Capabilities.EnergyStorage.BLOCK`. That capability is the only public surface of our power system.
There is no Age of Robotics network object, no channel, no controller block and no proprietary energy
unit.

## Why

The issue is explicit that cross mod energy compatibility matters and that we must not invent an
incompatible energy type. Forge Energy already is the shared standard on this platform, so the useful
question is not which API to use but whether we also need to ship the wires.

We do not, for four reasons.

- Players in this genre already have cables. Almost every modpack that would include Age of Robotics
  also includes at least one energy transport mod, and players have strong preferences about which.
  Shipping a fifth kind of cable adds a thing to craft rather than a thing to do.
- Cables are more work than they look. A conduit block needs connection logic, a network graph,
  chunk load and unload handling, block states for every connection shape, models for those states
  and a story for what happens when part of the network unloads. That is a feature in its own right
  and it competes with the machines the mod is actually about.
- A mod that only exposes the capability is a better citizen. Our blocks work with any cable, any
  wireless power mod and any energy meter or probe, because they all speak the same capability.
  Shipping our own transport invites the usual half compatible mess where our cable does not talk to
  their machine.
- Direct block to block transfer already covers the early game. A burner beside a steam chamber
  beside a turbine beside a battery bank works with no cable at all, which is exactly the shape of a
  first base.

## How third party cables interoperate

A cable from another mod finds our block by asking the level for the energy capability at our
position. It gets a `BufferedEnergyStorage`, which is a thin view over the core `EnergyBuffer` that
holds the real charge. From there the cable does what it always does. It calls `receiveEnergy` to
push power in and `extractEnergy` to pull power out, with the simulate flag first if it wants to
plan the transfer before committing to it.

Three details matter to anyone connecting to us.

- The same handler is exposed on all six faces. There is no per face input or output configuration,
  because nothing in the design asks for one.
- Direction is decided by the block's role, not by the face. A producer such as a solar panel
  reports `canReceive` as false, so a cable can never charge it. A consumer such as the extraction
  amplifier reports `canExtract` as false, so a cable can never siphon its working charge back out.
  A battery bank reports both as true.
- The handler instance never changes for the life of the block entity, so cables that cache the
  capability stay valid and we never have to invalidate it after a transfer.

## Push, not pull

Transfers between adjacent blocks are driven by the side that has energy to give.

A producer or a battery bank offers its spare RF outward from its own server tick, by calling
`pushEnergyToNeighbours`. A consumer does nothing at all. It waits to be filled and spends whatever
turns up.

This was chosen over pull for three reasons.

- Only one side of any pair drives a transfer, so the same RF cannot move twice in one tick and two
  adjacent blocks cannot fight over who moves it.
- A block that is not ticking never reaches into its neighbours, which keeps the number of capability
  lookups proportional to the number of generators rather than the number of machines.
- It matches what every cable mod already does to us, so our own direct transfers and a cable's
  transfers behave the same way from a machine's point of view.

The distribution policy lives in `EnergyDistribution.push`. One call is limited to a single extract
budget, so offering energy to six neighbours moves no more RF in total than offering it to one.
Neighbours are served in `Direction` order, first come first served, until that budget runs out.
There is no even split, because splitting a small budget six ways leaves every neighbour too short to
do anything. A generator that wants to spread its output can rotate the order it passes in.

## Plugging in a new machine

A later power ticket does not touch any shared file. The steps are the same for a generator, a
machine or a battery bank.

1. Declare the block's energy configuration as a constant, using `EnergySpec.producer`,
   `EnergySpec.consumer` or `EnergySpec.storage`. The factory you pick sets the role, and the role is
   what closes the unused direction for you.
2. Write a block entity that extends `AbstractEnergyBlockEntity` and passes that spec to the
   constructor. Saving and loading the charge is already handled.
3. Move energy internally with `generateEnergy` or `consumeEnergy`. Both mark the block changed, so
   the chunk gets saved. Never reach for `receiveEnergy` or `extractEnergy` from inside the block,
   because those are the outside world's methods and the role gate will refuse them.
4. If the block has energy to give, call `pushEnergyToNeighbours` from its server tick.
5. Register the capability by calling `PowerCapabilities.registerEnergyBlockEntity` from the ticket's
   own listener for `RegisterCapabilitiesEvent`. There is no central list to add to, which is
   deliberate, so two power tickets in flight at once cannot conflict over it.

The rules themselves live in `com.modpackswork.ageofrobotics.core.power`, which has no Minecraft
imports and is covered by unit tests. Anything that needs a decision about how much energy moves
belongs there rather than in a block entity.

## What is deliberately left out

- **Our own conduit.** Out of scope by decision, not by oversight. If playtesting shows the mod needs
  to stand alone without an energy transport mod installed, a conduit is a follow up issue and it
  changes nothing described above. It would be one more block that speaks the same capability.
- **Per face configuration.** No design requirement asks for input on one side and output on another.
- **A wireless or networked power model.** Same reasoning. The capability plus somebody's cables
  covers it.
- **Charge kept in the dropped item.** Issue #14 requires a battery bank to keep its charge when
  broken and re-placed. That is a block and item concern rather than a transport one, so it belongs
  to that ticket. The charge is already a single int on the block entity, which is all that ticket
  needs to copy into the dropped stack.
