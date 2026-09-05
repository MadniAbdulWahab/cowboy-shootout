package io.github.madniabdulwahab.shootout.simulation;

import java.util.Arrays;
import java.util.Objects;

/**
 * Stores the health and neighbor relationships of the cowboys who are
 * currently part of the shootout.
 *
 * <p>Cowboy IDs are zero-based internally. Left and right neighbors are
 * represented using arrays, allowing constant-time neighbor lookup and
 * removal.</p>
 */
final class CowboyCircle {

    static final int INITIAL_HEALTH_POINTS = 10;
    static final int MINIMUM_DAMAGE = 1;
    static final int MAXIMUM_DAMAGE = 5;

    private final int[] healthPoints;
    private final int[] leftNeighbors;
    private final int[] rightNeighbors;
    private final boolean[] inCircle;

    private int livingCount;

    CowboyCircle(int cowboyCount) {
        if (cowboyCount <= 0) {
            throw new IllegalArgumentException(
                    "The number of cowboys must be positive."
            );
        }

        healthPoints = new int[cowboyCount];
        leftNeighbors = new int[cowboyCount];
        rightNeighbors = new int[cowboyCount];
        inCircle = new boolean[cowboyCount];
        livingCount = cowboyCount;

        Arrays.fill(healthPoints, INITIAL_HEALTH_POINTS);
        Arrays.fill(inCircle, true);

        for (int cowboyId = 0; cowboyId < cowboyCount; cowboyId++) {
            leftNeighbors[cowboyId] =
                    cowboyId == 0 ? cowboyCount - 1 : cowboyId - 1;

            rightNeighbors[cowboyId] =
                    cowboyId == cowboyCount - 1 ? 0 : cowboyId + 1;
        }
    }

    int livingCount() {
        return livingCount;
    }

    int healthPointsOf(int cowboyId) {
        requirePresent(cowboyId);
        return healthPoints[cowboyId];
    }

    int leftNeighborOf(int cowboyId) {
        requirePresent(cowboyId);
        return leftNeighbors[cowboyId];
    }

    int rightNeighborOf(int cowboyId) {
        requirePresent(cowboyId);
        return rightNeighbors[cowboyId];
    }

    boolean contains(int cowboyId) {
        checkId(cowboyId);
        return inCircle[cowboyId];
    }

    /**
     * Applies damage and returns the target's remaining health points.
     */
    int applyDamage(int cowboyId, int damage) {
        requirePresent(cowboyId);

        if (damage < MINIMUM_DAMAGE || damage > MAXIMUM_DAMAGE) {
            throw new IllegalArgumentException(
                    "Damage must be between "
                            + MINIMUM_DAMAGE
                            + " and "
                            + MAXIMUM_DAMAGE
                            + "."
            );
        }

        if (healthPoints[cowboyId] <= 0) {
            throw new IllegalStateException(
                    "A defeated cowboy cannot take further damage."
            );
        }

        healthPoints[cowboyId] -= damage;
        return healthPoints[cowboyId];
    }

    /**
     * Removes a defeated cowboy and connects the two surrounding cowboys.
     */
    void remove(int cowboyId) {
        requirePresent(cowboyId);

        if (healthPoints[cowboyId] > 0) {
            throw new IllegalStateException(
                    "A living cowboy cannot be removed."
            );
        }

        if (livingCount == 1) {
            throw new IllegalStateException(
                    "The final cowboy cannot be removed."
            );
        }

        int leftNeighbor = leftNeighbors[cowboyId];
        int rightNeighbor = rightNeighbors[cowboyId];

        rightNeighbors[leftNeighbor] = rightNeighbor;
        leftNeighbors[rightNeighbor] = leftNeighbor;

        inCircle[cowboyId] = false;
        leftNeighbors[cowboyId] = -1;
        rightNeighbors[cowboyId] = -1;
        livingCount--;
    }

    private void requirePresent(int cowboyId) {
        checkId(cowboyId);

        if (!inCircle[cowboyId]) {
            throw new IllegalStateException(
                    "Cowboy " + (cowboyId + 1) + " is no longer in the circle."
            );
        }
    }

    private void checkId(int cowboyId) {
        Objects.checkIndex(cowboyId, healthPoints.length);
    }
}
