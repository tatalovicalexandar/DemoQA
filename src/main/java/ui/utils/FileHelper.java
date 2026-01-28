package ui.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;

public class FileHelper {
    // Create a temporary file with given name and size in bytes inside the given directory.
    public static Path createTempFileWithSize(Path dir, String filename, long sizeBytes) throws IOException {
        if (!Files.exists(dir)) Files.createDirectories(dir);
        Path file = dir.resolve(filename);
        try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            byte[] buf = new byte[8192];
            long written = 0;
            while (written < sizeBytes) {
                int toWrite = (int) Math.min(buf.length, sizeBytes - written);
                out.write(buf, 0, toWrite);
                written += toWrite;
            }
        }
        return file;
    }

    // Compute SHA-256 digest (hex) for the file.
    public static String sha256(Path file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(file);
             DigestInputStream dis = new DigestInputStream(is, md)) {
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) { /* digest updated */ }
        }
        byte[] digest = md.digest();
        return HexFormat.of().formatHex(digest);
    }

    // Wait until a file matching the given filename (or prefix/suffix if needed) appears in dir.
    public static Path waitForFile(Path dir, String expectedFilename, Duration timeout) throws InterruptedException {
        long end = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < end) {
            Path candidate = dir.resolve(expectedFilename);
            if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                return candidate;
            }
            // Support partial matches: sometimes browser appends (1) etc.
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                for (Path p : ds) {
                    if (p.getFileName().toString().startsWith(expectedFilename) || p.getFileName().toString().contains(expectedFilename)) {
                        if (Files.isRegularFile(p)) return p;
                    }
                }
            } catch (IOException ignored) {}
            Thread.sleep(250);
        }
        return null;
    }

    public static void cleanDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            for (Path p : ds) {
                Files.deleteIfExists(p);
            }
        }
    }
}
