package com.modpackswork.ageofrobotics.power;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * The one place a power block is joined to the Forge Energy capability.
 *
 * <p>There is deliberately no shared list of registered types here. Each power ticket adds its own
 * listener for {@link RegisterCapabilitiesEvent} on the mod event bus from its own class and calls
 * {@link #registerEnergyBlockEntity(RegisterCapabilitiesEvent, BlockEntityType)} with the type it
 * registered. Nothing in this package or in {@code AgeOfRobotics} has to be edited to add a
 * machine, so the power tickets never conflict with each other over a central registry.
 *
 * <p>The shape a later ticket writes, once it has a block entity type of its own:
 *
 * <pre>{@code
 * public final class SolarPanelRegistration {
 *
 *   public static void register(IEventBus modEventBus) {
 *     modEventBus.addListener(SolarPanelRegistration::registerCapabilities);
 *   }
 *
 *   private static void registerCapabilities(RegisterCapabilitiesEvent event) {
 *     PowerCapabilities.registerEnergyBlockEntity(event, SOLAR_PANEL_BLOCK_ENTITY.get());
 *   }
 * }
 * }</pre>
 *
 * <p>No type is registered yet, because there are no blocks in the mod so far. The first power
 * ticket to add one is the first caller.
 */
public final class PowerCapabilities {

  private PowerCapabilities() {}

  /**
   * Exposes a block entity type's stored RF to the world on every face.
   *
   * <p>The role in the block's {@link com.modpackswork.ageofrobotics.core.power.EnergySpec} decides
   * whether a given face actually accepts or gives energy, so there is nothing to configure per
   * side. The handler instance lives as long as the block entity does and is never swapped out, so
   * no capability invalidation is needed after a transfer.
   *
   * @param event the capability registration event from the mod event bus
   * @param type the block entity type to expose
   * @param <BE> the block entity class, which must hold an energy buffer
   */
  public static <BE extends AbstractEnergyBlockEntity> void registerEnergyBlockEntity(
      RegisterCapabilitiesEvent event, BlockEntityType<BE> type) {
    event.registerBlockEntity(
        Capabilities.EnergyStorage.BLOCK, type, (blockEntity, face) -> blockEntity.energyStorage());
  }
}
