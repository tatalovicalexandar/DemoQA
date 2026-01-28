# BookStore UI & API Test Framework (RestAssured + Selenium + TestNG)

Short description
-----------------
This repository is a starter test framework for demoqa (WebTables, Upload/Download and BookStore). It contains:

- UI tests implemented with Selenium WebDriver and TestNG using Page Object pattern
- API tests implemented with RestAssured and TestNG
- Helpers for file upload/download, polling utilities, and API user/token lifecycle
- Examples of positive and negative tests; tests are independent and designed to be robust

Technologies
------------
- Java
- Maven
- TestNG
- Selenium (selenium-java)
- RestAssured
- Lombok (for DTOs)
- WebDriverManager (driver management)
- Jackson (JSON binding)

Project layout
--------------
- src/test/java/pages/             — Page Objects for UI (EmployeeTablePage, RegistrationModalPage, UploadDownloadPage, ...)
- src/test/java/tests/             — TestNG test classes for UI and API tests
- src/test/java/utils/             — UI helper utilities (FileHelper, common utilities)
- src/test/java/api/               — API test classes and entry points
- src/test/java/api/utils/         — API helper utilities (RestClient, ApiHelpers)
- src/test/java/api/models/        — DTO models (Book, Employee)
- src/test/resources/              — config.properties, testng.xml

Quick start (local)
-------------------
1. Clone the repository and open in your IDE (IntelliJ / Eclipse).
2. Enable annotation processing in the IDE (required for Lombok):
   - IntelliJ: Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable
3. Use WebDriverManager (recommended) or set local browser driver paths.
4. Configure `src/test/resources/config.properties` if needed.
5. Run tests:
   - Run entire suite: mvn test
   - Run API tests only (example): mvn -Dtest=api.** test
   - Run tests directly from folder / package
   - Or use TestNG suites via testng.xml

UI tests overview
-----------------
- Pattern: TestNG + Page Object Model (POM).
- Tests are independent: each test resets/refreshes the page in `@BeforeMethod` due to app behavior (data lost on refresh).
- Upload/Download tests use an isolated temporary download directory per test run.
- Polling / explicit waits are used rather than fixed sleeps where possible.

API tests overview
------------------
- RestAssured with central `RestClient.spec()` and helper methods in `api.utils.ApiHelpers`.
- Tests are independent and create unique test data (UUID/timestamp).
- Helpers implement user/token lifecycle and book create/delete wrappers.
- Tests include both positive and negative scenarios; they attempt cleanup when possible.

Senior SDET considerations (implemented)
---------------------------------------
- Test independence: no implicit ordering; avoid `dependsOnMethods`. Each test creates and cleans up its data where feasible.
- Isolation: download/upload directories are isolated per test run to avoid cross-test interference.
- Robust waits: prefer polling loops and explicit waits instead of fixed `Thread.sleep` to reduce flakiness.
- Negative and boundary cases included (search negative, maxlength enforcement, pagination boundaries, invalid uploads).
- Bug coverage: there is a test that asserts the "Add modal must NOT be pre-filled after Edit -> Close" (a failing test will indicate the bug).
- Logging & artifacts: tests print useful info.

Troubleshooting tips
--------------------
- Upload.sendKeys() error: ensure the path is absolute and accessible.
- Download in headless mode: older headless implementations sometimes block downloads — prefer `--headless=new` or non-headless with Xvfb.
- Lombok missing generated methods: enable annotation processing in IDE.
- Flaky API tests due to infra 5xx: implement retry with exponential backoff for transient errors.
