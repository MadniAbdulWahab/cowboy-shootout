package io.github.madniabdulwahab.shootout;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongSupplier;

import io.github.madniabdulwahab.shootout.domain.ShootoutResult;
import io.github.madniabdulwahab.shootout.domain.ShotEvent;
import io.github.madniabdulwahab.shootout.protocol.ChecksumCalculator;
import io.github.madniabdulwahab.shootout.protocol.ProtocolWriter;
import io.github.madniabdulwahab.shootout.simulation.SeededShootoutRandom;
import io.github.madniabdulwahab.shootout.simulation.ShootoutSimulator;

/**
 * Command-line entry point for the cowboy shootout.
 */
public final class Main {

    private static final int SUCCESS_EXIT_CODE = 0;
    private static final int APPLICATION_ERROR_EXIT_CODE = 1;
    private static final int INPUT_ERROR_EXIT_CODE = 2;

    private static final Path DEFAULT_PROTOCOL_PATH =
            Path.of("shootout-protocol.json");

    private Main() {
    }

    public static void main(String[] args) {
        int exitCode = run(
                args,
                System.out,
                System.err,
                DEFAULT_PROTOCOL_PATH,
                () -> ThreadLocalRandom.current().nextLong()
        );

        if (exitCode != SUCCESS_EXIT_CODE) {
            System.exit(exitCode);
        }
    }

    static int run(
            String[] args,
            PrintStream output,
            PrintStream error,
            Path protocolPath,
            LongSupplier seedSupplier
    ) {
        Objects.requireNonNull(args, "args must not be null");
        Objects.requireNonNull(output, "output must not be null");
        Objects.requireNonNull(error, "error must not be null");
        Objects.requireNonNull(
                protocolPath,
                "protocolPath must not be null"
        );
        Objects.requireNonNull(
                seedSupplier,
                "seedSupplier must not be null"
        );

        int cowboyCount;

        try {
            cowboyCount = parseCowboyCount(args);
        } catch (IllegalArgumentException exception) {
            error.println("Input error: " + exception.getMessage());
            printUsage(error);
            return INPUT_ERROR_EXIT_CODE;
        }

        long randomSeed = seedSupplier.getAsLong();

        ShootoutSimulator simulator = new ShootoutSimulator(
                cowboyCount,
                new SeededShootoutRandom(randomSeed)
        );

        output.printf(
                "Starting shootout with %d cowboy(s).%n",
                cowboyCount
        );
        output.printf(
                "Starting cowboy: %d%n",
                simulator.startingCowboyNumber()
        );
        output.printf("Random seed: %d%n", randomSeed);

        ShootoutResult result;
        String checksum;

        try {
            try (ProtocolWriter protocolWriter =
                         new ProtocolWriter(
                                 protocolPath,
                                 cowboyCount,
                                 simulator.startingCowboyNumber(),
                                 randomSeed
                         )) {
                while (!simulator.isFinished()) {
                    ShotEvent shot = simulator.nextShot();
                    protocolWriter.writeShot(shot);
                    printShot(output, shot);
                }

                result = simulator.result();
                protocolWriter.finish(result);
            }

            checksum =
                    ChecksumCalculator.calculateSha256(protocolPath);
        } catch (IOException exception) {
            error.println(
                    "Could not write or verify the protocol file: "
                            + exception.getMessage()
            );
            return APPLICATION_ERROR_EXIT_CODE;
        }

        output.printf(
                "Winner: Cowboy %d with %d HP after %d shot(s).%n",
                result.winnerNumber(),
                result.winnerHealthPoints(),
                result.totalShots()
        );
        output.println(
                "Protocol: "
                        + protocolPath.toAbsolutePath().normalize()
        );
        output.println("SHA-256: " + checksum);

        return SUCCESS_EXIT_CODE;
    }

    private static int parseCowboyCount(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                    "Exactly one cowboy-count argument is required."
            );
        }

        int cowboyCount;

        try {
            cowboyCount = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "The cowboy count must be a whole number.",
                    exception
            );
        }

        if (cowboyCount <= 0) {
            throw new IllegalArgumentException(
                    "The cowboy count must be positive."
            );
        }

        return cowboyCount;
    }

    private static void printShot(
            PrintStream output,
            ShotEvent shot
    ) {
        output.printf(
                "Shot %d: Cowboy %d fired %s and hit Cowboy %d "
                        + "for %d HP; target HP: %d%s%n",
                shot.shotNumber(),
                shot.shooterNumber(),
                shot.direction(),
                shot.targetNumber(),
                shot.damage(),
                shot.targetHealthPointsAfter(),
                shot.targetKilled() ? " (killed)" : ""
        );
    }

    private static void printUsage(PrintStream output) {
        output.println(
                "Usage: java -jar cowboy-shootout.jar "
                        + "<number-of-cowboys>"
        );
    }
}
