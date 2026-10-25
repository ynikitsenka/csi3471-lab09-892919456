
# Lab 09B Prompt Engineering & AI Evaluation Log

**Student Name: Liza Nikitsenka** 
**BearID: 892919456 **  
**Repository Link: https://github.com/ynikitsenka/csi3471-lab09-892919456**

---

## Task 1: Data Parsing & Boundary Validation

### Step 0: Synthetic Test CSV Generation (via Copilot)

- Prompt String:

```text
Generate a CSV dataset called transactions.csv with at least 15 records to test my Java transaction parser.
Use these columns: id,timestamp,merchant,amount,category.
Include valid transactions, zero-cent and large amounts, and merchant names with commas or quotation marks. Also include invalid records with missing fields, negative amounts, invalid dates, letters instead of amounts, empty merchant names, malformed lines, and a blank row.
Include a merchant named Baylor Bookstore, Inc. Keep the invalid records in the dataset so I can test my parser's error handling. Generate the complete CSV data, including the header, without any Java code.
```

Follow-up prompt:

```text
insert them in the transactions.csv
```

- Generated CSV Sample:

id,timestamp,merchant,amount,category
1,2026-10-01T09:15:00,Campus Cafe,8.75,Food
2,2026-10-01T11:30:00,"Baylor Bookstore, Inc.",42.99,Books
3,2026-10-02T14:05:00,"Moe's ""Quick"" Shop",12.00,Groceries
4,2026-10-02T16:20:00,City Bus,2.50,Transportation
5,2026-10-03T08:00:00,Cloud Storage,0.00,Subscriptions
6,2026-10-03T12:40:00,Green Market,23.45,Groceries
7,2026-10-04T19:10:00,University Dining,15.00,Food
8,2026-10-05T10:25:00,Electronics Outlet,999999999.99,Electronics
9,2026-10-05T17:55:00,Neighborhood Pharmacy,6.20,Health
10,2026-10-06T13:30:00,Westside Books,31.00,Books
11,2026-10-07T09:00:00,Parking Services,0.01,Transportation
12,2026-10-07T18:45:00,Movie House,28.50,Entertainment
13,2026-10-08T07:15:00,Fresh Bakery,4.00,Food
14,2026-10-08T20:30:00,Hardware Depot,67.89,Home
15,2026-10-09T12:00:00,Pet Supply Co,18.25,Shopping
16,2026-10-10T15:15:00,Missing Amount,,Food
17,2026-10-10T16:00:00,Negative Test,-5.25,Shopping
18,2026-02-30T10:00:00,Invalid Date Shop,10.00,Shopping
19,2026-10-11T11:45:00,Letters Instead of Amount,abc,Food
20,2026-10-11T12:30:00,,14.50,Shopping
21,2026-10-12T08:00:00,Missing Category,7.25
22,2026-10-12T09:30:00,"Unclosed Merchant,21.00,Services
23,2026-10-12T10:45:00,Extra Field Shop,3.50,Other,unexpected
24,2026-10-12T11:30:00,Missing Timestamp,5.00,Travel


### Iteration 1: Naive Code Prompt

- Prompt String:

write the code for TransactionParser.java. It should read a CSV transaction line and turn it into a Transaction object. Check that all the fields are there, the date is valid, 
and the amount is a valid number that is not negative. Handle commas and quotes in merchant names correctly. If a line is invalid, throw InvalidTransactionException with an error message. Keep the code simple and easy to understand. 
Use my existing Java classes and don't change any other files.

- Copilot Output:

public class TransactionParser {

    public Transaction parse(String line) {
        if (line == null) {
            throw new InvalidTransactionException("Transaction line cannot be null");
        }

        List<String> fields = parseCsvFields(line);
        if (fields.size() != 5) {
            throw new InvalidTransactionException(
                    "Expected 5 fields but found " + fields.size());
        }

        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).trim().isEmpty()) {
                throw new InvalidTransactionException(
                        "Field " + (i + 1) + " cannot be empty");
            }
        }

        String id = fields.get(0).trim();
        String timestamp = fields.get(1).trim();
        String merchant = fields.get(2);
        String category = fields.get(4).trim();

        try {
            LocalDateTime.parse(timestamp);
        } catch (DateTimeParseException e) {
            throw new InvalidTransactionException("Invalid timestamp: " + timestamp);
        }

        double amount;
        try {
            amount = Double.parseDouble(fields.get(3).trim());
        } catch (NumberFormatException e) {
            throw new InvalidTransactionException(
                    "Invalid amount: " + fields.get(3));
        }

        if (!Double.isFinite(amount) || amount < 0) {
            throw new InvalidTransactionException(
                    "Amount must be a non-negative number");
        }

        return new Transaction(id, timestamp, merchant, amount, category);
    }

    private List<String> parseCsvFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean quoteClosed = false;

        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);

            if (inQuotes) {
                if (current == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                        quoteClosed = true;
                    }
                } else {
                    field.append(current);
                }
            } else if (current == ',') {
                fields.add(field.toString());
                field.setLength(0);
                quoteClosed = false;
            } else if (current == '"') {
                if (field.length() != 0 || quoteClosed) {
                    throw new InvalidTransactionException(
                            "Unexpected quote in CSV field");
                }
                inQuotes = true;
            } else {
                if (quoteClosed) {
                    throw new InvalidTransactionException(
                            "Unexpected character after quoted CSV field");
                }
                field.append(current);
            }
        }

        if (inQuotes) {
            throw new InvalidTransactionException("Unclosed quoted CSV field");
        }

        fields.add(field.toString());
        return fields;
    }
}


