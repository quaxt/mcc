package com.quaxt;

import java.util.regex.Pattern;

public enum TokenType {
    IDENTIFIER("[a-zA-Z_]\\w*\\b"),
    OPEN_PAREN("\\("), CLOSE_PAREN("\\)"),
    OPEN_BRACE("\\{"), CLOSE_BRACE("\\}"), NUMERIC("[0-9]+\\b"), SEMICOLON(";"), SINGLE_LINE_COMMENT("//.*"),
    MULTILINE_COMMENT(Pattern.compile("/\\*.*\\*/", Pattern.DOTALL)),
    INT("int"),RETURN("return"),VOID("void"),DECREMENT("--"),MINUS("-"),NEGATE("~");

    Pattern regex;


    TokenType(String pattern) {
        regex = Pattern.compile(pattern);
    }

    TokenType(Pattern pattern) {
        regex = pattern;
    }


    public boolean hasValue() {
        return this == IDENTIFIER || this == NUMERIC;
    }

    public boolean isComment() {
        return this == SINGLE_LINE_COMMENT || this == MULTILINE_COMMENT;
    }
}
