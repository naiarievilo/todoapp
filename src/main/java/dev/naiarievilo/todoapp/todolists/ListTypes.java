package dev.naiarievilo.todoapp.todolists;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ListTypes {
    INBOX("inbox"),
    CALENDAR("calendar"),
    CUSTOM("custom");

    private final String type;

    ListTypes(String type) {
        this.type = type;
    }

    @JsonValue
    public String getType() { return type; }

    @Override
    public String toString() {
        return this.name().charAt(0) + this.name().substring(1).toLowerCase();
    }
}
