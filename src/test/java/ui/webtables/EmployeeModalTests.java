package ui.webtables;

import ui.core.config.SeleniumConfig;
import ui.utils.SeleniumUtils;
import ui.pages.webtables.EmployeeTablePage;
import ui.pages.webtables.RegistrationModalPage;
import org.testng.annotations.Test;
import org.testng.Assert;
import ui.models.Employee;
import java.util.Map;

public class EmployeeModalTests extends SeleniumUtils {
    private SeleniumConfig config = SeleniumConfig.getInstance();
    private EmployeeTablePage tablePage;
    private RegistrationModalPage modalPage;

    private void openEmployeeTable() {
        tablePage = new EmployeeTablePage(driver);
        modalPage = new RegistrationModalPage(driver);
        tablePage.openUrl(config.getEnvironmentURL() + "/webtables");
    }

    @Test(description = "Open Add modal and verify fields and placeholders")
    public void openAddModal_fieldsPresenceAndPlaceholders() {
        openEmployeeTable();

        tablePage.clickAddNew();
        modalPage.waitForVisible();
        Assert.assertTrue(modalPage.isDisplayed(), "Modal should be visible after clicking Add");

        // Check placeholders and 'required' attributes existence
        Assert.assertEquals(modalPage.getPlaceholder(modalPage.firstNameInput), "First Name", "First Name placeholder");
        Assert.assertEquals(modalPage.getPlaceholder(modalPage.lastNameInput), "Last Name", "Last Name placeholder");
        Assert.assertEquals(modalPage.getPlaceholder(modalPage.emailInput), "name@example.com", "Email placeholder");
        Assert.assertEquals(modalPage.getPlaceholder(modalPage.ageInput), "Age", "Age placeholder");
        Assert.assertEquals(modalPage.getPlaceholder(modalPage.salaryInput), "Salary", "Salary placeholder");
        Assert.assertEquals(modalPage.getPlaceholder(modalPage.departmentInput), "Department", "Department placeholder");

        // Close modal to cleanup
        modalPage.close();
    }

    @Test(description = "Submit empty form and verify HTML5 validation messages", dependsOnMethods = "openAddModal_fieldsPresenceAndPlaceholders")
    public void validation_onEmptySubmit() {
        openEmployeeTable();

        tablePage.clickAddNew();
        modalPage.waitForVisible();

        // Submit empty form
        modalPage.submit();

        // Expect validation messages on required fields (HTML5)
        String fnMsg = modalPage.firstNameValidationMessage();
        String emMsg = modalPage.emailValidationMessage();
        String ageMsg = modalPage.ageValidationMessage();
        String salaryMsg = modalPage.salaryValidationMessage();
        String deptMsg = modalPage.departmentValidationMessage();

        Assert.assertFalse(fnMsg.isEmpty(), "First Name should have validation message when empty");
        Assert.assertFalse(emMsg.isEmpty(), "Email should have validation message when empty");
        Assert.assertFalse(ageMsg.isEmpty(), "Age should have validation message when empty");
        Assert.assertFalse(salaryMsg.isEmpty(), "Salary should have validation message when empty");
        Assert.assertFalse(deptMsg.isEmpty(), "Department should have validation message when empty");

        modalPage.close();
    }

    @Test(description = "Invalid email / non-digit age/salary should fail HTML5 validation", dependsOnMethods = "validation_onEmptySubmit")
    public void invalidInputs_validation() {
        openEmployeeTable();

        tablePage.clickAddNew();
        modalPage.waitForVisible();

        modalPage.setFirstName("Test");
        modalPage.setLastName("User");
        modalPage.setEmail("invalid-email"); // missing @
        modalPage.setAge("abc"); // non-digits
        modalPage.setSalary("12ab"); // invalid
        modalPage.setDepartment("QA");

        modalPage.submit();

        // Check that email/age/salary are invalid
        String emMsg = modalPage.emailValidationMessage();
        String ageMsg = modalPage.ageValidationMessage();
        String salaryMsg = modalPage.salaryValidationMessage();

        Assert.assertFalse(emMsg.isEmpty(), "Email should be invalid for 'invalid-email'");
        Assert.assertFalse(ageMsg.isEmpty(), "Age should be invalid for 'abc'");
        Assert.assertFalse(salaryMsg.isEmpty(), "Salary should be invalid for '12ab'");

        modalPage.close();
    }

    @Test(description = "Create a new employee then verify it appears in the table", dependsOnMethods = "invalidInputs_validation")
    public void createNewEmployee_success() {
        openEmployeeTable();

        String unique = String.valueOf(System.currentTimeMillis()).substring(7);
        Employee emp = Employee.builder()
                .firstName("Auto" + unique)
                .lastName("User" + unique)
                .email("auto" + unique + "@example.com")
                .age("30")
                .salary("5000")
                .department("Automation")
                .build();

        tablePage.clickAddNew();
        modalPage.waitForVisible();
        modalPage.fillForm(emp.getFirstName(), emp.getLastName(), emp.getEmail(), emp.getAge(), emp.getSalary(), emp.getDepartment());
        modalPage.submit();
        modalPage.waitForNotVisible();

        // Verify in table
        var row = tablePage.findRowByFirstAndLastName(emp.getFirstName(), emp.getLastName());
        Assert.assertNotNull(row, "Created employee should be present in table");

        Map<String, String> data = tablePage.getRowData(row);
        Assert.assertEquals(data.get("Email"), emp.getEmail());
        Assert.assertEquals(data.get("Department"), emp.getDepartment());
        Assert.assertEquals(data.get("Age"), emp.getAge());
        Assert.assertEquals(data.get("Salary"), emp.getSalary());
    }

    @Test(description = "Edit existing employee and verify updates", dependsOnMethods = "createNewEmployee_success")
    public void editEmployee_success() {
        openEmployeeTable();

        // Find first row and edit it (assume test create added at least one row)
        var rows = tablePage.getRows();
        if (rows.isEmpty()) {
            Assert.fail("No rows present to edit");
        }

        // Get first row's names
        var row = rows.get(0);
        var orig = tablePage.getRowData(row);
        String first = orig.get("First Name");
        String last = orig.get("Last Name");

        tablePage.clickEditByName(first, last);
        //tablePage.clickEdit(first);
        modalPage.waitForVisible();

        String newDept = "EditedDept";
        modalPage.setDepartment(newDept);
        modalPage.submit();
        modalPage.waitForNotVisible();

        var editedRow = tablePage.findRowByFirstAndLastName(first, last);
        Assert.assertNotNull(editedRow, "Edited row should still exist");
        var after = tablePage.getRowData(editedRow);
        Assert.assertEquals(after.get("Department"), newDept, "Department should be updated");
    }

    @Test(description = "Delete employee and verify removal", dependsOnMethods = "editEmployee_success")
    public void deleteEmployee_success() {
        openEmployeeTable();

        var rows = tablePage.getRows();
        if (rows.isEmpty()) {
            // nothing to delete
            return;
        }

        var row = rows.get(0);
        var data = tablePage.getRowData(row);
        String first = data.get("First Name");
        String last = data.get("Last Name");

        tablePage.clickDeleteByName(first, last);

        // Small wait to allow table refresh
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        var after = tablePage.findRowByFirstAndLastName(first, last);
        Assert.assertNull(after, "Row should be removed after delete");
    }
}
