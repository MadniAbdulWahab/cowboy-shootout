package io.github.madniabdulwahab.shootout.protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Calculates cryptographic checksums for protocol files.
 */
public final class ChecksumCalculator {

    private static final String SHA_256 = "SHA-256";

    private ChecksumCalculator() {
    }

    public static String calculateSha256(Path filePath)
            throws IOException {
        Objects.requireNonNull(
                filePath,
                "filePath must not be null"
        );

        MessageDigest messageDigest = createSha256Digest();

        try (DigestInputStream inputStream =
                     new DigestInputStream(
                             Files.newInputStream(filePath),
                             messageDigest
                     )) {
            inputStream.transferTo(OutputStream.nullOutputStream());
        }

        return HexFormat.of().formatHex(messageDigest.digest());
    }

    private static MessageDigest createSha256Digest() {
        try {
            return MessageDigest.getInstance(SHA_256);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not supported by this Java runtime.",
                    exception
            );
        }
    }
}
