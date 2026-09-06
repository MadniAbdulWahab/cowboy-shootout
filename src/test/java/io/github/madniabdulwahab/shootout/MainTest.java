package io.github.madniabdulwahab.shootout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.madniabdulwahab.shootout.protocol.ChecksumCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper();

    @TempDir
    Path temporaryDirectory;

    @Test
    void runsShootoutAndProducesProtocolAndChecksum()
            throws IOException {
        Path protocolPath =
                temporaryDirectory.resolve("protocol.json");

        ByteArrayOutputStream outputBytes =
                new ByteArrayOutputStream();
        ByteArrayOutputStream errorBytes =
                new ByteArrayOutputStream();

        int exitCode;

        try (
                PrintStream output = new PrintStream(
                        outputBytes,
                        true,
                        StandardCharsets.UTF_8
                );
                PrintStream error = new PrintStream(
                        errorBytes,
                        true,
                        StandardCharsets.UTF_8
                )
        ) {
            exitCode = Main.run(
                    new String[]{"2"},
                    output,
                    error,
                    protocolPath,
                    () -> 42L
            );
        }

        String consoleOutput =
                outputBytes.toString(StandardCharsets.UTF_8);
        String errorOutput =
                errorBytes.toString(StandardCharsets.UTF_8);

        JsonNode protocol =
                OBJECT_MAPPER.readTree(
                        Files.readString(
                                protocolPath,
                                StandardCharsets.UTF_8
                        )
                );

        String checksum =
                ChecksumCalculator.calculateSha256(protocolPath);

        assertAll(
                () -> assertEquals(0, exitCode),
                () -> assertTrue(Files.exists(protocolPath)),
                () -> assertEquals("", errorOutput),
                () -> assertEquals(
                        2,
                        protocol.get("cowboyCount").asInt()
                ),
                () -> assertEquals(
                        42L,
                        protocol.get("randomSeed").asLong()
                ),
                () -> assertTrue(
                        protocol.get("shots").size() > 0
                ),
                () -> assertTrue(
                        protocol.has("result")
                ),
                () -> assertTrue(
                        consoleOutput.contains("Shot 1:")
                ),
                () -> assertTrue(
                        consoleOutput.contains("Winner: Cowboy")
                ),
                () -> assertTrue(
                        consoleOutput.contains(
                                "SHA-256: " + checksum
                        )
                )
        );
    }

    @Test
    void rejectsInvalidCommandLineArguments() {
        Path protocolPath =
                temporaryDirectory.resolve("invalid.json");

        ByteArrayOutputStream errorBytes =
                new ByteArrayOutputStream();

        PrintStream ignoredOutput = new PrintStream(
                OutputStream.nullOutputStream()
        );
        PrintStream errorOutput = new PrintStream(errorBytes);

        int missingArgumentExitCode = Main.run(
                new String[]{},
                ignoredOutput,
                errorOutput,
                protocolPath,
                () -> 1L
        );

        int nonNumericExitCode = Main.run(
                new String[]{"two"},
                ignoredOutput,
                errorOutput,
                protocolPath,
                () -> 1L
        );

        int zeroExitCode = Main.run(
                new String[]{"0"},
                ignoredOutput,
                errorOutput,
                protocolPath,
                () -> 1L
        );

        assertAll(
                () -> assertEquals(2, missingArgumentExitCode),
                () -> assertEquals(2, nonNumericExitCode),
                () -> assertEquals(2, zeroExitCode),
                () -> assertFalse(Files.exists(protocolPath)),
                () -> assertTrue(
                        errorBytes
                                .toString(StandardCharsets.UTF_8)
                                .contains("Usage:")
                )
        );
    }
}
