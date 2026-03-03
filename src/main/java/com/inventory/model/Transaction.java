package com.inventory.model;

public class Transaction {

    private int transactionId;
    private int itemId;
    private int personId;
    private String issuedDateTime;
    private String returnedDateTime;
    private String remarks;

    public Transaction() {}

    public Transaction(int itemId, int personId, String issuedDateTime, String remarks) {
        this.itemId = itemId;
        this.personId = personId;
        this.issuedDateTime = issuedDateTime;
        this.remarks = remarks;
    }

    public Transaction(int transactionId, int itemId, int personId,
                       String issuedDateTime, String returnedDateTime, String remarks) {
        this.transactionId = transactionId;
        this.itemId = itemId;
        this.personId = personId;
        this.issuedDateTime = issuedDateTime;
        this.returnedDateTime = returnedDateTime;
        this.remarks = remarks;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public String getIssuedDateTime() {
        return issuedDateTime;
    }

    public void setIssuedDateTime(String issuedDateTime) {
        this.issuedDateTime = issuedDateTime;
    }

    public String getReturnedDateTime() {
        return returnedDateTime;
    }

    public void setReturnedDateTime(String returnedDateTime) {
        this.returnedDateTime = returnedDateTime;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}