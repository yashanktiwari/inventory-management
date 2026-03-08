package com.inventory.model;

import java.time.LocalDateTime;

public class AuditEntry {

    private String modifiedBy;
    private LocalDateTime modifiedAt;
    private String fieldName;
    private String oldValue;
    private String newValue;

    public AuditEntry(String modifiedBy,
                      LocalDateTime modifiedAt,
                      String fieldName,
                      String oldValue,
                      String newValue) {

        this.modifiedBy = modifiedBy;
        this.modifiedAt = modifiedAt;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getModifiedBy() { return modifiedBy; }
    public LocalDateTime getModifiedAt() { return modifiedAt; }
    public String getFieldName() { return fieldName; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
}