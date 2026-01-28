package api;

public class BookStoreApiTests {
/*
    @Test
    public void createNewUser() {
        String payload = """
        {
            "userName": "testuser",
            "password": "Test@1234"
        }
        """;

        Response response = RestClient.spec()
                .body(payload)
                .post("/Account/v1/User");

        Assert.assertEquals(response.getStatusCode(), 201, "Status code should be 201");
        Assert.assertNotNull(response.jsonPath().getString("userId"), "User ID should not be null");
    }

    @Test
    public void getListOfAllBooks() {
        Response response = RestClient.spec()
                .get("/BookStore/v1/Books");

        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");
        Assert.assertTrue(response.jsonPath().getList("books").size() > 0, "Books list should not be empty");
    }

    @Test
    public void createNewBook() {
        Book newBook = Book.builder()
                .isbn("9781449325862")
                .title("Test Book")
                .subTitle("A Subtitle")
                .author("Test Author")
                .publishDate("2023-01-01")
                .publisher("Test Publisher")
                .pages(100)
                .description("A test book description")
                .website("https://example.com")
                .build();

        Response response = RestClient.spec()
                .body(newBook)
                .post("/BookStore/v1/Books");

        Assert.assertEquals(response.getStatusCode(), 201, "Status code should be 201");
        Assert.assertEquals(response.jsonPath().getString("isbn"), newBook.getIsbn(), "ISBN should match");
    }

    @Test
    public void getPreviouslyCreatedBook() {
        String isbn = "9781449325862"; // Replace with the ISBN of the created book

        Response response = RestClient.spec()
                .get("/BookStore/v1/Book?ISBN=" + isbn);

        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");
        Assert.assertEquals(response.jsonPath().getString("isbn"), isbn, "ISBN should match the requested value");
    }

    @Test
    public void editBook() {
        String isbn = "9781449325862"; // Replace with the ISBN of the created book
        String updatedTitle = "Updated Test Book";

        Book updatedBook = Book.builder()
                .isbn(isbn)
                .title(updatedTitle)
                .build();

        Response response = RestClient.spec()
                .body(updatedBook)
                .put("/BookStore/v1/Book");

        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");
        Assert.assertEquals(response.jsonPath().getString("title"), updatedTitle, "Title should be updated");
    }

    @Test
    public void deleteBook() {
        String isbn = "9781449325862"; // Replace with the ISBN of the created book

        Response response = RestClient.spec()
                .delete("/BookStore/v1/Book?ISBN=" + isbn);

        Assert.assertEquals(response.getStatusCode(), 204, "Status code should be 204");
    }

 */
}
