package io.github.madniabdulwahab.shootout.protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.github.madniabdulwahab.shootout.domain.ShootoutResult;
import io.github.madniabdulwahab.shootout.domain.ShootoutRules;
import io.github.madniabdulwahab.shootout.domain.ShotEvent;

/**
 * Writes a shootout protocol as streaming UTF-8 JSON.
 */
public final class ProtocolWriter implements AutoCloseable {

    private static final int PROTOCOL_VERSION = 1;
    private static final JsonFactory JSON_FACTORY =
            JsonFactory.builder().build();

    private final JsonGenerator generator;

    private boolean finished;
    private boolean closed;

    public ProtocolWriter(
            Path outputPath,
            int cowboyCount,
            int startingCowboyNumber,
            long randomSeed
    ) throws IOException {
        Objects.requireNonNull(outputPath, "outputPath must not be null");

        if (cowboyCount <= 0) {
            throw new IllegalArgumentException(
                    "The number of cowboys must be positive."
            );
        }

        if (startingCowboyNumber <= 0
                || startingCowboyNumber > cowboyCount) {
            throw new IllegalArgumentException(
                    "The starting cowboy number must be between 1 and "
                            + cowboyCount
                            + "."
            );
        }

        OutputStream outputStream = Files.newOutputStream(
                outputPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );

        generator = JSON_FACTORY.createGenerator(
                outputStream,
                JsonEncoding.UTF8
        );

        generator.useDefaultPrettyPrinter();
        writeHeader(cowboyCount, startingCowboyNumber, randomSeed);
    }

    public void writeShot(ShotEvent shot) throws IOException {
        Objects.requireNonNull(shot, "shot must not be null");
        ensureWritable();

        generator.writeStartObject();
        generator.writeNumberField("shotNumber", shot.shotNumber());
        generator.writeNumberField(
                "shooterNumber",
                shot.shooterNumber()
        );
        generator.writeNumberField(
                "shooterHealthPoints",
                shot.shooterHealthPoints()
        );
        generator.writeStringField(
                "direction",
                shot.direction().name()
        );
        generator.writeNumberField(
                "targetNumber",
                shot.targetNumber()
        );
        generator.writeNumberField("damage", shot.damage());
        generator.writeNumberField(
                "targetHealthPointsAfter",
                shot.targetHealthPointsAfter()
        );
        generator.writeBooleanField(
                "targetKilled",
                shot.targetKilled()
        );
        generator.writeEndObject();
    }

    public void finish(ShootoutResult result) throws IOException {
        Objects.requireNonNull(result, "result must not be null");
        ensureWritable();

        generator.writeEndArray();

        generator.writeObjectFieldStart("result");
        generator.writeNumberField(
                "winnerNumber",
                result.winnerNumber()
        );
        generator.writeNumberField(
                "winnerHealthPoints",
                result.winnerHealthPoints()
        );
        generator.writeNumberField(
                "totalShots",
                result.totalShots()
        );
        generator.writeEndObject();

        generator.writeEndObject();
        generator.flush();

        finished = true;
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            generator.close();
            closed = true;
        }
    }

    private void writeHeader(
            int cowboyCount,
            int startingCowboyNumber,
            long randomSeed
    ) throws IOException {
        generator.writeStartObject();
        generator.writeNumberField(
                "protocolVersion",
                PROTOCOL_VERSION
        );
        generator.writeNumberField("cowboyCount", cowboyCount);
        generator.writeNumberField(
                "initialHealthPoints",
                ShootoutRules.INITIAL_HEALTH_POINTS
        );
        generator.writeNumberField(
                "startingCowboyNumber",
                startingCowboyNumber
        );
        generator.writeNumberField("randomSeed", randomSeed);
        generator.writeArrayFieldStart("shots");
    }

    private void ensureWritable() {
        if (closed) {
            throw new IllegalStateException(
                    "The protocol writer is already closed."
            );
        }

        if (finished) {
            throw new IllegalStateException(
                    "The protocol has already been completed."
            );
        }
    }
}
