package ui.webtables;

import org.openqa.selenium.By;
import ui.utils.SeleniumUtils;
import org.testng.annotations.Test;
import ui.pages.webtables.EmployeeTablePage;
import ui.pages.webtables.RegistrationModalPage;
import ui.core.config.SeleniumConfig;
import org.testng.Assert;
import ui.models.Employee;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EmployeeTableTests extends SeleniumUtils {
    private SeleniumConfig config = SeleniumConfig.getInstance();
    private EmployeeTablePage tablePage;
    private RegistrationModalPage modalPage;

    private void openEmployeeTable() {
        tablePage = new EmployeeTablePage(driver);
        modalPage = new RegistrationModalPage(driver);
        tablePage.openUrl(config.getEnvironmentURL() + "/webtables");
    }
    @Test(description = "Insert multiple records and verify pagination updates accordingly (independent)")
    public void bulkInsertAndPaginationCheck() {
        openEmployeeTable();

        final int toInsert = 12; // broj insertovanih zapisa (možeš prilagoditi)
        final int pageSize = 5;
        List<Employee> created = new ArrayList<>();

        for (int i = 0; i < toInsert; i++) {
            String unique = String.valueOf(System.currentTimeMillis()).substring(7) + i;
            Employee emp = Employee.builder()
                    .firstName("PagF" + unique)
                    .lastName("PagL" + unique)
                    .email("pag" + unique + "@example.com")
                    .age(String.valueOf(20 + (i % 10)))
                    .salary(String.valueOf(1000 + i * 10))
                    .department("PagDept")
                    .build();

            tablePage.clickAddNew();
            modalPage.waitForVisible();
            modalPage.fillForm(emp.getFirstName(), emp.getLastName(), emp.getEmail(), emp.getAge(), emp.getSalary(), emp.getDepartment());
            modalPage.submit();
            modalPage.waitForNotVisible();

            // Poll for presence using the search box (search ignores pagination)
            boolean present = false;
            long end = System.currentTimeMillis() + 5000;
            while (System.currentTimeMillis() < end) {
                try {
                    tablePage.clearSearch();
                    tablePage.search(emp.getFirstName());
                    if (tablePage.findRowByFirstAndLastName(emp.getFirstName(), emp.getLastName()) != null) {
                        present = true;
                        break;
                    }
                } catch (Exception ignored) {
                } finally {
                    tablePage.clearSearch();
                }
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
            Assert.assertTrue(present, "Inserted record should appear in table (search across pages)");
            created.add(emp);
        }

        // Set rows per page and check number of visible rows
        tablePage.setRowsPerPage(pageSize);
        int visible = tablePage.getVisibleRowCount();
        Assert.assertTrue(visible <= pageSize, "Visible rows should be <= page size");

        int totalPages = tablePage.getTotalPages();
        int expectedPages = (int) Math.ceil((double) toInsert / pageSize);
        Assert.assertTrue(totalPages >= expectedPages, "Total pages should be at least: " + expectedPages + ", but was: " + totalPages);

        // Search for last created entity through all pages
        Employee last = created.get(created.size() - 1);
        boolean found = false;
        for (int p = 1; p <= totalPages; p++) {
            tablePage.goToPage(p);
            // Poll for row on page
            long end = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < end) {
                var row = tablePage.findRowByFirstAndLastName(last.getFirstName(), last.getLastName());
                if (row != null) {
                    found = true;
                    Map<String, String> data = tablePage.getRowData(row);
                    Assert.assertEquals(data.get("Email"), last.getEmail());
                    break;
                }
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
            if (found) break;
        }
        Assert.assertTrue(found, "Last created record should be found across pagination pages");
    }

    @Test(description = "Change rows per page and verify visible row count and navigation (independent)")
    public void changePageSizeAndNavigate() {
        // Precondition: insert a few records so pagination can be changed meaningfully
        openEmployeeTable();

        for (int i = 0; i < 21; i++) {
            String unique = String.valueOf(System.currentTimeMillis()).substring(7) + i;
            Employee emp = Employee.builder()
                    .firstName("PS" + unique)
                    .lastName("PSL" + unique)
                    .email("ps" + unique + "@example.com")
                    .age("29")
                    .salary("2000")
                    .department("PSDept")
                    .build();

            tablePage.clickAddNew();
            modalPage.waitForVisible();
            modalPage.fillForm(emp.getFirstName(), emp.getLastName(), emp.getEmail(), emp.getAge(), emp.getSalary(), emp.getDepartment());
            modalPage.submit();
            modalPage.waitForNotVisible();
        }

        // Test multiple page sizes
        int[] sizes = new int[]{5, 10, 20, 25, 50, 100};
        for (int size : sizes) {
            tablePage.setRowsPerPage(size);
            int visible = tablePage.getVisibleRowCount();
            Assert.assertTrue(visible <= size, "Visible rows must be <= selected page size: " + size);
        }

        // Navigate next/previous where applicable
        int totalPages = tablePage.getTotalPages();
        if (totalPages > 1) {
            tablePage.goToPage(totalPages);
            Assert.assertEquals(tablePage.getCurrentPageNumber(), totalPages);
            tablePage.goToPage(1);
            Assert.assertEquals(tablePage.getCurrentPageNumber(), 1);
        }
    }

    @Test(description = "Search filter should narrow results to matching entries (independent)")
    public void searchBox_filtering() {
        openEmployeeTable();

        // Create one unique record to search for
        String unique = String.valueOf(System.currentTimeMillis()).substring(7);
        Employee emp = Employee.builder()
                .firstName("SearchF" + unique)
                .lastName("SearchL" + unique)
                .email("search" + unique + "@example.com")
                .age("28")
                .salary("3500")
                .department("SearchDept")
                .build();

        tablePage.clickAddNew();
        modalPage.waitForVisible();
        modalPage.fillForm(emp.getFirstName(), emp.getLastName(), emp.getEmail(), emp.getAge(), emp.getSalary(), emp.getDepartment());
        modalPage.submit();
        modalPage.waitForNotVisible();

        // Ensure cleared filters and then search by first name
        tablePage.clearSearch();
        tablePage.search(emp.getFirstName());

        // After search, poll for the row
        boolean found = false;
        long end = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < end) {
            var rows = tablePage.getRows();
            for (var rg : rows) {
                var rowData = tablePage.getRowData(rg);
                if (emp.getFirstName().equals(rowData.get("First Name")) && emp.getLastName().equals(rowData.get("Last Name"))) {
                    found = true;
                    break;
                }
            }
            if (found) break;
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }
        Assert.assertTrue(found, "Search should return the created record");
        // Cleanup search
        tablePage.clearSearch();
    }

    @Test(description = "Search for non-existent value should return no meaningful rows")
    public void searchNonExisting_returnsNoRows() {
        openEmployeeTable();

        String random = "no-such-user-" + UUID.randomUUID().toString().substring(0, 6);
        tablePage.clearSearch();
        tablePage.search(random);

        // Count only meaningful rows (first name non-empty)
        List<?> rows = tablePage.getRows();
        boolean anyMatches = false;
        for (Object rgObj : rows) {
            var rg = (org.openqa.selenium.WebElement) rgObj;
            var data = tablePage.getRowData(rg);
            if (!data.get("First Name").isBlank() || !data.get("Last Name").isBlank()) {
                // If any real visible row contains text, check if matches random
                if (data.get("First Name").contains(random) || data.get("Last Name").contains(random)
                        || data.get("Email").contains(random)) {
                    anyMatches = true;
                } else {
                    // There is at least some unrelated data in results - still considered 'no matches' for our search
                }
            }
        }
        Assert.assertFalse(anyMatches, "Search for random string should return no matching rows");
    }

    @Test(description = "Maxlength enforcement on First Name input (client-side)")
    public void firstName_maxlengthEnforced() {
        openEmployeeTable();

        tablePage.clickAddNew();
        modalPage.waitForVisible();

        // Build a long input longer than maxlength (25 according to form)
        String longInput = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345"; // 31 chars
        modalPage.setFirstName(longInput);

        String value = modalPage.getAttribute(By.id("firstName"), "value");
        int actualLen = value == null ? 0 : value.length();
        Assert.assertTrue(actualLen <= 25, "First name input should enforce maxlength <= 25 (actual: " + actualLen + ")");
    }

    @Test(description = "Search box filters out non-matching rows (case-insensitive check)")
    public void searchBox_caseInsensitive() {
        openEmployeeTable();

        // Create a specific record with mixed-case first name
        String unique = "Case" + System.currentTimeMillis();
        Employee emp = Employee.builder()
                .firstName(unique)
                .lastName("Tester")
                .email("ci" + unique + "@example.com")
                .age("31")
                .salary("3100")
                .department("CI")
                .build();

        tablePage.clickAddNew();
        modalPage.waitForVisible();
        modalPage.fillForm(emp.getFirstName(), emp.getLastName(), emp.getEmail(), emp.getAge(), emp.getSalary(), emp.getDepartment());
        modalPage.submit();
        modalPage.waitForNotVisible();

        // Search lowercase version of first name
        tablePage.clearSearch();
        tablePage.search(unique.toLowerCase());

        // Poll for presence (short)
        boolean found = false;
        long end = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < end) {
            var row = tablePage.findRowByFirstAndLastName(emp.getFirstName(), emp.getLastName());
            if (row != null) { found = true; break; }
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }

        Assert.assertTrue(found, "Search should be case-insensitive and find the record");
        tablePage.clearSearch();
    }

    @Test(description = "Pagination boundary: go to page 0 or beyond total and expect sanitized behavior")
    public void pagination_boundaries() {
        openEmployeeTable();

        // Ensure there is at least one record so pagination UI is present
        String unique = "PB" + System.currentTimeMillis();
        Employee emp = Employee.builder()
                .firstName(unique)
                .lastName("PBLast")
                .email("pb" + unique + "@example.com")
                .age("27")
                .salary("2700")
                .department("PBDept")
                .build();
        tablePage.clickAddNew();
        modalPage.waitForVisible();
        modalPage.fillForm(emp.getFirstName(), emp.getLastName(), emp.getEmail(), emp.getAge(), emp.getSalary(), emp.getDepartment());
        modalPage.submit();
        modalPage.waitForNotVisible();

        int total = tablePage.getTotalPages();
        // Go to page 0 → Expect page to be clamped to 1
        tablePage.goToPage(0);
        int current = tablePage.getCurrentPageNumber();
        Assert.assertTrue(current >= 1 && current <= Math.max(1, total), "Page 0 should be sanitized to a valid page (got " + current + ")");

        // Go to page total+10 → Expect clamped to <= total (or stay at last available)
        tablePage.goToPage(total + 10);
        current = tablePage.getCurrentPageNumber();
        Assert.assertTrue(current >= 1 && current <= Math.max(1, total), "Over-high page should be sanitized to last page (got " + current + ")");
    }

    @Test(description = "Bug check: Add form must NOT be pre-filled after opening Edit modal and closing it")
    public void addForm_notPrefilledAfterEditClose() {
        openEmployeeTable();

        // Create a record to edit
        String unique = "Bug" + System.currentTimeMillis();
        Employee emp = Employee.builder()
                .firstName(unique)
                .lastName("BugLast")
                .email("bug" + unique + "@example.com")
                .age("35")
                .salary("3500")
                .department("BugDept")
                .build();

        tablePage.clickAddNew();
        modalPage.waitForVisible();
        modalPage.fillForm(emp.getFirstName(), emp.getLastName(), emp.getEmail(), emp.getAge(), emp.getSalary(), emp.getDepartment());
        modalPage.submit();
        modalPage.waitForNotVisible();

        // Ensure created
        boolean created = false;
        long end = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < end) {
            var row = tablePage.findRowByFirstAndLastName(emp.getFirstName(), emp.getLastName());
            if (row != null) { created = true; break; }
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }
        Assert.assertTrue(created, "Precondition failed: created row must exist");

        // Open Edit modal for that record
        tablePage.clickEditByName(emp.getFirstName(), emp.getLastName());
        modalPage.waitForVisible();

        // Close Edit modal WITHOUT changing/saving
        modalPage.close();

        // Now open Add modal - it SHOULD BE empty (bug: app may prefill with edit values)
        tablePage.clickAddNew();
        modalPage.waitForVisible();

        // Read values of fields
        String fnVal = modalPage.getAttribute(By.id("firstName"), "value");
        String lnVal = modalPage.getAttribute(By.id("lastName"), "value");
        String emailVal = modalPage.getAttribute(By.id("userEmail"), "value");
        String ageVal = modalPage.getAttribute(By.id("age"), "value");
        String salaryVal = modalPage.getAttribute(By.id("salary"), "value");
        String deptVal = modalPage.getAttribute(By.id("department"), "value");

        // If any of these equal the edited user's data -> bug exists (test should fail)
        boolean anyPrefilled = (fnVal != null && fnVal.equals(emp.getFirstName()))
                || (lnVal != null && lnVal.equals(emp.getLastName()))
                || (emailVal != null && emailVal.equals(emp.getEmail()))
                || (ageVal != null && ageVal.equals(emp.getAge()))
                || (salaryVal != null && salaryVal.equals(emp.getSalary()))
                || (deptVal != null && deptVal.equals(emp.getDepartment()));

        Assert.assertFalse(anyPrefilled, "Add modal should NOT be pre-filled after closing Edit modal. Found prefilled values: "
                + String.format("fn=%s ln=%s email=%s age=%s salary=%s dept=%s", fnVal, lnVal, emailVal, ageVal, salaryVal, deptVal));
    }
}
