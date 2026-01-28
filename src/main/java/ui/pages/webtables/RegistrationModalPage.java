package ui.pages.webtables;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ui.pages.common.BasePageObject;
import org.openqa.selenium.By;
import java.util.NoSuchElementException;

public class RegistrationModalPage extends BasePageObject {
    private final By modalRoot = By.cssSelector("div.modal-dialog.modal-lg");
    private final By modalContent = By.cssSelector("div.modal-content");
    private final By closeButton = By.cssSelector("button.close");
    private final By submitButton = By.cssSelector("button#submit");

    // Field locators by id
    public final By firstNameInput = By.id("firstName");
    public final By lastNameInput = By.id("lastName");
    public final By emailInput = By.id("userEmail");
    public final By ageInput = By.id("age");
    public final By salaryInput = By.id("salary");
    public final By departmentInput = By.id("department");

    public RegistrationModalPage(WebDriver driver) {
        super();
    }

    public void waitForVisible() {
        wait.waitForElementToBeVisible(modalRoot);
        wait.waitForElementToBeVisible(modalContent);
    }

    public boolean isDisplayed() {
        try {
            return find(modalRoot).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void close() {
        click(closeButton);
        wait.waitForElementToBeInvisible(modalRoot);
    }

    public void submit() {
        click(submitButton);
    }

    public void setFirstName(String value) {
        safeSendKeys(firstNameInput, value);
    }

    public void setLastName(String value) {
        safeSendKeys(lastNameInput, value);
    }

    public void setEmail(String value) {
        safeSendKeys(emailInput, value);
    }

    public void setAge(String value) {
        safeSendKeys(ageInput, value);
    }

    public void setSalary(String value) {
        safeSendKeys(salaryInput, value);
    }

    public void setDepartment(String value) {
        safeSendKeys(departmentInput, value);
    }

    public String getPlaceholder(By inputLocator) {
        try {
            return find(inputLocator).getAttribute("placeholder");
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public String getAttribute(By inputLocator, String attribute) {
        try {
            return find(inputLocator).getAttribute(attribute);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Vraća HTML5 validationMessage za polje; ako je polje validno vraća prazan string.
     * Radi tako što poziva checkValidity() i vraća validationMessage.
     */
    public String getFieldValidationMessage(By inputLocator) {
        WebElement element = find(inputLocator);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Ako validno -> checkValidity() == true -> return empty string
        Boolean valid = (Boolean) js.executeScript("return arguments[0].checkValidity();", element);

        if (valid != null && valid) return "";
        Object msg = js.executeScript("return arguments[0].validationMessage;", element);
        return msg == null ? "" : msg.toString();
    }

    // Convenience getters for validationMessage per field
    public String firstNameValidationMessage() { return getFieldValidationMessage(firstNameInput); }
    public String lastNameValidationMessage() { return getFieldValidationMessage(lastNameInput); }
    public String emailValidationMessage() { return getFieldValidationMessage(emailInput); }
    public String ageValidationMessage() { return getFieldValidationMessage(ageInput); }
    public String salaryValidationMessage() { return getFieldValidationMessage(salaryInput); }
    public String departmentValidationMessage() { return getFieldValidationMessage(departmentInput); }

    // Fill the Registration Form without submitting
    public void fillForm(String firstName, String lastName, String email, String age, String salary, String department) {
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setAge(age);
        setSalary(salary);
        setDepartment(department);
    }

    // Wait until modal is not visible (after successful submit)
    public void waitForNotVisible() {
        wait.waitForElementToBeInvisible(modalRoot);
    }
}
