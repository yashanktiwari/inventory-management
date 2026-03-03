package com.inventory.model;

public class TransactionHistory {

    private int transactionId;
    private String itemName;
    private String personName;
    private String issuedDateTime;
    private String returnedDateTime;
    private String remarks;
    private String employeeId;
    private String itemId;

    public TransactionHistory(int transactionId,
                              String itemId,
                              String itemName,
                              String employeeId,
                              String personName,
                              String issuedDateTime,
                              String returnedDateTime,
                              String remarks) {

        this.transactionId = transactionId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.employeeId = employeeId;
        this.personName = personName;
        this.issuedDateTime = issuedDateTime;
        this.returnedDateTime = returnedDateTime;
        this.remarks = remarks;
    }

    public int getTransactionId() { return transactionId; }
    public String getItemName() { return itemName; }
    public String getPersonName() { return personName; }
    public String getIssuedDateTime() { return issuedDateTime; }
    public String getReturnedDateTime() { return returnedDateTime; }
    public String getRemarks() { return remarks; }
    public String getEmployeeId() { return employeeId; }
    public String getItemId() { return itemId; }
}