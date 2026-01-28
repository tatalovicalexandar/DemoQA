package api.helpers;

import api.clients.RestClient;
import api.endpoints.BookStoreEndpoints;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class BookStoreApiHelper {
    private static final Logger log = LogManager.getLogger(BookStoreApiHelper.class);
    public static void addBookToUser(String token, String userId, String isbn) {

        log.info("Adding book with ISBN: " + isbn + " to user with ID: " + userId);

        RestClient.spec()
            .header("Authorization", "Bearer " + token)
            .body("""
            {
              "userId": "%s",
              "collectionOfIsbns": [{ "isbn": "%s" }]
            }
            """.formatted(userId, isbn))
            .post(BookStoreEndpoints.BOOKS)
            .then()
            .statusCode(anyOf(is(201), is(409)));

        log.info("Book with ISBN: " + isbn + " added to user with ID: " + userId);
    }
}
