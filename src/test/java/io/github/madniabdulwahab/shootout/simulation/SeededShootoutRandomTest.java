package io.github.madniabdulwahab.shootout.simulation;

import io.github.madniabdulwahab.shootout.domain.ShootoutRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeededShootoutRandomTest {

    @Test
    void producesSameSequenceForSameSeed() {
        ShootoutRandom first = new SeededShootoutRandom(12345L);
        ShootoutRandom second = new SeededShootoutRandom(12345L);

        for (int iteration = 0; iteration < 100; iteration++) {
            assertEquals(
                    first.selectStartingCowboy(20),
                    second.selectStartingCowboy(20)
            );

            assertEquals(
                    first.nextDamage(),
                    second.nextDamage()
            );
        }
    }

    @Test
    void producesValuesInsideRequiredRanges() {
        ShootoutRandom random = new SeededShootoutRandom(98765L);

        for (int iteration = 0; iteration < 10_000; iteration++) {
            int startingCowboy = random.selectStartingCowboy(50);
            int damage = random.nextDamage();

            assertTrue(startingCowboy >= 0);
            assertTrue(startingCowboy < 50);
            assertTrue(damage >= ShootoutRules.MINIMUM_DAMAGE);
            assertTrue(damage <= ShootoutRules.MAXIMUM_DAMAGE);
        }
    }

    @Test
    void rejectsNonPositiveCowboyCount() {
        ShootoutRandom random = new SeededShootoutRandom(1L);

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> random.selectStartingCowboy(0)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> random.selectStartingCowboy(-1)
                )
        );
    }
}
