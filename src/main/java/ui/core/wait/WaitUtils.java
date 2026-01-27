package ui.core.wait;

import ui.core.config.SeleniumConfig;
import ui.core.driver.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class WaitUtils {
    private final WebDriverWait waitUtils;

    // Constructor to initialize the WebDriver and WebDriverWait
    public WaitUtils() {
        WebDriver driver = DriverFactory.getDriver();
        int timeout = Integer.parseInt(SeleniumConfig.getInstance().getTimeout());
        this.waitUtils = new WebDriverWait(driver, Duration.ofSeconds(timeout));
    }

    // Common wait method for element to be clickable - By
    public void waitForElementToBeClickable(By locator) {
        waitUtils.until(ExpectedConditions.elementToBeClickable(locator));
    }

    // Common wait method for element to be clickable - WebElement
    public void waitForElementToBeClickable(WebElement element) {
        waitUtils.until(ExpectedConditions.elementToBeClickable(element));
    }

    // Common wait method for presence of element
    public void waitForElementPresence(By locator) {
        waitUtils.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    // Function to implement dynamic wait for URL
    public void waitForUrl(String partialUrl) {
        waitUtils.until(ExpectedConditions.urlContains(partialUrl));
    }

    // Common wait method for element to became visible
    public void waitForElementToBeVisible(By locator) {
        waitUtils.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // Common wait method for element to became invisible
    public void waitForElementToBeInvisible(By locator) {
        waitUtils.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public void waitForAttributeToBe(By locator, String text) {
        waitUtils.until(ExpectedConditions.attributeToBe(locator, "value", text));
    }
}
