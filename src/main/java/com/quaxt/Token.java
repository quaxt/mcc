package com.quaxt;

import java.util.EnumMap;

public record Token(TokenType type, String value) {
    static EnumMap<TokenType, Token> fixed = new EnumMap<>(TokenType.class);

    static {
        for (TokenType t : TokenType.values()) {
            if (!t.hasValue()) {
                fixed.put(t, new Token(t, null));
            }
        }

    }

    public static final Token RETURN = fixed.get(TokenType.RETURN);
    public static final Token INT = fixed.get(TokenType.INT);
    public static final Token VOID = fixed.get(TokenType.VOID);
    public static final Token SEMICOLON = fixed.get(TokenType.SEMICOLON);
    public static final Token OPEN_PAREN = fixed.get(TokenType.OPEN_PAREN);
    public static final Token CLOSE_PAREN = fixed.get(TokenType.CLOSE_PAREN);
    public static final Token OPEN_BRACE = fixed.get(TokenType.OPEN_BRACE);
    public static final Token CLOSE_BRACE = fixed.get(TokenType.CLOSE_BRACE);

    public static Token of(TokenType tokenType) {
        if (tokenType == null) throw new IllegalArgumentException("TokenType must not be null");
        Token t = fixed.get(tokenType);
        if (t != null) {
            return t;
        }
        throw new IllegalArgumentException(tokenType + " requires value");
    }

    public static Token of(TokenType tokenType, String value) {
        if (tokenType == TokenType.IDENTIFIER) {
            switch (value) {
                case "return":
                    return RETURN;
                case "int":
                    return INT;
                case "void":
                    return VOID;
            }
        }
        return new Token(tokenType, value);
    }

    public String toString() {
        if (type.hasValue()) {
            return type.toString() + "=" + value;
        }
        return type.toString();

    }

    public boolean isType() {
        return TokenType.IDENTIFIER == type || TokenType.INT == type || TokenType.VOID == type;
    }
}
