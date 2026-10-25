package edu.baylor.csi3471.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    }
}
