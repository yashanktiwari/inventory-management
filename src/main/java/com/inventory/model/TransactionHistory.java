package com.inventory.model;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionHistory {

    private int transactionId;

    private String buySell;
    private String plant;
    private String department;
    private String location;

    private String employeeCode;
    private String employeeName;

    private String ipAddress;

    private String itemCode;
    private String itemName;
    private String itemMake;
    private String itemModel;
    private String itemSerial;
    private String itemCondition;
    private String itemLocation;
    private String itemCategory;

    private String imeiNo;
    private String simNo;

    private String poNo;
    private String partyName;

    private String status;

    private LocalDateTime issuedDateTime;
    private LocalDateTime returnedDateTime;

    private String remarks;

    private Double itemCount;
    private double minimumStock;
    private String unit;

    private String attachmentFile;

    private String lastModifiedBy;
    private List<AuditEntry> auditEntries;
    private boolean available;

    private Integer parentTransactionId;

    public TransactionHistory() {
    }

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
            String itemCondition,
            String itemLocation,
            String itemCategory,
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
            List<AuditEntry> auditEntries,
            int parentTransactionId
    ) {

        this.transactionId = transactionId;
        this.parentTransactionId = parentTransactionId;

        this.buySell = buySell;
        this.plant = plant;
        this.department = department;
        this.location = location;

        this.employeeCode = employeeId;
        this.employeeName = employeeName;

        this.ipAddress = ipAddress;

        this.itemCode = itemCode;
        this.itemName = itemName;
        this.itemMake = itemMake;
        this.itemModel = itemModel;
        this.itemSerial = itemSerial;
        this.itemCondition = itemCondition;
        this.itemLocation = itemLocation;
        this.itemCategory = itemCategory;

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

    public String getItemCondition() { return itemCondition; }
    public int getTransactionId() { return transactionId; }

    public String getBuySell() { return buySell; }
    public String getPlant() { return plant; }
    public String getDepartment() { return department; }
    public String getLocation() { return location; }

    public String getEmployeeCode() { return employeeCode; }
    public String getEmployeeName() { return employeeName; }

    public String getIpAddress() { return ipAddress; }

    public String getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public String getItemMake() { return itemMake; }
    public String getItemModel() { return itemModel; }
    public String getItemSerial() { return itemSerial; }
    public String getItemLocation() { return itemLocation; }
    public String getItemCategory() { return itemCategory; }

    public String getImeiNo() { return imeiNo; }
    public String getSimNo() { return simNo; }

    public String getPoNo() { return poNo; }
    public String getPartyName() { return partyName; }

    public String getStatus() { return status; }

    public LocalDateTime getIssuedDateTime() { return issuedDateTime; }
    public LocalDateTime getReturnedDateTime() { return returnedDateTime; }

    public String getRemarks() { return remarks; }

    public Double getItemCount() { return itemCount; }
    public double getMinimumStock() { return minimumStock; }
    public String getUnit() { return unit; }

    public String getAttachmentFile() { return attachmentFile; }

    public String getLastModifiedBy() { return lastModifiedBy; }
    public List<AuditEntry> getAuditEntries() { return auditEntries; }

    public boolean isAvailable() { return available; }

    public Integer getParentTransactionId() {
        return parentTransactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public void setBuySell(String buySell) {
        this.buySell = buySell;
    }

    public void setPlant(String plant) {
        this.plant = plant;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemMake(String itemMake) {
        this.itemMake = itemMake;
    }

    public void setItemModel(String itemModel) {
        this.itemModel = itemModel;
    }

    public void setItemSerial(String itemSerial) {
        this.itemSerial = itemSerial;
    }

    public void setItemCondition(String itemCondition) {
        this.itemCondition = itemCondition;
    }

    public void setItemLocation(String itemLocation) {
        this.itemLocation = itemLocation;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public void setImeiNo(String imeiNo) {
        this.imeiNo = imeiNo;
    }

    public void setSimNo(String simNo) {
        this.simNo = simNo;
    }

    public void setPoNo(String poNo) {
        this.poNo = poNo;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setIssuedDateTime(LocalDateTime issuedDateTime) {
        this.issuedDateTime = issuedDateTime;
    }

    public void setReturnedDateTime(LocalDateTime returnedDateTime) {
        this.returnedDateTime = returnedDateTime;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setItemCount(Double itemCount) {
        this.itemCount = itemCount;
    }

    public void setMinimumStock(double minimumStock) {
        this.minimumStock = minimumStock;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setAttachmentFile(String attachmentFile) {
        this.attachmentFile = attachmentFile;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public void setAuditEntries(List<AuditEntry> auditEntries) {
        this.auditEntries = auditEntries;
    }

    public void setParentTransactionId(Integer parentTransactionId) {
        this.parentTransactionId = parentTransactionId;
    }

    public void setAvailable(boolean available) { this.available = available; }
}