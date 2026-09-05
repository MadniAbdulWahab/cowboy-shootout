package io.github.madniabdulwahab.shootout.protocol;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.madniabdulwahab.shootout.domain.Direction;
import io.github.madniabdulwahab.shootout.domain.ShootoutResult;
import io.github.madniabdulwahab.shootout.domain.ShotEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtocolWriterTest {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper();

    @TempDir
    Path temporaryDirectory;

    @Test
    void writesACompleteValidUtf8JsonProtocol() throws IOException {
        Path outputPath =
                temporaryDirectory.resolve("protocol.json");

        try (ProtocolWriter writer =
                     new ProtocolWriter(outputPath, 2, 1, 42L)) {
            writer.writeShot(
                    new ShotEvent(
                            1, 1, 10, Direction.RIGHT,
                            2, 5, 5, false
                    )
            );
            writer.writeShot(
                    new ShotEvent(
                            2, 2, 5, Direction.LEFT,
                            1, 5, 5, false
                    )
            );
            writer.writeShot(
                    new ShotEvent(
                            3, 1, 5, Direction.LEFT,
                            2, 5, 0, true
                    )
            );
            writer.finish(new ShootoutResult(1, 5, 3));
        }

        String json = Files.readString(
                outputPath,
                StandardCharsets.UTF_8
        );
        JsonNode root = OBJECT_MAPPER.readTree(json);

        assertAll(
                () -> assertEquals(
                        1,
                        root.get("protocolVersion").asInt()
                ),
                () -> assertEquals(
                        2,
                        root.get("cowboyCount").asInt()
                ),
                () -> assertEquals(
                        10,
                        root.get("initialHealthPoints").asInt()
                ),
                () -> assertEquals(
                        1,
                        root.get("startingCowboyNumber").asInt()
                ),
                () -> assertEquals(
                        42L,
                        root.get("randomSeed").asLong()
                ),
                () -> assertTrue(root.get("shots").isArray()),
                () -> assertEquals(
                        3,
                        root.get("shots").size()
                )
        );

        JsonNode firstShot = root.get("shots").get(0);

        assertAll(
                () -> assertEquals(
                        1,
                        firstShot.get("shooterNumber").asInt()
                ),
                () -> assertEquals(
                        2,
                        firstShot.get("targetNumber").asInt()
                ),
                () -> assertEquals(
                        5,
                        firstShot.get("damage").asInt()
                ),
                () -> assertEquals(
                        5,
                        firstShot
                                .get("targetHealthPointsAfter")
                                .asInt()
                )
        );

        JsonNode result = root.get("result");

        assertAll(
                () -> assertEquals(
                        1,
                        result.get("winnerNumber").asInt()
                ),
                () -> assertEquals(
                        5,
                        result.get("winnerHealthPoints").asInt()
                ),
                () -> assertEquals(
                        3,
                        result.get("totalShots").asLong()
                )
        );
    }

    @Test
    void preventsWritingAfterProtocolIsFinished()
            throws IOException {
        Path outputPath =
                temporaryDirectory.resolve("finished.json");

        ShotEvent shot = new ShotEvent(
                1, 1, 10, Direction.RIGHT,
                2, 1, 9, false
        );

        try (ProtocolWriter writer =
                     new ProtocolWriter(outputPath, 2, 1, 1L)) {
            writer.finish(new ShootoutResult(1, 10, 0));

            assertAll(
                    () -> assertThrows(
                            IllegalStateException.class,
                            () -> writer.writeShot(shot)
                    ),
                    () -> assertThrows(
                            IllegalStateException.class,
                            () -> writer.finish(
                                    new ShootoutResult(1, 10, 0)
                            )
                    )
            );
        }
    }

    @Test
    void rejectsInvalidProtocolMetadata() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ProtocolWriter(
                                temporaryDirectory.resolve("zero.json"),
                                0,
                                1,
                                1L
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new ProtocolWriter(
                                temporaryDirectory.resolve("start.json"),
                                3,
                                4,
                                1L
                        )
                )
        );
    }
}
