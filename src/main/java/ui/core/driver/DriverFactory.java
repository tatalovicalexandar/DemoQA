package ui.core.driver;


import ui.core.config.SeleniumConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class DriverFactory {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final Logger log = LogManager.getLogger(DriverFactory.class);
    private final String browser;
    SeleniumConfig config = SeleniumConfig.getInstance();

    public DriverFactory(String browser) {
        this.browser = (browser == null || browser.isEmpty()) ? config.getBrowserName() : browser.toLowerCase();
    }

    public WebDriver createDriver() {
        log.info("Initializing driver for browser: {}", browser);

        WebDriver createdDriver = switch (browser) {
            case "chrome" -> new ChromeDriver(setupChromeOptions());
            case "firefox" -> new FirefoxDriver(setupFirefoxOptions());
            case "edge" -> new EdgeDriver(setupEdgeOptions());
            default -> {
                log.warn("Unknown browser: {}, defaulting to Chrome", browser);
                yield new ChromeDriver(setupChromeOptions());
            }
        };

        driver.set(createdDriver);
        driver.get().manage().window().maximize();
        log.info("Browser {} driver initialized successfully", browser);
        return driver.get();
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
            log.info("Driver closed successfully.");
        }
    }

    // --------------------------------------- PRIVATE HELPERS ----------------------------------------

    private ChromeOptions setupChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        setupDriver(options);
        return options;
    }

    private FirefoxOptions setupFirefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--width=1920", "--height=1080");
        options.addPreference("dom.webnotifications.enabled", false);
        setupDriver(options);
        return options;
    }

    private EdgeOptions setupEdgeOptions() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("start-maximized", "--disable-notifications");
        setupDriver(options);
        return options;
    }

    // Centralized method for all options and headless setup.
    private <T> void setupDriver(T options) {
        // Setup WebDriverManager or system driver
        if (options instanceof ChromeOptions) setupDriverBinary("chromedriver", WebDriverManager.chromedriver());
        else if (options instanceof FirefoxOptions) setupDriverBinary("geckodriver", WebDriverManager.firefoxdriver());
        else if (options instanceof EdgeOptions) setupDriverBinary("msedgedriver", WebDriverManager.edgedriver());

        // Headless configuration
        configureHeadless(options);
    }

    private void setupDriverBinary(String systemProperty, WebDriverManager manager) {
        if (config.getUseWebDriverManager()) {
            manager.setup();
        } else {
            System.setProperty("webdriver." + systemProperty + ".driver", "src/main/resources/" + systemProperty + ".exe");
        }
    }

    private void configureHeadless(Object options) {
        if (!config.getHeadlessDriver()) return;

        if (options instanceof ChromeOptions chrome) {
            chrome.addArguments("--headless", "--window-size=1920,1080");
        } else if (options instanceof FirefoxOptions firefox) {
            firefox.addArguments("--headless", "--width=1920", "--height=1080");
        } else if (options instanceof EdgeOptions edge) {
            edge.addArguments("--headless", "--window-size=1920,1080");
        }

        log.info("Browser {} will run in headless mode", browser);
    }
}
