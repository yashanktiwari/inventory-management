package com.inventory.model;

import java.time.LocalDateTime;

public class WorkLog {

    private int id;
    private String username;
    private WorkType workType;
    private String details;
    private LocalDateTime createdAt;

    public WorkLog() {}

    public WorkLog(int id, String username, WorkType workType, String details, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.workType = workType;
        this.details = details;
        this.createdAt = createdAt;
    }

    public WorkLog(String username, WorkType workType, String details) {
        this.username = username;
        this.workType = workType;
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public WorkType getWorkType() {
        return workType;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setWorkType(WorkType workType) {
        this.workType = workType;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}