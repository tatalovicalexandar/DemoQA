package api.clients;

import api.endpoints.BookStoreEndpoints;

public class AuthClient {

    public static String generateToken(String username, String password) {
        return RestClient.spec()
                .body("""
            {
              "userName": "%s",
              "password": "%s"
            }
            """.formatted(username, password))
                .post(BookStoreEndpoints.GENERATE_TOKEN)
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }
}

