package com.modpackswork.ageofrobotics.core.power;

import java.util.List;
import java.util.Objects;

/**
 * The push convention for handing RF to neighbours.
 *
 * <p>Age of Robotics pushes rather than pulls: a block that has energy to spare offers it outward
 * on its own tick, and a block that needs energy simply waits to be filled. Only one side of any
 * pair ever drives a transfer, so two adjacent blocks cannot move the same RF twice in a tick, and
 * a machine that is not ticking never reaches into its neighbours.
 *
 * <p>Sinks are served in the order given, first come first served, until the source's per call
 * extract budget is used up. Callers that want to spread output evenly over time can rotate the
 * order they pass in. There is no fair split, because a split that leaves every neighbour slightly
 * short is worse than filling them one at a time.
 */
public final class EnergyDistribution {

  private EnergyDistribution() {}

  /**
   * Offers the source's spare energy to each sink in turn.
   *
   * <p>The whole call is limited to one extract budget, so pushing to six neighbours moves no more
   * RF in total than pushing to one. A source whose role does not provide energy, or that is empty,
   * moves nothing and makes no offers at all.
   *
   * @param source the buffer with energy to spare
   * @param sinks the neighbours to offer it to, in the order they should be served
   * @return the total RF moved out of the source
   * @throws NullPointerException if the source or the list is null
   */
  public static int push(EnergyBuffer source, List<? extends EnergySink> sinks) {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(sinks, "sinks");

    final int budget = source.extract(source.spec().maxExtractRate(), true);
    if (budget <= 0) {
      return 0;
    }

    int moved = 0;
    for (final EnergySink sink : sinks) {
      final int remaining = budget - moved;
      if (remaining <= 0) {
        break;
      }
      final int accepted = sink.receiveEnergy(remaining, false);
      if (accepted > 0) {
        // The rate cap was already applied once when the budget was worked out, so take the
        // accepted amount off the buffer directly rather than charging it against the rate again.
        source.drain(accepted);
        moved += accepted;
      }
    }
    return moved;
  }
}
