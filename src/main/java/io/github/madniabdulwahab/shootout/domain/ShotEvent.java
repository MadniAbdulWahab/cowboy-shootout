package io.github.madniabdulwahab.shootout.domain;

import java.util.Objects;

import static io.github.madniabdulwahab.shootout.domain.ShootoutRules.MAXIMUM_DAMAGE;
import static io.github.madniabdulwahab.shootout.domain.ShootoutRules.MINIMUM_DAMAGE;

/**
 * An immutable description of one shot in the shootout.
 *
 * <p>Cowboy numbers are one-based because they are intended for the
 * console and JSON protocol.</p>
 */
public record ShotEvent(
        long shotNumber,
        int shooterNumber,
        int shooterHealthPoints,
        Direction direction,
        int targetNumber,
        int damage,
        int targetHealthPointsAfter,
        boolean targetKilled
) {

    public ShotEvent {
        if (shotNumber <= 0) {
            throw new IllegalArgumentException(
                    "The shot number must be positive."
            );
        }

        if (shooterNumber <= 0 || targetNumber <= 0) {
            throw new IllegalArgumentException(
                    "Cowboy numbers must be positive."
            );
        }

        if (shooterNumber == targetNumber) {
            throw new IllegalArgumentException(
                    "A cowboy cannot shoot themselves."
            );
        }

        if (shooterHealthPoints <= 0) {
            throw new IllegalArgumentException(
                    "The shooter must be alive."
            );
        }

        Objects.requireNonNull(direction, "Direction must not be null.");

        if (damage < MINIMUM_DAMAGE || damage > MAXIMUM_DAMAGE) {
            throw new IllegalArgumentException(
                    "Damage must be between "
                            + MINIMUM_DAMAGE
                            + " and "
                            + MAXIMUM_DAMAGE
                            + "."
            );
        }

        Direction expectedDirection =
                shooterHealthPoints % 2 == 0
                        ? Direction.RIGHT
                        : Direction.LEFT;

        if (direction != expectedDirection) {
            throw new IllegalArgumentException(
                    "The direction does not match the shooter's health points."
            );
        }

        boolean targetActuallyKilled = targetHealthPointsAfter <= 0;

        if (targetKilled != targetActuallyKilled) {
            throw new IllegalArgumentException(
                    "The killed flag does not match the target's health points."
            );
        }
    }
}
