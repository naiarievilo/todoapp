package dev.naiarievilo.todoapp.todolists;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ListTypes {
    INBOX("inbox"),
    CALENDAR("calendar"),
    PERSONALIZED("custom");

    private final String type;

    ListTypes(String type) {
        this.type = type;
    }

    @JsonValue
    public String getType() { return type; }
}
