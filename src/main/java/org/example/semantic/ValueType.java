package org.example.semantic;

public enum ValueType {
    NUMBER,
    STRING,
    BOOLEAN,
    UNKNOWN,
    ERROR;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
