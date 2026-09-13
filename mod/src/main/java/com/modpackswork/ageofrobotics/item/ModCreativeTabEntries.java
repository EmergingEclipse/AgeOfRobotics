package com.modpackswork.ageofrobotics.item;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * Puts this mod's items into vanilla creative tabs.
 *
 * <p>A creative tab of the mod's own is a separate ticket, so until then each item joins the
 * vanilla tab that matches what it is for. The Basic Electrical Circuit is a crafting component,
 * which puts it in Ingredients alongside redstone and the vanilla ingots it is made from.
 *
 * <p>This lives in its own file so that later item tickets add a line here instead of editing the
 * mod entrypoint, which every branch would otherwise conflict on.
 */
public final class ModCreativeTabEntries {

  private ModCreativeTabEntries() {}

  /**
   * Subscribes the tab contribution to the mod event bus.
   *
   * @param modEventBus the mod event bus, from the mod constructor
   */
  public static void register(IEventBus modEventBus) {
    modEventBus.addListener(ModCreativeTabEntries::onBuildCreativeTabContents);
  }

  private static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
    if (CreativeModeTabs.INGREDIENTS.equals(event.getTabKey())) {
      event.accept(ModItems.BASIC_ELECTRICAL_CIRCUIT);
    }
  }
}
