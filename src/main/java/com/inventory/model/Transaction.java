package com.inventory.model;

public class Transaction {

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
    private Double itemCount;
    private String unit;

    public Transaction() {}

    public Transaction(String buySell, String plant, String department, String location, String employeeId, String employeeName, String ipAddress, String itemCode, String itemName, String itemMake, String itemModel, String itemSerial, String imeiNo, String simNo, String poNo, String partyName, String status, Double itemCount, String unit) {
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
        this.itemCount = itemCount;
        this.unit = unit;
    }

    public Double getItemCount() {
        return itemCount;
    }

    public void setItemCount(Double itemCount) {
        this.itemCount = itemCount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getBuySell() {
        return buySell;
    }

    public void setBuySell(String buySell) {
        this.buySell = buySell;
    }

    public String getPlant() {
        return plant;
    }

    public void setPlant(String plant) {
        this.plant = plant;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemMake() {
        return itemMake;
    }

    public void setItemMake(String itemMake) {
        this.itemMake = itemMake;
    }

    public String getItemModel() {
        return itemModel;
    }

    public void setItemModel(String itemModel) {
        this.itemModel = itemModel;
    }

    public String getItemSerial() {
        return itemSerial;
    }

    public void setItemSerial(String itemSerial) {
        this.itemSerial = itemSerial;
    }

    public String getImeiNo() {
        return imeiNo;
    }

    public void setImeiNo(String imeiNo) {
        this.imeiNo = imeiNo;
    }

    public String getSimNo() {
        return simNo;
    }

    public void setSimNo(String simNo) {
        this.simNo = simNo;
    }

    public String getPoNo() {
        return poNo;
    }

    public void setPoNo(String poNo) {
        this.poNo = poNo;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}