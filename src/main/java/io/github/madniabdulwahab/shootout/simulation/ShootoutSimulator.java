package io.github.madniabdulwahab.shootout.simulation;

import io.github.madniabdulwahab.shootout.domain.Direction;
import io.github.madniabdulwahab.shootout.domain.ShootoutResult;
import io.github.madniabdulwahab.shootout.domain.ShotEvent;

import java.util.Objects;

/**
 * Executes the shootout rules one shot at a time.
 */
public final class ShootoutSimulator {

    private final CowboyCircle circle;
    private final ShootoutRandom random;
    private final int startingCowboyId;

    private int activeCowboyId;
    private long shotCount;

    public ShootoutSimulator(int cowboyCount, ShootoutRandom random) {
        this.random = Objects.requireNonNull(random, "random must not be null");
        this.circle = new CowboyCircle(cowboyCount);

        int selectedCowboyId = random.selectStartingCowboy(cowboyCount);
        if (selectedCowboyId < 0 || selectedCowboyId >= cowboyCount) {
            throw new IllegalStateException(
                    "Random source returned an invalid starting cowboy: "
                            + selectedCowboyId
            );
        }

        this.startingCowboyId = selectedCowboyId;
        this.activeCowboyId = selectedCowboyId;
    }

    public int startingCowboyNumber() {
        return toCowboyNumber(startingCowboyId);
    }

    public int livingCowboyCount() {
        return circle.livingCount();
    }

    public boolean isFinished() {
        return circle.livingCount() == 1;
    }

    public ShotEvent nextShot() {
        if (isFinished()) {
            throw new IllegalStateException("The shootout has already finished");
        }

        int shooterId = activeCowboyId;
        int shooterHP = circle.healthPointsOf(shooterId);

        Direction direction = shooterHP % 2 == 0
                ? Direction.RIGHT
                : Direction.LEFT;

        int targetId = direction == Direction.RIGHT
                ? circle.rightNeighborOf(shooterId)
                : circle.leftNeighborOf(shooterId);

        int damage = random.nextDamage();
        int targetHPAfter = circle.applyDamage(targetId, damage);
        boolean targetKilled = targetHPAfter <= 0;

        long currentShotNumber = shotCount + 1;

        ShotEvent event = new ShotEvent(
                currentShotNumber,
                toCowboyNumber(shooterId),
                shooterHP,
                direction,
                toCowboyNumber(targetId),
                damage,
                targetHPAfter,
                targetKilled
        );

        if (targetKilled) {
            circle.remove(targetId);
        } else {
            activeCowboyId = targetId;
        }

        shotCount = currentShotNumber;
        return event;
    }

    public ShootoutResult result() {
        if (!isFinished()) {
            throw new IllegalStateException("The shootout has not finished yet");
        }

        return new ShootoutResult(
                toCowboyNumber(activeCowboyId),
                circle.healthPointsOf(activeCowboyId),
                shotCount
        );
    }

    private static int toCowboyNumber(int cowboyId) {
        return cowboyId + 1;
    }
}
