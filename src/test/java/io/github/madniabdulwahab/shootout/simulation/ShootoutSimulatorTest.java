package io.github.madniabdulwahab.shootout.simulation;

import io.github.madniabdulwahab.shootout.domain.Direction;
import io.github.madniabdulwahab.shootout.domain.ShootoutResult;
import io.github.madniabdulwahab.shootout.domain.ShotEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShootoutSimulatorTest {

    @Test
    void oneCowboyFinishesWithoutAnyShots() {
        ShootoutSimulator simulator =
                new ShootoutSimulator(1, new ScriptedRandom(0));

        assertTrue(simulator.isFinished());
        assertEquals(1, simulator.startingCowboyNumber());
        assertEquals(new ShootoutResult(1, 10, 0), simulator.result());
        assertThrows(IllegalStateException.class, simulator::nextShot);
    }

    @Test
    void survivingTargetBecomesTheNextShooter() {
        ShootoutSimulator simulator =
                new ShootoutSimulator(3, new ScriptedRandom(0, 1, 1));

        ShotEvent firstShot = simulator.nextShot();
        ShotEvent secondShot = simulator.nextShot();

        assertEquals(
                new ShotEvent(
                        1, 1, 10, Direction.RIGHT,
                        2, 1, 9, false
                ),
                firstShot
        );

        assertEquals(
                new ShotEvent(
                        2, 2, 9, Direction.LEFT,
                        1, 1, 9, false
                ),
                secondShot
        );
    }

    @Test
    void shooterContinuesAfterKillingTargetAndCircleCloses() {
        ShootoutSimulator simulator =
                new ShootoutSimulator(
                        3,
                        new ScriptedRandom(0, 5, 5, 5, 5, 1)
                );

        simulator.nextShot();
        simulator.nextShot();
        simulator.nextShot();

        ShotEvent killingShot = simulator.nextShot();
        ShotEvent followingShot = simulator.nextShot();

        assertEquals(3, killingShot.shooterNumber());
        assertEquals(2, killingShot.targetNumber());
        assertTrue(killingShot.targetKilled());
        assertEquals(0, killingShot.targetHealthPointsAfter());

        assertEquals(3, followingShot.shooterNumber());
        assertEquals(1, followingShot.targetNumber());
        assertEquals(2, simulator.livingCowboyCount());
    }

    @Test
    void returnsResultAfterFinalCowboyIsKilled() {
        ShootoutSimulator simulator =
                new ShootoutSimulator(2, new ScriptedRandom(0, 5, 5, 5));

        while (!simulator.isFinished()) {
            simulator.nextShot();
        }

        assertEquals(new ShootoutResult(1, 5, 3), simulator.result());
    }

    @Test
    void resultCannotBeRequestedBeforeShootoutFinishes() {
        ShootoutSimulator simulator =
                new ShootoutSimulator(2, new ScriptedRandom(0, 1));

        assertThrows(IllegalStateException.class, simulator::result);
    }

    @Test
    void rejectsInvalidStartingCowboyFromRandomSource() {
        assertThrows(
                IllegalStateException.class,
                () -> new ShootoutSimulator(
                        3,
                        new ScriptedRandom(3)
                )
        );
    }

    private static final class ScriptedRandom implements ShootoutRandom {

        private final int startingCowboyId;
        private final int[] damages;
        private int damageIndex;

        private ScriptedRandom(int startingCowboyId, int... damages) {
            this.startingCowboyId = startingCowboyId;
            this.damages = damages;
        }

        @Override
        public int selectStartingCowboy(int cowboyCount) {
            return startingCowboyId;
        }

        @Override
        public int nextDamage() {
            if (damageIndex >= damages.length) {
                throw new AssertionError("No scripted damage value remaining");
            }

            return damages[damageIndex++];
        }
    }
}
