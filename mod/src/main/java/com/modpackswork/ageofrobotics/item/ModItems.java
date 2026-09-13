package com.modpackswork.ageofrobotics.item;

import com.modpackswork.ageofrobotics.core.ModMetadata;
import com.modpackswork.ageofrobotics.core.robot.CircuitTier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Every item Age of Robotics registers.
 *
 * <p>One {@link DeferredRegister.Items} for the whole mod, attached to the mod event bus by {@link
 * com.modpackswork.ageofrobotics.AgeOfRobotics}. Later item tickets add a constant here and their
 * own resources; nothing else in the mod needs to change.
 *
 * <p>Registry names come from the {@code core} model rather than from string literals, so the item,
 * its recipe result and the gameplay rules that reference it cannot drift apart.
 */
public final class ModItems {

  private static final DeferredRegister.Items ITEMS =
      DeferredRegister.createItems(ModMetadata.MOD_ID);

  /**
   * The Basic Electrical Circuit, the overworld-only crafting gate on tier 1 and tier 2 robot parts
   * (issue #15).
   */
  public static final DeferredItem<Item> BASIC_ELECTRICAL_CIRCUIT =
      ITEMS.registerSimpleItem(CircuitTier.BASIC.itemPath(), new Item.Properties());

  private ModItems() {}

  /**
   * Attaches the item registry and the creative tab contribution to the mod event bus.
   *
   * @param modEventBus the mod event bus, from the mod constructor
   */
  public static void register(IEventBus modEventBus) {
    ITEMS.register(modEventBus);
    ModCreativeTabEntries.register(modEventBus);
  }
}
