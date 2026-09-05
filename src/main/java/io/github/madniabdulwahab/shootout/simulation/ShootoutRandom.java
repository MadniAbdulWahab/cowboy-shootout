package io.github.madniabdulwahab.shootout.simulation;

/**
 * Provides the random decisions required by a shootout.
 *
 * <p>Separating randomness from the simulation allows tests to supply
 * predetermined values.</p>
 */
public interface ShootoutRandom {

    /**
     * Returns a zero-based starting cowboy ID.
     */
    int selectStartingCowboy(int cowboyCount);

    /**
     * Returns a damage value from 1 through 5.
     */
    int nextDamage();
}
