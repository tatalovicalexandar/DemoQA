package ui.pages.webtables;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;
import ui.pages.common.BasePageObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class EmployeeTablePage extends BasePageObject  {

    /** =========================================== Table Locators ============================================== */
    public static final By addButton = By.id("addNewRecordButton");
    public static final By searchBox = By.id("searchBox");
    public static final By searchButton = By.id("basic-addon2");

    // Root table
    private static final By tableRoot = By.cssSelector("div.ReactTable");
    private static final By tbodyRows = By.cssSelector("div.rt-tbody div.rt-tr-group");
    private static final By loadingOverlay = By.cssSelector("div.-loading");
    //public static final By rowsInTable = By.cssSelector("div.rt-tbody div.rt-tr-group");

    /** ===================================== Pagination / Page size Locators ======================================== */
    private static final By pageSizeSelect = By.cssSelector("select[aria-label='rows per page']");
    private static final By pageJumpInput = By.cssSelector("input[aria-label='jump to page']");
    private static final By totalPagesSpan = By.cssSelector("span.-totalPages");
    public static final By nextPageButton = By.cssSelector("button[aria-label='Next Page']");

    public EmployeeTablePage(WebDriver driver) {
        super();
    }

    By editByFirstName(String firstName) {
        return By.xpath(
                "//div[@role='row' and .//div[text()='" + firstName + "']]//span[starts-with(@id,'edit-record')]"
        );
    }
    By deleteByFirstName(String firstName) {
        return By.xpath(
                "//div[@role='row' and .//div[text()='" + firstName + "']]//span[starts-with(@id,'delete-record')]"
        );
    }

    public void clickEdit(String firstName) {
        find(editByFirstName(firstName)).click();
    }
    public void clickDelete(String firstName) {
        find(deleteByFirstName(firstName)).click();
    }

    public boolean isUserPresent(String firstName, String lastName) {
        String xpath = "//div[@role='row' and .//div[text()='" + firstName + "'] and .//div[text()='" + lastName + "']]";
        return !driver.findElements(By.xpath(xpath)).isEmpty();
    }

    private String getCellTextSafe(List<WebElement> cells, int index) {
        if (index < 0 || index >= cells.size()) return "";
        String txt = cells.get(index).getText();
        return txt == null ? "" : txt.trim();
    }

    public List<WebElement> getRows() {
        waitForLoadingToFinish();
        return driver.findElements(tbodyRows);
    }

    public Map<String, String> getRowData(WebElement rowGroup) {
        WebElement tr = rowGroup.findElement(By.cssSelector("div.rt-tr"));
        List<WebElement> cells = tr.findElements(By.cssSelector("div.rt-td"));

        Map<String, String> row = new LinkedHashMap<>();
        row.put("First Name", getCellTextSafe(cells, 0));
        row.put("Last Name", getCellTextSafe(cells, 1));
        row.put("Age", getCellTextSafe(cells, 2));
        row.put("Email", getCellTextSafe(cells, 3));
        row.put("Salary", getCellTextSafe(cells, 4));
        row.put("Department", getCellTextSafe(cells, 5));
        return row;
    }

    public void waitForLoadingToFinish() {
        try {
            /*
            wait.until((ExpectedCondition<Boolean>) d -> {
                List<WebElement> elems = d.findElements(loadingOverlay);
                if (elems.isEmpty()) return true;
                return !elems.get(0).isDisplayed();
            });
            */
            wait.waitForElementPresence(loadingOverlay);
        } catch (Exception ignored) { }
    }

    public WebElement findRowByFirstAndLastName(String firstName, String lastName) {
        waitForLoadingToFinish();
        List<WebElement> rows = getRows();
        for (WebElement rg : rows) {
            Map<String, String> data = getRowData(rg);
            if (firstName.equals(data.get("First Name")) && lastName.equals(data.get("Last Name"))) {
                return rg;
            }
        }
        return null;
    }

     // Click on the Add button (if exists)
    public void clickAddNew() {
        WebElement add = find(addButton);
        click(add);
        //waitForLoadingToFinish();
    }

    public void clickEditByName(String firstName, String lastName) {
        WebElement rg = findRowByFirstAndLastName(firstName, lastName);
        if (rg == null) throw new NoSuchElementException("Row not found for " + firstName + " " + lastName);
        WebElement tr = rg.findElement(By.cssSelector("div.rt-tr"));
        WebElement edit = tr.findElement(By.xpath(".//span[contains(@id,'edit-record') or @title='Edit']"));
        click(edit);
    }

    public void clickEditByIndex(int index) {
        List<WebElement> rows = getRows();
        if (index < 0 || index >= rows.size()) throw new IndexOutOfBoundsException("Row index out of range");
        WebElement tr = rows.get(index).findElement(By.cssSelector("div.rt-tr"));
        WebElement edit = tr.findElement(By.xpath(".//span[contains(@id,'edit-record') or @title='Edit']"));
        click(edit);
    }

    public void clickDeleteByName(String firstName, String lastName) {
        WebElement rg = findRowByFirstAndLastName(firstName, lastName);
        if (rg == null) throw new NoSuchElementException("Row not found for " + firstName + " " + lastName);
        WebElement tr = rg.findElement(By.cssSelector("div.rt-tr"));
        WebElement del = tr.findElement(By.xpath(".//span[contains(@id,'delete-record') or @title='Delete']"));
        click(del);
        waitForLoadingToFinish();
    }

    public void clickDeleteByIndex(int index) {
        List<WebElement> rows = getRows();
        if (index < 0 || index >= rows.size()) throw new IndexOutOfBoundsException("Row index out of range");
        WebElement tr = rows.get(index).findElement(By.cssSelector("div.rt-tr"));
        WebElement del = tr.findElement(By.xpath(".//span[contains(@id,'delete-record') or @title='Delete']"));
        click(del);
        waitForLoadingToFinish();
    }

    public int getVisibleRowCount() {
        return getRows().size();
    }
    public int getCurrentPageNumber() {
        try {
            WebElement input = find(pageJumpInput);
            String val = input.getAttribute("value");
            return Integer.parseInt(val);
        } catch (Exception e) {
            return 1;
        }
    }
    public void setRowsPerPage(int pageSize) {
        WebElement element = find(pageSizeSelect);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        Select sel = new Select(element);
        sel.selectByVisibleText(pageSize + " rows");
        waitForLoadingToFinish();
    }

    public void goToPage(int pageNumber) {
        WebElement input = find(pageJumpInput);
        input.clear();
        input.sendKeys(String.valueOf(pageNumber));
        input.sendKeys(Keys.ENTER);
        waitForLoadingToFinish();
    }

    public int getTotalPages() {
        try {
            WebElement total = driver.findElement(totalPagesSpan);
            String txt = total.getText().trim();
            return Integer.parseInt(txt);
        } catch (Exception e) {
            return 1;
        }
    }

    public void search(String text) {
        safeSendKeys(searchBox, text);
        // small wait for table to update
        waitForLoadingToFinish();
    }

    public void clearSearch() {
        WebElement sb = find(searchBox);
        sb.clear();
        sb.sendKeys("");
        waitForLoadingToFinish();
    }

    public boolean isNextEnabled() {
        WebElement nextBtn = driver.findElement(By.cssSelector("div.-next button"));
        return nextBtn.isEnabled();
    }

    public boolean isPreviousEnabled() {
        WebElement prevBtn = driver.findElement(By.cssSelector("div.-previous button"));
        return prevBtn.isEnabled();
    }

    public static int parseSalary(String salaryText) {
        if (salaryText == null || salaryText.isBlank()) return 0;
        String normalized = salaryText.replaceAll("[^0-9-]", "");
        return Integer.parseInt(normalized);
    }

}
