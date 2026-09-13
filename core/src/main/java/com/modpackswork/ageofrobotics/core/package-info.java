/**
 * Loader-independent core of Age of Robotics.
 *
 * <p>Nothing in this module may import Minecraft or NeoForge classes: it holds the gameplay data
 * model (robot parts and stats, power maths, tiering) so it can be unit tested without launching
 * the game. The {@code mod} module adapts these types to Minecraft registries and block entities.
 */
package com.modpackswork.ageofrobotics.core;
