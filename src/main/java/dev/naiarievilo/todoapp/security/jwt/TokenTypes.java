package dev.naiarievilo.todoapp.security.jwt;

public enum TokenTypes {
    USER_VERIFICATION(1),
    USER_ACCESS(2),
    REFRESH_ACCESS(3),
    USER_UNLOCKING(4),
    USER_ENABLING(5);

    private final int type;

    TokenTypes(int type) {
        this.type = type;
    }

    public int value() {
        return type;
    }
}
