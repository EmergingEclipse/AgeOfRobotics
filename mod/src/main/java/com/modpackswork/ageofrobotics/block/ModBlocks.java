package com.modpackswork.ageofrobotics.block;

import com.modpackswork.ageofrobotics.core.ModMetadata;
import com.modpackswork.ageofrobotics.core.world.ScrapGroundBlock;
import java.util.List;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Every block this mod registers, plus the block item that lets each one be carried and placed.
 *
 * <p>The registry names come from {@link ScrapGroundBlock} in the {@code core} module so worldgen
 * (issue #6), the structural guards and this class cannot drift apart. Only the Minecraft-typed
 * properties live here.
 */
public final class ModBlocks {

  private static final DeferredRegister.Blocks BLOCKS =
      DeferredRegister.createBlocks(ModMetadata.MOD_ID);
  private static final DeferredRegister.Items BLOCK_ITEMS =
      DeferredRegister.createItems(ModMetadata.MOD_ID);

  /**
   * The Machine Dimension's surface and mid-depth filler. Properties copy vanilla stone, which is
   * the role it plays: strength 1.5F with 6.0F blast resistance, basedrum note block instrument and
   * drops only with the correct tool.
   *
   * <p>A rusted junk aggregate is not stone, so two properties deliberately differ. The map colour
   * is the dull oxidised orange of terracotta rather than grey, so the dimension reads as its own
   * place on a map. The sound is copper rather than metal, because metal is the loud iron block
   * clang and this block is mined constantly across an entire dimension.
   */
  public static final DeferredBlock<Block> SCRAP_BLOCK =
      BLOCKS.registerSimpleBlock(
          ScrapGroundBlock.SCRAP_BLOCK.path(),
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.TERRACOTTA_ORANGE)
              .instrument(NoteBlockInstrument.BASEDRUM)
              .requiresCorrectToolForDrops()
              .strength(1.5F, 6.0F)
              .sound(SoundType.COPPER));

  /**
   * The deep-layer filler, the deepslate analogue issue #6 needs below the scrap layer. Properties
   * copy vanilla deepslate: strength 3.0F with 6.0F blast resistance and the correct tool required.
   *
   * <p>Dark grey map colour and the heavy netherite block sound both exist so a player can tell
   * they have crossed into the deep layer by look and by ear, exactly as deepslate does.
   */
  public static final DeferredBlock<Block> COMPACTED_SCRAP =
      BLOCKS.registerSimpleBlock(
          ScrapGroundBlock.COMPACTED_SCRAP.path(),
          BlockBehaviour.Properties.of()
              .mapColor(MapColor.COLOR_GRAY)
              .instrument(NoteBlockInstrument.BASEDRUM)
              .requiresCorrectToolForDrops()
              .strength(3.0F, 6.0F)
              .sound(SoundType.NETHERITE_BLOCK));

  /** Block item for {@link #SCRAP_BLOCK}, so the block can be carried and placed. */
  public static final DeferredItem<BlockItem> SCRAP_BLOCK_ITEM =
      BLOCK_ITEMS.registerSimpleBlockItem(SCRAP_BLOCK, new Item.Properties());

  /** Block item for {@link #COMPACTED_SCRAP}, so the block can be carried and placed. */
  public static final DeferredItem<BlockItem> COMPACTED_SCRAP_ITEM =
      BLOCK_ITEMS.registerSimpleBlockItem(COMPACTED_SCRAP, new Item.Properties());

  private ModBlocks() {}

  /**
   * The terrain filler blocks, in the order they are meant to stack from the surface downwards.
   * Worldgen in issue #6 and structure work in issue #35 should read this rather than naming block
   * fields one by one.
   *
   * @return the scrap ground block set, surface layer first
   */
  public static List<DeferredBlock<Block>> scrapGroundSet() {
    return List.of(SCRAP_BLOCK, COMPACTED_SCRAP);
  }

  /**
   * The block items of the scrap ground set, in the same order as {@link #scrapGroundSet()}.
   *
   * @return the block items, surface layer first
   */
  public static List<DeferredItem<BlockItem>> scrapGroundSetItems() {
    return List.of(SCRAP_BLOCK_ITEM, COMPACTED_SCRAP_ITEM);
  }

  /**
   * Attaches both registers to the mod event bus. Nothing is registered until this runs.
   *
   * @param modEventBus the mod-specific event bus from the mod constructor
   */
  public static void register(IEventBus modEventBus) {
    BLOCKS.register(modEventBus);
    BLOCK_ITEMS.register(modEventBus);
  }
}
