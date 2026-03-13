package com.inventory.model.master;

public class EmployeeMaster {

    private String employeeCode;
    private String employeeName;

    public EmployeeMaster() {}

    public EmployeeMaster(String employeeCode, String employeeName) {
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
    }

    public String getEmployeeCode() { return employeeCode; }
    public String getEmployeeName() { return employeeName; }

    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
}