### 4-Dimensional Critical Evaluation

| Evaluation Dimension | Passing? | Concrete Analysis & Identified Flaws |
|---|----------|---|
| Correctness & Logic | Yes      | Checks the fields, date, and amount. Handles commas and quotes in merchant names. |
| Code Quality & Modularity | Yes      | Uses a separate method to handle CSV parsing, keeping the code organized. |
| Security & Safety | Yes      | Rejects invalid input and prevents negative or invalid amounts. |
| Human Comprehension | Yes      | Uses standard Java methods, and the code is easy to follow. |

### Iteration 2: Engineered Prompt & Refactoring

- Prompt String:

I reviewed the generated parser and verified that it uses the existing `Transaction` and `InvalidTransactionException` classes. I checked that invalid timestamps and amounts produce `InvalidTransactionException`, and that the amount cannot be negative or non-finite. The CSV parsing helper handles commas and quotation marks in merchant names instead of relying on a simple `split(",")`. 
I also ran the Maven tests to verify the implementation.

### Step D: What I Would Do Differently

I didn't try a short prompt but I know for sure that if you're not specific enough 
the copilot can fill in things that don't meet the qualifications. 
---

## Task 2: Secure Database Persistence (BookDaoImpl)

### Iteration 1: Naive Code Prompt

- Prompt String:

Write a Java BookDaoImpl class that connects to an SQLite database and supports basic book operations.

- Copilot Output:

public class BookDaoImpl implements BookDao {

    private static final Logger LOGGER =
            Logger.getLogger(BookDaoImpl.class.getName());

    private static final String URL = "jdbc:sqlite:campus_books.db";

    public BookDaoImpl() {
        String sql = "CREATE TABLE IF NOT EXISTS books (" +
                "id INTEGER PRIMARY KEY, title TEXT NOT NULL, author TEXT NOT NULL)";

        try (Connection con = DriverManager.getConnection(URL);
             Statement stmt = con.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            throw new DataAccessException("Database error", e);
        }
    }

    @Override
    public Book findById(int id) {
        String sql = "SELECT * FROM books WHERE id = ?";

        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Book(rs.getInt("id"),
                            rs.getString("title"), rs.getString("author"));
                }
                return null;
            }
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            throw new DataAccessException("Database error", e);
        }
    }

    @Override
    public List<Book> findByAuthor(String author) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE author = ?";

        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, author);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(new Book(rs.getInt("id"),
                            rs.getString("title"), rs.getString("author")));
                }
            }
            return books;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            throw new DataAccessException("Database error", e);
        }
    }

    @Override
    public void insert(Book book) {
        String sql = "INSERT INTO books VALUES (?, ?, ?)";

        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, book.getId());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            throw new DataAccessException("Database error", e);
        }
    }

    @Override
    public void update(Book book) {
        String sql = "UPDATE books SET title = ?, author = ? WHERE id = ?";

        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setInt(3, book.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            throw new DataAccessException("Database error", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM books WHERE id = ?";

        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            throw new DataAccessException("Database error", e);
        }
    }
}

### 4-Dimensional Critical Evaluation

| Evaluation Dimension | Passing? | Concrete Analysis & Identified Flaws |
|---|-----|---|
| Correctness & Logic | Yes | Includes all five database operations and creates the books table. |
| Code Quality & Modularity | Yes | Each database operation has its own method, making the code organized. |
| Security & Safety | Yes |  Uses PreparedStatement for queries with parameters and try-with-resources to close resources. |
| Human Comprehension | Yes | Uses standard Java database methods and is relatively easy to follow. |

### Iteration 2: Engineered Prompt & Refactoring

I didn't have to run it again because whatever it gave me from the first prompt was working accurately

