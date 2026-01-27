package utils;

import config.Config;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

public class RestClient {
    private static RequestSpecification requestSpec;

    static {
        String baseUri = Config.getOrDefault("base.uri", "https://demoqa.com");
        String basePath = Config.getOrDefault("base.path", "");
        RestAssured.baseURI = baseUri;
        RestAssured.basePath = basePath;

        RequestSpecBuilder builder = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .setContentType("application/json");

        String timeout = Config.getOrDefault("request.timeout", "5000");
        // RestAssured default timeouts can be configured per request if needed.

        requestSpec = builder.build();
    }

    public static RequestSpecification spec() {
        return RestAssured.given().spec(requestSpec);
    }

    public static String resolveEndpoint(String template) {
        // Simple placeholder replacement for {isbn} or other path params
        // Example usage: config has book.byIsbn.endpoint=/BookStore/v1/Book/{isbn}
        if (template == null) return "";
        return template;
    }
}
