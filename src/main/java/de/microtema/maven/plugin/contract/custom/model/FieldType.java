package de.microtema.maven.plugin.contract.custom.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FieldType {

    TEXT, BOOLEAN, NUMBER, DATE;

    private String value;

    FieldType() {
        this.value = name().toLowerCase();
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
