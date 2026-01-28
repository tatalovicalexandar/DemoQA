package ui.uploaddownload;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ui.core.config.SeleniumConfig;
import ui.pages.uploaddownload.UploadDownloadPage;
import ui.utils.FileHelper;
import ui.utils.SeleniumUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

public class UploadDownloadTests extends SeleniumUtils {
    private UploadDownloadPage page;
    private Path downloadDir;
    private SeleniumConfig config = SeleniumConfig.getInstance();

    private void preconditions() throws IOException {
        page = new UploadDownloadPage(driver);
        page.openUrl(config.getEnvironmentURL() + "/upload-download");
        page.waitUntilReady();
        downloadDir = config.getDownloadDir();
    }

    @Test(description = "Download anchor should save a file to disk and checksum must match expected")
    public void downloadFile_success_andVerifyChecksum() throws Exception {
        preconditions();

        // If you know expected filename and checksum in advance, set them here.
        // From the HTML fragment download attribute is "sampleFile.jpeg"
        String expectedFilename = "sampleFile.jpeg";
        String expectedFileChecksum = "04ec95ca2b997689dcc4d66c05063d1d3f2ef2a10b8b1f153c9db4bcde823852";

        // If you have a reference checksum (for demo we will not have it), compute it here.
        // For demo purposes we'll verify that the file appears and has non-zero size.
        page.clickDownload();

        Path downloaded = FileHelper.waitForFile(downloadDir, expectedFilename, Duration.ofSeconds(10));
        Assert.assertNotNull(downloaded, "Downloaded file should appear in download folder within timeout");

        // Wait until file is not empty
        int attempts = 0;
        while (java.nio.file.Files.size(downloaded) == 0 && attempts < 10) {
            Thread.sleep(500);
            attempts++;
        }

        long size = java.nio.file.Files.size(downloaded);
        Assert.assertTrue(size > 0, "Downloaded file should not be empty on the path: " + downloaded.toString());

        // Optional: compute checksum for integrity
        String sha256 = FileHelper.sha256(downloaded);
        Assert.assertNotNull(sha256);

        Assert.assertEquals(sha256, expectedFileChecksum, "Downloaded file checksum should match expected value");

        // Log checksum for investigation (in real project use logger)
        System.out.println("Downloaded file sha256: " + sha256);
    }

    @Test(description = "Upload small text file should be accepted by input element and the UI should reflect filename")
    public void uploadFile_success() throws Exception {
        preconditions();

        // Create a small temporary file to upload
        Path tmp = FileHelper.createTempFileWithSize(downloadDir, "upload-test.txt", 1024);
        page.uploadFile(tmp.toAbsolutePath().toString());

        // Verify via JS that the input reports the uploaded file name
        String uploaded = page.getUploadedFileName();
        Assert.assertNotNull(uploaded, "Uploaded filename should be available via JS");
        Assert.assertEquals(uploaded, tmp.getFileName().toString(), "Uploaded file name should match the local file name");
    }

    @Test(description = "Upload with invalid path should result in WebDriver exception (negative)")
    public void upload_invalidPath_throws() throws IOException {
        preconditions();

        String invalidPath = downloadDir.resolve("does-not-exist-12345.bin").toAbsolutePath().toString();
        boolean threw = false;
        try {
            page.uploadFile(invalidPath);
        } catch (Exception e) {
            // WebDriver typically throws an exception when the path does not exist or is not accessible
            threw = true;
            System.out.println("Expected exception for invalid upload path: " + e.getMessage());
        }
        Assert.assertTrue(threw, "Uploading a non-existent file should throw an exception (client-side failure)");
    }

    @Test(description = "Download + upload roundtrip: download file then re-upload it and verify name")
    public void download_thenUpload_roundtrip() throws Exception {
        preconditions();

        String expectedFilename = "sampleFile.jpeg";
        page.clickDownload();
        Path downloaded = FileHelper.waitForFile(downloadDir, expectedFilename, Duration.ofSeconds(10));
        Assert.assertNotNull(downloaded, "Downloaded file should appear");

        // Re-upload the same file
        page.uploadFile(downloaded.toAbsolutePath().toString());
        String uploaded = page.getUploadedFileName();
        Assert.assertNotNull(uploaded);
        Assert.assertTrue(uploaded.toLowerCase().contains("samplefile"), "Uploaded name should include original file name");
    }

    @Test(description = "Large file upload behavior (creates a 10MB file then uploads) - observe performance/timeout")
    public void upload_largeFile_behavior() throws Exception {
        preconditions();

        // Create ~10MB file
        Path large = FileHelper.createTempFileWithSize(downloadDir, "large-upload.bin", 10 * 1024 * 1024);
        long start = System.currentTimeMillis();
        page.uploadFile(large.toAbsolutePath().toString());
        long elapsed = System.currentTimeMillis() - start;

        // Check input accepted the file
        String uploaded = page.getUploadedFileName();
        Assert.assertNotNull(uploaded, "Large file should be accepted by input element (browser-client)");
        Assert.assertEquals(uploaded, large.getFileName().toString());

        // Simple perf assertion: uploading should complete within a reasonable time on CI agent
        Assert.assertTrue(elapsed < 120_000, "Large file upload operation should not hang (elapsed ms: " + elapsed + ")");
    }
}
