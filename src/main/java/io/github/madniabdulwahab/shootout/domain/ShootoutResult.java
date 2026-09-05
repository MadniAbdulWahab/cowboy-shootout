package io.github.madniabdulwahab.shootout.domain;

/**
 * The immutable final result of a completed shootout.
 */
public record ShootoutResult(
        int winnerNumber,
        int winnerHealthPoints,
        long totalShots
) {

    public ShootoutResult {
        if (winnerNumber <= 0) {
            throw new IllegalArgumentException(
                    "The winner number must be positive."
            );
        }

        if (winnerHealthPoints <= 0) {
            throw new IllegalArgumentException(
                    "The winner must have positive health points."
            );
        }

        if (totalShots < 0) {
            throw new IllegalArgumentException(
                    "The total number of shots must not be negative."
            );
        }
    }
}
