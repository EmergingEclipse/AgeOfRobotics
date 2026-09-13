package com.modpackswork.ageofrobotics;

import com.modpackswork.ageofrobotics.core.ModMetadata;
import com.modpackswork.ageofrobotics.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeoForge entrypoint for Age of Robotics.
 *
 * <p>Deliberately empty of gameplay content: each feature area registers itself from its own
 * package ({@code item}, {@code block}, {@code dimension}, {@code robot}, {@code power}) as those
 * tickets land. This class only owns the mod event bus wiring.
 */
@Mod(ModMetadata.MOD_ID)
public class AgeOfRobotics {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModMetadata.MOD_NAME);

  /**
   * Called by FML when the mod is constructed.
   *
   * @param modEventBus the mod-specific event bus registrations should be attached to
   * @param modContainer this mod's container, used for config registration
   */
  public AgeOfRobotics(IEventBus modEventBus, ModContainer modContainer) {
    LOGGER.info("{} loading ({})", ModMetadata.MOD_NAME, ModMetadata.MOD_ID);
    ModItems.register(modEventBus);
  }
}
