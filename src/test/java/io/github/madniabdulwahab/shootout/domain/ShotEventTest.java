package io.github.madniabdulwahab.shootout.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShotEventTest {

    @Test
    void rejectsInvalidBasicValues() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ShotEvent(
                                0, 1, 10, Direction.RIGHT,
                                2, 3, 7, false
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ShotEvent(
                                1, 0, 10, Direction.RIGHT,
                                2, 3, 7, false
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ShotEvent(
                                1, 1, 10, Direction.RIGHT,
                                1, 3, 7, false
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ShotEvent(
                                1, 1, 0, Direction.RIGHT,
                                2, 3, 7, false
                        )
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new ShotEvent(
                                1, 1, 10, null,
                                2, 3, 7, false
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ShotEvent(
                                1, 1, 10, Direction.RIGHT,
                                2, 0, 7, false
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ShotEvent(
                                1, 1, 10, Direction.RIGHT,
                                2, 6, 4, false
                        )
                )
        );
    }

    @Test
    void rejectsDirectionThatDoesNotMatchShooterHealth() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ShotEvent(
                                1, 1, 10, Direction.LEFT,
                                2, 3, 7, false
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ShotEvent(
                                1, 1, 9, Direction.RIGHT,
                                2, 3, 7, false
                        )
                )
        );
    }

    @Test
    void rejectsKilledFlagThatDoesNotMatchTargetHealth() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ShotEvent(
                                1, 1, 10, Direction.RIGHT,
                                2, 5, 0, false
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ShotEvent(
                                1, 1, 10, Direction.RIGHT,
                                2, 5, 1, true
                        )
                )
        );
    }
}
