package api;

import api.clients.AuthClient;
import api.clients.RestClient;
import api.endpoints.BookStoreEndpoints;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.hamcrest.Matchers.*;

public class BookStoreE2ETest {
    private String username;
    private String password;
    private String token;
    private String userId;
    private String isbn;

    @BeforeClass
    public void setup() {
        username = "user_" + System.currentTimeMillis();
        password = "Test1234!";

        System.out.println("Registering User: " + username + " with Password: " + password);

        userId = RestClient.spec()
                .body("""
                {
                  "userName": "%s",
                  "password": "%s"
                }
                """.formatted(username, password))
                .post(BookStoreEndpoints.CREATE_USER)
                .then()
                .statusCode(201)
                .extract()
                .path("userID");
        System.out.println("Created User ID: " + userId);

        token = AuthClient.generateToken(username, password);
        System.out.println("Generated Token: " + token);

        isbn = RestClient.spec()
                .get(BookStoreEndpoints.BOOKS)
                .then()
                .statusCode(200)
                .extract()
                .path("books[0].isbn");

        System.out.println("Selected ISBN: " + isbn);
    }

    @Test
    public void addBookToUser() {
        RestClient.specWithAuth(token)
                .body("""
                {
                  "userId": "%s",
                  "collectionOfIsbns": [{ "isbn": "%s" }]
                }
                """.formatted(userId, isbn))
                .post(BookStoreEndpoints.BOOKS)
                .then()
                .statusCode(201);
    }

    @Test
    public void updateBook() {
        String targetIsbn = "9781449337711";

        String jsonBody = """
        {
          "userId": "%s",
          "isbn": "%s"
        }
        """.formatted(userId, targetIsbn);

        int statusCode = RestClient.spec()
                .body(jsonBody)
                .put(BookStoreEndpoints.BOOKS + "/" + isbn)
                .then()
                .statusCode(allOf(greaterThanOrEqualTo(200), lessThan(300)))
                .extract()
                .statusCode();

        System.out.println("Request sent for UserID: " + userId);
        System.out.println("Status code: " + statusCode);
    }

    @Test
    public void deleteBookFromUser() {
        RestClient.specWithAuth(token)
                .delete(BookStoreEndpoints.BOOK_BY_ISBN + "?ISBN=" + isbn)
                .then()
                .statusCode(allOf(greaterThanOrEqualTo(200), lessThan(300)));
    }

    @Test
    public void createUser_withWeakPassword_shouldFail() {
        RestClient.spec()
                .body("""
                {
                  "userName": "weakUser",
                  "password": "12345"
                }
                """)
                .post(BookStoreEndpoints.CREATE_USER)
                .then()
                .statusCode(400)
                .body("message", containsString("Password must be eight characters or longer."));
    }

    @Test
    public void getBooks_withPostMethod_shouldFail() {
        int statusCode = RestClient.spec()
                .post(BookStoreEndpoints.BOOKS)
                .then()
                .statusCode(allOf(greaterThanOrEqualTo(400), lessThan(500)))
                .extract()
                .statusCode();

        System.out.println("Status code: " + statusCode);
    }

    @Test
    public void addBook_withoutAuth_shouldFail() {
        RestClient.spec()
                .body("""
                {
                  "userId": "someId",
                  "collectionOfIsbns": [{ "isbn": "9781449325862" }]
                }
                """)
                .post(BookStoreEndpoints.BOOKS)
                .then()
                .statusCode(401);
    }

    @Test
    public void getBook_withInvalidIsbn_shouldFail() {
        RestClient.spec()
                .get(BookStoreEndpoints.BOOK_BY_ISBN + "?ISBN=invalid")
                .then()
                .statusCode(allOf(greaterThanOrEqualTo(400), lessThan(500)));
    }

    @Test
    public void updateBook_withoutAuth_shouldFail() {
        String targetIsbn = "9781449337711";

        String jsonBody = """
        {
          "userId": "%s",
          "isbn": "%s"
        }
        """.formatted(userId, targetIsbn);

        int statusCode = RestClient.spec()
                .body(jsonBody)
                .put(BookStoreEndpoints.BOOKS + "/" + isbn)
                .then()
                .statusCode(anyOf(is(401), is(400)))
                .extract()
                .statusCode();

        System.out.println("Request sent for UserID: " + userId);
        System.out.println("Status code: " + statusCode);
    }

}
