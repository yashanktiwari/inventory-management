package com.inventory.model;

public class TransactionHistory {

    private int transactionId;

    private String buySell;
    private String plant;
    private String department;
    private String location;

    private String employeeId;
    private String employeeName;

    private String ipAddress;

    private String itemCode;
    private String itemName;
    private String itemMake;
    private String itemModel;
    private String itemSerial;

    private String imeiNo;
    private String simNo;

    private String poNo;
    private String partyName;

    private String status;

    private String issuedDateTime;
    private String returnedDateTime;

    private String remarks;

    private Double itemCount;
    private String unit;

    private String attachmentFile;

    public TransactionHistory(
            int transactionId,
            String buySell,
            String plant,
            String department,
            String location,
            String employeeId,
            String employeeName,
            String ipAddress,
            String itemCode,
            String itemName,
            String itemMake,
            String itemModel,
            String itemSerial,
            String imeiNo,
            String simNo,
            String poNo,
            String partyName,
            String status,
            String issuedDateTime,
            String returnedDateTime,
            String remarks,
            Double itemCount,
            String unit,
            String attachmentFile
    ) {

        this.transactionId = transactionId;

        this.buySell = buySell;
        this.plant = plant;
        this.department = department;
        this.location = location;

        this.employeeId = employeeId;
        this.employeeName = employeeName;

        this.ipAddress = ipAddress;

        this.itemCode = itemCode;
        this.itemName = itemName;
        this.itemMake = itemMake;
        this.itemModel = itemModel;
        this.itemSerial = itemSerial;

        this.imeiNo = imeiNo;
        this.simNo = simNo;

        this.poNo = poNo;
        this.partyName = partyName;

        this.status = status;

        this.issuedDateTime = issuedDateTime;
        this.returnedDateTime = returnedDateTime;

        this.remarks = remarks;

        this.itemCount = itemCount;
        this.unit = unit;

        this.attachmentFile = attachmentFile;
    }

    public int getTransactionId() { return transactionId; }

    public String getBuySell() { return buySell; }
    public String getPlant() { return plant; }
    public String getDepartment() { return department; }
    public String getLocation() { return location; }

    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }

    public String getIpAddress() { return ipAddress; }

    public String getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public String getItemMake() { return itemMake; }
    public String getItemModel() { return itemModel; }
    public String getItemSerial() { return itemSerial; }

    public String getImeiNo() { return imeiNo; }
    public String getSimNo() { return simNo; }

    public String getPoNo() { return poNo; }
    public String getPartyName() { return partyName; }

    public String getStatus() { return status; }

    public String getIssuedDateTime() { return issuedDateTime; }
    public String getReturnedDateTime() { return returnedDateTime; }

    public String getRemarks() { return remarks; }

    public Double getItemCount() { return itemCount; }
    public String getUnit() { return unit; }

    public String getAttachmentFile() { return attachmentFile; }
}