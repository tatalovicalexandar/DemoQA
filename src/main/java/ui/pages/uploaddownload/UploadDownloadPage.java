package ui.pages.uploaddownload;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import ui.pages.common.BasePageObject;

public class UploadDownloadPage extends BasePageObject {
    private final By downloadAnchor = By.cssSelector("a#downloadButton");
    private final By uploadInput = By.cssSelector("input#uploadFile");
    private final By pageRoot = By.cssSelector("h1.text-center");

    public UploadDownloadPage(WebDriver driver) {
        super();
    }

    public void waitUntilReady() {
        wait.waitForElementToBeVisible(pageRoot);
    }

    // Clicks the download anchor which has a download attribute.
    public void clickDownload() {
        click(downloadAnchor);
    }

    // Upload a file using the native <input type="file"> element by sending absolute path.
    public void uploadFile(String absoluteFilePath) {
        safeSendKeys(uploadInput, absoluteFilePath);
    }

    // Returns the name of the first selected file in the file input using JS:
    public String getUploadedFileName() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object res = js.executeScript(
                "var el = document.getElementById('uploadFile'); if (!el || !el.files || el.files.length===0) return null; return el.files[0].name;");
        return res == null ? null : res.toString();
    }

    // Returns raw value attribute of input (browser-dependent, often fake path).
    public String getUploadInputValue() {
        return elementValue(uploadInput);
    }

}
