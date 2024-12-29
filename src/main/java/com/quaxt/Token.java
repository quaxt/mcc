package com.quaxt;

import java.util.EnumMap;

public record Token(TokenType type, String value) {
    static EnumMap<TokenType, Token> fixed = new EnumMap<>(TokenType.class);

    static {
        for (TokenType t:TokenType.values()){
            if (!t.hasValue()){
                fixed.put(t, new Token(t, null));
            }
        }

    }

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
                case "return": return fixed.get(TokenType.RETURN);
                case "int": return fixed.get(TokenType.INT);
                case "void": return fixed.get(TokenType.VOID);
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
}
