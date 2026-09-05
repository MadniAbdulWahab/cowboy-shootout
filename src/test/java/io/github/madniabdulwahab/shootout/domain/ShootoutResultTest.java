package io.github.madniabdulwahab.shootout.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShootoutResultTest {

    @Test
    void rejectsInvalidResultValues() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ShootoutResult(0, 5, 10)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ShootoutResult(1, 0, 10)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ShootoutResult(1, 5, -1)
                )
        );
    }
}
