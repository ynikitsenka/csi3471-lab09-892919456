package edu.baylor.csi3471.parser;

public class Transaction {
    private String id;
    private String timestamp;
    private String merchant;
    private double amount;
    private String category;

    public Transaction(String id, String timestamp, String merchant,
                       double amount, String category) {
        this.id = id;
        this.timestamp = timestamp;
        this.merchant = merchant;
        this.amount = amount;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMerchant() {
        return merchant;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }
}