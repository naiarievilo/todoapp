package dev.naiarievilo.todoapp.security.jwt;

public enum TokenTypes {
    USER_VERIFICATION(1),
    USER_ACCESS(2),
    USER_UNLOCKING(3),
    USER_ENABLING(4);

    private final int type;

    TokenTypes(int type) {
        this.type = type;
    }

    public int value() {
        return type;
    }
}
