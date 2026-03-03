package com.inventory.model;

public class Person {

    private int personId;
    private String employeeId;
    private String personName;
    private String department;

    public Person() {}

    public Person(String employeeId, String personName, String department) {
        this.employeeId = employeeId;
        this.personName = personName;
        this.department = department;
    }

    public Person(int personId, String employeeId,
                  String personName, String department) {
        this.personId = personId;
        this.employeeId = employeeId;
        this.personName = personName;
        this.department = department;
    }

    public int getPersonId() { return personId; }
    public String getEmployeeId() { return employeeId; }
    public String getPersonName() { return personName; }
    public String getDepartment() { return department; }

    @Override
    public String toString() {
        return employeeId + " - " + personName + " (" + department + ")";
    }
}