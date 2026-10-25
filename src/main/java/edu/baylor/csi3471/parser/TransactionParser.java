package edu.baylor.csi3471.parser;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

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
