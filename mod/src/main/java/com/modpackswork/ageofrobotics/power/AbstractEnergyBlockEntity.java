package com.modpackswork.ageofrobotics.power;

import com.modpackswork.ageofrobotics.core.power.EnergyBuffer;
import com.modpackswork.ageofrobotics.core.power.EnergyDistribution;
import com.modpackswork.ageofrobotics.core.power.EnergySink;
import com.modpackswork.ageofrobotics.core.power.EnergySpec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Base class for any block entity that holds RF.
 *
 * <p>Subclass this for every power block in the mod: generators, machines and battery banks alike.
 * All this class does is own an {@link EnergyBuffer}, persist its charge, and hand the world a
 * Forge Energy view of it. The rules that decide how much energy moves live in the core module, so
 * they can be unit tested without a running game.
 *
 * <p>A subclass supplies an {@link EnergySpec} to the constructor and is then expected to do three
 * things. It moves energy internally with {@link #generateEnergy(int)} or {@link
 * #consumeEnergy(int)}, both of which mark the block changed for you. If it is a generator or a
 * battery bank it calls {@link #pushEnergyToNeighbours()} from its server tick. And it registers
 * the Forge Energy capability for its own block entity type through {@link PowerCapabilities}.
 *
 * <p>Every face exposes the same view, and the {@link
 * com.modpackswork.ageofrobotics.core.power.EnergyRole} in the spec decides the direction. Per face
 * configuration is not supported, because nothing in the design asks for it.
 */
public abstract class AbstractEnergyBlockEntity extends BlockEntity {

  /** Tag key the stored charge is written under. Changing it would orphan existing saves. */
  private static final String ENERGY_TAG = "Energy";

  private final EnergyBuffer energy;
  private final BufferedEnergyStorage energyStorage;

  /**
   * Creates the block entity with an empty buffer.
   *
   * @param type the registered block entity type
   * @param pos the position in the world
   * @param state the block state at that position
   * @param spec the capacity, transfer rates and role of this kind of block
   */
  protected AbstractEnergyBlockEntity(
      BlockEntityType<?> type, BlockPos pos, BlockState state, EnergySpec spec) {
    super(type, pos, state);
    this.energy = new EnergyBuffer(spec);
    this.energyStorage = new BufferedEnergyStorage(this.energy, this::setChanged);
  }

  /**
   * Returns the Forge Energy view handed out through the capability.
   *
   * @return the handler, the same instance for the life of this block entity
   */
  public final IEnergyStorage energyStorage() {
    return energyStorage;
  }

  /**
   * Returns the live charge, for subclasses that need more than the helpers below.
   *
   * @return the buffer; note that writing to it directly will not mark the block changed
   */
  protected final EnergyBuffer energyBuffer() {
    return energy;
  }

  /**
   * Returns the charge currently held, for GUIs, tooltips and probe mods.
   *
   * @return the stored RF
   */
  public final int storedEnergy() {
    return energy.stored();
  }

  /**
   * Returns the most RF this block can hold.
   *
   * @return the capacity
   */
  public final int energyCapacity() {
    return energy.capacity();
  }

  /**
   * Banks energy this block produced itself, bypassing the role gate.
   *
   * @param amount the RF generated this tick
   * @return the RF actually banked, less than asked for once the buffer is full
   */
  protected final int generateEnergy(int amount) {
    final int banked = energy.fill(amount);
    if (banked > 0) {
      setChanged();
    }
    return banked;
  }

  /**
   * Spends energy on this block's own work, bypassing the role gate.
   *
   * @param amount the RF the operation costs
   * @return the RF actually spent, less than asked for once the buffer runs dry
   */
  protected final int consumeEnergy(int amount) {
    final int spent = energy.drain(amount);
    if (spent > 0) {
      setChanged();
    }
    return spent;
  }

  /**
   * Offers spare energy to the six neighbouring blocks.
   *
   * <p>This is the push half of the convention described in {@link EnergyDistribution}. Call it
   * from a server tick on producers and storage. Consumers never need it, and calling it on one
   * moves nothing because the role gate refuses.
   *
   * <p>Neighbours are asked for their handler through the capability, so an adjacent block from any
   * other mod is a valid target. Faces are visited in {@link Direction} order and the first that
   * accepts energy gets it, up to the spec's extract rate for the whole call.
   *
   * @return the total RF moved out, zero on the client or when nothing next door wants energy
   */
  protected final int pushEnergyToNeighbours() {
    final Level world = getLevel();
    if (world == null || world.isClientSide() || !energy.canExtract()) {
      return 0;
    }

    final List<EnergySink> sinks = new ArrayList<>(Direction.values().length);
    for (final Direction face : Direction.values()) {
      final IEnergyStorage neighbour =
          world.getCapability(
              Capabilities.EnergyStorage.BLOCK, worldPosition.relative(face), face.getOpposite());
      if (neighbour != null && neighbour.canReceive()) {
        sinks.add(neighbour::receiveEnergy);
      }
    }

    final int moved = EnergyDistribution.push(energy, sinks);
    if (moved > 0) {
      setChanged();
    }
    return moved;
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putInt(ENERGY_TAG, energy.stored());
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    // A missing key reads as zero, which is the right answer for a freshly placed block.
    energy.setStored(tag.getInt(ENERGY_TAG));
  }
}
