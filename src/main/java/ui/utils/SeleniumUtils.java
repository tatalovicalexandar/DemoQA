package ui.utils;

import org.testng.annotations.*;
import ui.core.config.SeleniumConfig;
import ui.core.driver.DriverFactory;
import ui.core.wait.WaitUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import java.io.IOException;
import java.nio.file.Path;

public class SeleniumUtils {
    protected WebDriver driver;
    protected WaitUtils waitUtils;
    protected String testName;
    protected Logger log;

    @Parameters({ "browser" })
    @BeforeMethod(alwaysRun=true)
    public void setUp(@Optional("firefox") String browser, ITestContext ctx) throws IOException {
        testName = ctx.getCurrentXmlTest().getName();
        //this.browserName = config.getBrowserName();
        //this.browserName = ctx.getCurrentXmlTest().getParameter("browser");
        //this.browserName = (browser != null && !browser.isEmpty()) ? browser : "firefox";
        log = LogManager.getLogger(testName);

        DriverFactory factory = new DriverFactory(browser);
        factory.createDriver();
        driver = factory.getDriver();

        // Initialize WaitUtils with the driver
        waitUtils = new WaitUtils();

        cleanupAllDownloads();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownAfterEach() throws IOException {
        Path downloadDir = SeleniumConfig.getInstance().getDownloadDir();
        FileHelper.cleanDirectory(downloadDir);

        // Check if the driver exists before attempting to quit it
        if (DriverFactory.getDriver() != null) {
            DriverFactory.quitDriver();
        } else {
            log.warn("Driver was already closed or never initialized.");
        }
    }

    public void cleanupAllDownloads() throws IOException {
        Path base = SeleniumConfig.getInstance().getDownloadDir();
        FileHelper.cleanDirectory(base);
    }

    public String getCurrentTestNameFromXml() {
        return this.testName;
    }
}
