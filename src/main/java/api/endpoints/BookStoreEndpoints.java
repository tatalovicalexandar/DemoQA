package api.endpoints;

public class BookStoreEndpoints {
    public static final String CREATE_USER = "/Account/v1/User";
    public static final String GENERATE_TOKEN = "/Account/v1/GenerateToken";


    // Book Store Endpoint to manage relation: User → Book: POST, GET, DELETE
    public static final String BOOKS = "/BookStore/v1/Books";

    // Endpoint to manage relation: User → Book (by ISBN): GET and DELETE
    public static final String BOOK_BY_ISBN = "/BookStore/v1/Book";

    // Endpoint to manage relation: User → Book (by ISBN): PUT
    public static final String EDIT_BY_ISBN = "/BookStore/v1/Book/{ISBN}";
}