I reviewed the implementation to ensure that all methods in `BookDao` were implemented. 
I checked that queries with user-supplied values use placeholders rather than concatenating values into SQL strings. I also verified the use of try-with-resources and that caught `SQLException` exceptions are logged before being wrapped in `DataAccessException`. I ran `mvn clean test` and confirmed that the build and existing tests passed.

### Step D: What I Would Do Differently

I would't do anything differently because the code was already correct and passed all tests. I would have added more tests to check for edge cases, such as inserting a book with a duplicate ID or deleting a non-existent book, 
to ensure that the implementation handles these scenarios gracefully.

---

## Task 3: Copilot JUnit 5 Test Generation & Edge-Case Audit

### Iteration 1: Naive Test Generation

- Prompt String:

Help me write JUnit 5 tests for my TransactionParser class. Test a valid transaction, invalid dates, negative amounts, missing fields, and merchant names with commas and quotes. Use @ParameterizedTest with @ValueSource or @CsvSource, and use assertThrows for invalid records. 
Keep the tests simple and easy to understand. Don't change my other files.

- Copilot Output:

class TransactionParserTest {

    private final TransactionParser parser = new TransactionParser();

    @Test
    void parsesValidTransaction() {
        Transaction transaction = parser.parse(
                "1,2026-10-01T09:15:00,Campus Cafe,8.75,Food");

        assertEquals("1", transaction.getId());
        assertEquals("2026-10-01T09:15:00", transaction.getTimestamp());
        assertEquals("Campus Cafe", transaction.getMerchant());
        assertEquals(8.75, transaction.getAmount());
        assertEquals("Food", transaction.getCategory());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1,2026-02-30T10:00:00,Invalid Date Shop,10.00,Shopping",
            "1,not-a-date,Invalid Date Shop,10.00,Shopping"
    })
    void rejectsInvalidDates(String line) {
        assertThrows(InvalidTransactionException.class, () -> parser.parse(line));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1,2026-10-01T09:15:00,Negative Test,-5.25,Shopping",
            "1,2026-10-01T09:15:00,Letters Instead of Amount,abc,Food"
    })
    void rejectsInvalidAmounts(String line) {
        assertThrows(InvalidTransactionException.class, () -> parser.parse(line));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1,2026-10-01T09:15:00,Missing Category,7.25",
            "1,2026-10-01T09:15:00,Missing Amount,,Food"
    })
    void rejectsMissingFields(String line) {
        assertThrows(InvalidTransactionException.class, () -> parser.parse(line));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "2,2026-10-01T11:30:00,\"Baylor Bookstore, Inc.\",42.99,Books;Baylor Bookstore, Inc.",
            "3,2026-10-02T14:05:00,\"Moe's \"\"Quick\"\" Shop\",12.00,Groceries;Moe's \"Quick\" Shop"
    }, delimiter = ';', quoteCharacter = '\u0000')
    void parsesMerchantNamesWithCommasAndQuotes(String line, String expectedMerchant) {
        Transaction transaction = parser.parse(line);

        assertEquals(expectedMerchant, transaction.getMerchant());
    }

}


### 4-Dimensional Critical Evaluation

| Evaluation Dimension | Passing? | Concrete Analysis & Identified Flaws |
|---|----------|--|
| Correctness & Logic | no       | Copilot missed two edge cases: an empty merchant name and a blank input line.|
| Code Quality & Modularity | yes      | Uses separate test methods and parameterized tests to organize the checks. |
| Security & Safety | yes      |Checks that invalid transaction data is rejected. |
| Human Comprehension | yes      | Test names make it clear what each test checks. |

### Iteration 2: Refined Tests & Manual Edge-Case Audit


I manually added two additional edge-case tests:

1. `rejectsEmptyMerchant()` checks that a transaction with an empty merchant name throws `InvalidTransactionException`.
2. `rejectsBlankLine()` checks that an empty input line throws `InvalidTransactionException`.

@Test
void rejectsEmptyMerchant() {
String line = "1,2026-10-01T09:15:00,,8.75,Food";

        assertThrows(InvalidTransactionException.class,
                () -> parser.parse(line));
    }
    @Test
    void rejectsBlankLine() {
        assertThrows(InvalidTransactionException.class,
                () -> parser.parse(""));

These tests extend the coverage of invalid input. I then ran `mvn clean test` to verify the test suite.

### Step D: What I Would Do Differently

Make sure i tell it to include every possible test and list some instances 

## Final Verification

Before submission, I ran `mvn clean test` and confirmed that the Maven build and tests passed.

The project should be cleaned with `mvn clean` before creating the ZIP file. The submission should include the Maven project ZIP and `PROMPT_LOG.md`.
