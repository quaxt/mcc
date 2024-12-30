package com.quaxt;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    static Pattern WHITESPACE = Pattern.compile("\\s+");

    public static List<Token> lex(String src) {
        TokenType[] tokenTypes = TokenType.values();
        Matcher matcher = TokenType.IDENTIFIER.regex.matcher(src);
        List<Token> tokens = new ArrayList<>();
        outer:
        for (int i = 0; i < src.length(); ) {
            matcher.usePattern(WHITESPACE);
            matcher.region(i, src.length());
            if (matcher.lookingAt()) {
                int end = matcher.end();
                matcher.region(end, src.length());
                i = end;
                if (i == src.length()) {
                    break;
                }
            }
            for (TokenType tokenType : tokenTypes) {
                matcher.usePattern(tokenType.regex);
                if (matcher.lookingAt()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    if (!tokenType.isComment()){
                        if (tokenType.hasValue()) {
                            tokens.add(Token.of(tokenType, src.substring(start, end)));
                        } else {
                            tokens.add(Token.of(tokenType));
                        }
                    }

                    i = end;
                    continue outer;
                }
            }
            throw new IllegalArgumentException("can't handle token at " + src.substring(i));
        }
        return tokens;
    }
}
