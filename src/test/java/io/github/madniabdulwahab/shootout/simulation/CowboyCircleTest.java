package io.github.madniabdulwahab.shootout.simulation;

import org.junit.jupiter.api.Test;

import io.github.madniabdulwahab.shootout.domain.ShootoutRules;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CowboyCircleTest {

    @Test
    void rejectsNonPositiveCowboyCounts() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new CowboyCircle(0)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new CowboyCircle(-1)
                )
        );
    }

    @Test
    void initializesEveryCowboyWithTenHealthPoints() {
        CowboyCircle circle = new CowboyCircle(4);

        assertEquals(4, circle.livingCount());

        for (int cowboyId = 0; cowboyId < 4; cowboyId++) {
            assertEquals(
                    ShootoutRules.INITIAL_HEALTH_POINTS,
                    circle.healthPointsOf(cowboyId)
            );
            assertTrue(circle.contains(cowboyId));
        }
    }

    @Test
    void connectsCowboysInBothDirectionsWithWraparound() {
        CowboyCircle circle = new CowboyCircle(4);

        assertAll(
                () -> assertEquals(3, circle.leftNeighborOf(0)),
                () -> assertEquals(1, circle.rightNeighborOf(0)),
                () -> assertEquals(0, circle.leftNeighborOf(1)),
                () -> assertEquals(2, circle.rightNeighborOf(1)),
                () -> assertEquals(1, circle.leftNeighborOf(2)),
                () -> assertEquals(3, circle.rightNeighborOf(2)),
                () -> assertEquals(2, circle.leftNeighborOf(3)),
                () -> assertEquals(0, circle.rightNeighborOf(3))
        );
    }

    @Test
    void connectsSingleCowboyToItself() {
        CowboyCircle circle = new CowboyCircle(1);

        assertAll(
                () -> assertEquals(1, circle.livingCount()),
                () -> assertEquals(0, circle.leftNeighborOf(0)),
                () -> assertEquals(0, circle.rightNeighborOf(0))
        );
    }

    @Test
    void appliesDamageAndReturnsRemainingHealthPoints() {
        CowboyCircle circle = new CowboyCircle(2);

        int healthAfterFirstShot = circle.applyDamage(1, 4);
        int healthAfterSecondShot = circle.applyDamage(1, 5);

        assertAll(
                () -> assertEquals(6, healthAfterFirstShot),
                () -> assertEquals(1, healthAfterSecondShot),
                () -> assertEquals(1, circle.healthPointsOf(1))
        );
    }

    @Test
    void permitsHealthPointsToBecomeNegative() {
        CowboyCircle circle = new CowboyCircle(2);

        circle.applyDamage(1, 5);
        circle.applyDamage(1, 4);

        int remainingHealth = circle.applyDamage(1, 5);

        assertEquals(-4, remainingHealth);
    }

    @Test
    void rejectsDamageOutsideTheAllowedRange() {
        CowboyCircle circle = new CowboyCircle(2);

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> circle.applyDamage(1, 0)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> circle.applyDamage(1, 6)
                )
        );
    }

    @Test
    void removesDefeatedCowboyAndClosesCircle() {
        CowboyCircle circle = new CowboyCircle(4);

        circle.applyDamage(1, 5);
        circle.applyDamage(1, 5);
        circle.remove(1);

        assertAll(
                () -> assertEquals(3, circle.livingCount()),
                () -> assertFalse(circle.contains(1)),
                () -> assertEquals(2, circle.rightNeighborOf(0)),
                () -> assertEquals(0, circle.leftNeighborOf(2)),
                () -> assertEquals(3, circle.leftNeighborOf(0)),
                () -> assertEquals(0, circle.rightNeighborOf(3))
        );
    }

    @Test
    void removesFirstCowboyAndPreservesWraparound() {
        CowboyCircle circle = new CowboyCircle(3);

        circle.applyDamage(0, 5);
        circle.applyDamage(0, 5);
        circle.remove(0);

        assertAll(
                () -> assertEquals(2, circle.livingCount()),
                () -> assertEquals(2, circle.leftNeighborOf(1)),
                () -> assertEquals(1, circle.rightNeighborOf(2))
        );
    }

    @Test
    void rejectsRemovingLivingCowboy() {
        CowboyCircle circle = new CowboyCircle(2);

        assertThrows(
                IllegalStateException.class,
                () -> circle.remove(1)
        );
    }

    @Test
    void rejectsAccessToRemovedCowboy() {
        CowboyCircle circle = new CowboyCircle(2);

        circle.applyDamage(1, 5);
        circle.applyDamage(1, 5);
        circle.remove(1);

        assertAll(
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> circle.healthPointsOf(1)
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> circle.leftNeighborOf(1)
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> circle.applyDamage(1, 1)
                )
        );
    }

    @Test
    void refusesToRemoveFinalCowboy() {
        CowboyCircle circle = new CowboyCircle(1);

        circle.applyDamage(0, 5);
        circle.applyDamage(0, 5);

        assertThrows(
                IllegalStateException.class,
                () -> circle.remove(0)
        );

        assertEquals(1, circle.livingCount());
    }

    @Test
    void rejectsInvalidCowboyIds() {
        CowboyCircle circle = new CowboyCircle(3);

        assertAll(
                () -> assertThrows(
                        IndexOutOfBoundsException.class,
                        () -> circle.healthPointsOf(-1)
                ),
                () -> assertThrows(
                        IndexOutOfBoundsException.class,
                        () -> circle.healthPointsOf(3)
                )
        );
    }
}
