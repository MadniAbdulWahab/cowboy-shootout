package io.github.madniabdulwahab.shootout.simulation;

import java.util.Random;

/**
 * Reproducible pseudorandom values for a shootout.
 */
public final class SeededShootoutRandom implements ShootoutRandom {

    private final Random random;

    public SeededShootoutRandom(long seed) {
        random = new Random(seed);
    }

    @Override
    public int selectStartingCowboy(int cowboyCount) {
        if (cowboyCount <= 0) {
            throw new IllegalArgumentException(
                    "The number of cowboys must be positive."
            );
        }

        return random.nextInt(cowboyCount);
    }

    @Override
    public int nextDamage() {
        return random.nextInt(
                CowboyCircle.MINIMUM_DAMAGE,
                CowboyCircle.MAXIMUM_DAMAGE + 1
        );
    }
}
