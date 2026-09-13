package com.modpackswork.ageofrobotics.block;

import com.modpackswork.ageofrobotics.core.ModMetadata;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * Puts this mod's blocks into vanilla creative tabs.
 *
 * <p>Deliberately a file of its own so tickets landing in parallel do not all edit one shared
 * registration class.
 *
 * <p>No mod tab of our own yet. A tab with two terrain blocks in it is worse than no tab, and the
 * blocks are easier to find where players already look for terrain. Natural Blocks is the tab
 * vanilla files stone and deepslate under for exactly this role.
 *
 * <p>The annotation names no bus on purpose. NeoForge 21.1 deprecated {@code bus()} and picks the
 * bus per listener from the event type, and {@link BuildCreativeModeTabContentsEvent} is a mod bus
 * event, so it is subscribed there automatically.
 */
@EventBusSubscriber(modid = ModMetadata.MOD_ID)
public final class BlockCreativeTabEntries {

  private BlockCreativeTabEntries() {}

  /**
   * Adds the scrap ground block set to the Natural Blocks tab.
   *
   * @param event the tab contents event, fired once per creative tab
   */
  @SubscribeEvent
  public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
    if (!CreativeModeTabs.NATURAL_BLOCKS.equals(event.getTabKey())) {
      return;
    }

    ModBlocks.scrapGroundSetItems().forEach(event::accept);
  }
}
