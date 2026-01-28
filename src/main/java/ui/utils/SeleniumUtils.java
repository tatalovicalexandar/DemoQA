package ui.utils;

import ui.core.driver.DriverFactory;
import ui.core.wait.WaitUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

public class SeleniumUtils {
    protected WebDriver driver;
    protected WaitUtils waitUtils;
    protected String testName;
    protected Logger log;

    @Parameters({ "browser" })
    @BeforeMethod(alwaysRun=true)
    public void setUp(@Optional("firefox") String browser, ITestContext ctx){
        testName = ctx.getCurrentXmlTest().getName();
        //this.browserName = config.getBrowserName();
        //this.browserName = ctx.getCurrentXmlTest().getParameter("browser");
        //this.browserName = (browser != null && !browser.isEmpty()) ? browser : "firefox";
        log = LogManager.getLogger(testName);
        System.out.println("Test name: " + testName);
        //System.out.println("Browser: " + this.browserName);

        DriverFactory factory = new DriverFactory(browser);
        factory.createDriver();
        driver = factory.getDriver();

        // Initialize WaitUtils with the driver
        waitUtils = new WaitUtils();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownAfterEach() {
        // Check if the driver exists before attempting to quit it
        if (DriverFactory.getDriver() != null) {
            DriverFactory.quitDriver();
        } else {
            log.warn("Driver was already closed or never initialized.");
        }
    }

    public String getCurrentTestNameFromXml() {
        return this.testName;
    }
}
