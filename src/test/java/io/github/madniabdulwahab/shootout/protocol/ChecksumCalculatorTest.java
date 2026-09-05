package io.github.madniabdulwahab.shootout.protocol;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChecksumCalculatorTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void calculatesKnownSha256Checksum() throws IOException {
        Path filePath = temporaryDirectory.resolve("abc.txt");

        Files.writeString(
                filePath,
                "abc",
                StandardCharsets.UTF_8
        );

        String checksum =
                ChecksumCalculator.calculateSha256(filePath);

        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223"
                        + "b00361a396177a9cb410ff61f20015ad",
                checksum
        );
    }

    @Test
    void calculatesChecksumOfEmptyFile() throws IOException {
        Path filePath = temporaryDirectory.resolve("empty.txt");
        Files.createFile(filePath);

        String checksum =
                ChecksumCalculator.calculateSha256(filePath);

        assertEquals(
                "e3b0c44298fc1c149afbf4c8996fb924"
                        + "27ae41e4649b934ca495991b7852b855",
                checksum
        );
    }
}
