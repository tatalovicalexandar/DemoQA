package ui.core.config;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SeleniumConfig {
    private static SeleniumConfig instance;

    private SeleniumConfig() { }

    // Public method to get the single instance of the class
    public static SeleniumConfig getInstance() {
        if (instance == null) {
            instance = new SeleniumConfig();
        }
        return instance;
    }

    // Read configuration from XML file
    public static String configFile = "selenium-config.xml";
    public String getBrowserName() {
        return getConfigValue(configFile, "browserName");
    }
    public Boolean getHeadlessDriver() {
        return Boolean.valueOf(getConfigValue(configFile, "headlessDriver"));
    }
    public String getEnvironmentURL() {
        return getConfigValue(configFile, "environmentURL");
    }
    public String getDownloadPath() { return getConfigValue(configFile, "downloadPath"); }
    public String getTimeout() {
        return getConfigValue(configFile, "timeout");
    }
    public Boolean getUseWebDriverManager() {
        return Boolean.valueOf(getConfigValue(configFile, "useWebDriverManager"));
    }
    private static String getConfigValue(String configFile, String propertyName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            InputStream is = SeleniumConfig.class.getClassLoader().getResourceAsStream(configFile);
            if (is == null) {
                throw new RuntimeException(configFile + " not found in classpath");
            }
            Document document = builder.parse(is);

            return document.getElementsByTagName(propertyName).item(0).getTextContent();

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Path getDownloadDir() {
        // Base path from XML
        String base = getDownloadPath();

        if (base == null || base.isBlank()) {
            throw new RuntimeException("downloadPath is not defined in selenium-config.xml");
        }

        Path path = Paths.get(
                System.getProperty("user.dir"),
                base,
                String.valueOf(Thread.currentThread().getId())
        );

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create download directory: " + path, e);
        }

        return path.toAbsolutePath();
    }
}
