package com.inventory.model;

import java.time.LocalDateTime;
import java.util.List;

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

    private LocalDateTime issuedDateTime;
    private LocalDateTime returnedDateTime;

    private String remarks;

    private Double itemCount;
    private String unit;

    private String attachmentFile;

    private String lastModifiedBy;
    private List<AuditEntry> auditEntries;

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
            LocalDateTime issuedDateTime,
            LocalDateTime returnedDateTime,
            String remarks,
            Double itemCount,
            String unit,
            String attachmentFile,
            String lastModifiedBy,
            List<AuditEntry> auditEntries
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

        this.lastModifiedBy = lastModifiedBy;
        this.auditEntries = auditEntries;
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

    public LocalDateTime getIssuedDateTime() { return issuedDateTime; }
    public LocalDateTime getReturnedDateTime() { return returnedDateTime; }

    public String getRemarks() { return remarks; }

    public Double getItemCount() { return itemCount; }
    public String getUnit() { return unit; }

    public String getAttachmentFile() { return attachmentFile; }

    public String getLastModifiedBy() { return lastModifiedBy; }
    public List<AuditEntry> getAuditEntries() { return auditEntries; }
}