package ui.pages.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ui.core.config.SeleniumConfig;
import ui.core.driver.DriverFactory;
import ui.core.wait.WaitUtils;

import java.util.Optional;

public class BasePageObject {
    protected WebDriver driver;
    protected WaitUtils wait;
    protected Logger log;
    //protected String environmentURL;

    SeleniumConfig config = SeleniumConfig.getInstance();
    //String environmentURL = config.getEnvironmentURL();

    public BasePageObject() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WaitUtils();
        this.log = LogManager.getLogger(this.getClass());
        //this.environmentURL = config.getEnvironmentURL();
    }

    /** Open page with given URL */
    public void openUrl(String url) { driver.get(url); }

    /** Find element using given locator */
    public WebElement find(By locator) {
        return driver.findElement(locator);
    }

    /** Click on element with given locator when it's visible - By */
    public void click(By locator) {
        wait.waitForElementToBeClickable(locator);
        find(locator).click();
    }

    /** Click on element with given locator when it's visible - WebElement */
    public void click(WebElement element) {
        wait.waitForElementToBeClickable(element);
        element.click();
    }

    /** Check if element exist in HTML */
    public boolean elementExist(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    /** Check for element value in DOM structure */
    public String elementValue(By locator) {
        WebElement element = find(locator);
        return element.getDomProperty("value");
    }

    /** Check for element text */
    // e.g. Span elements doesn't have value property in DOM, but it can be obtained by getText()
    public String elementText(By locator) {
        WebElement element = find(locator);
        return element.getText();
    }

    /** Check if element is enabled in HTML */
    public boolean elementEnabled(By locator) { return find(locator).isEnabled(); }

    /** Check if element is displayed and enabled in HTML */
    public boolean isElementClickable(By locator) {
        wait.waitForElementToBeVisible(locator);
        WebElement element = find(locator);
        return element.isDisplayed() && element.isEnabled(); }

    /** Type given text into element with given locator */
    public void type(String text, By locator) {
        find(locator).sendKeys(text);
    }

    /** Get URL of current page from browser */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /** Provide element DOM property */
    public @Nullable String getElementDOMProperty(By locator, String property) {
        return Optional.ofNullable(find(locator)).map(e -> e.getDomProperty(property)).orElse(null);
    }

    /** Get Page Title of current page from browser */
    public String getPageTitle(By locator) {
        wait.waitForElementPresence(locator);
        return find(locator).getText().trim();
    }

    /** Check for element readOnly DOM property */
    public @Nullable String elementReadOnly(By locator) {
        return find(locator).getDomAttribute("readOnly");
    }

    /** Safe Send Keys to the element */
    public void safeSendKeys(By locator, CharSequence... keys) {
        wait.waitForElementToBeVisible(locator);
        WebElement element = find(locator);

        // Clean field only in case if first parametar is String
        if (keys.length == 1 && keys[0] instanceof String) {
            element.clear();
        }

        element.sendKeys(keys);

        // Recovery scenario: Check if element value
        if (keys.length == 1 && keys[0] instanceof String) {
            String text = (String) keys[0];
            if (!text.equals(getElementDOMProperty(locator, "value"))) {
                element.clear();
                element.sendKeys(keys);
            }
        }
    }
}
