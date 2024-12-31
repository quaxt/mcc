package com.quaxt.mcc.parser;


import com.quaxt.mcc.Op;
import com.quaxt.mcc.Token;
import com.quaxt.mcc.TokenType;

import java.util.List;

public class Parser {

    private static void expect(Token expected, List<Token> tokens) {
        Token token = tokens.removeFirst();
        if (expected != token) {
            throw new IllegalArgumentException("Expected " + expected + ", got " + token);
        }
    }

    static Statement parseStatement(List<Token> tokens) {
        expect(Token.RETURN, tokens);
        Exp exp = parseExpr(tokens);
        expect(Token.SEMICOLON, tokens);
        return new Return(exp);
    }

    public static Program parseProgram(List<Token> tokens) {
        Function function = parseFunction(tokens);
        return new Program(function);
    }

    private static Token parseType(List<Token> tokens) {
        Token type = tokens.removeFirst();
        if (!type.isType()) {
            throw new IllegalArgumentException("Expected type, got " + type);
        }
        return type;
    }

    private static String parseIdentifier(List<Token> tokens) {
        Token identifier = tokens.removeFirst();
        if (identifier.type() != TokenType.IDENTIFIER) {
            throw new IllegalArgumentException("Expected identifier, got " + identifier);
        }
        return identifier.value();
    }

    private static Function parseFunction(List<Token> tokens) {
        Token returnType = parseType(tokens);
        String name = parseIdentifier(tokens);
        expect(Token.OPEN_PAREN, tokens);
        expect(Token.VOID, tokens);
        expect(Token.CLOSE_PAREN, tokens);
        expect(Token.OPEN_BRACE, tokens);
        Statement statement = parseStatement(tokens);
        expect(Token.CLOSE_BRACE, tokens);

        return new Function(name, returnType, statement);
    }


    private static Exp parseExpr(List<Token> tokens) {
        Token token = tokens.removeFirst();
        if (TokenType.NUMERIC == token.type()) {
            return new Int(Integer.parseInt(token.value()));
        } else if (TokenType.NEGATE == token.type()) {
            return new UnaryOp(Op.NEGATE, parseExpr(tokens));
        } else if (TokenType.COMPLIMENT == token.type()) {
            return new UnaryOp(Op.COMPLEMENT, parseExpr(tokens));
        } else if (TokenType.OPEN_PAREN == token.type()) {
            Exp r = parseExpr(tokens);
            expect(Token.CLOSE_PAREN, tokens);
            return r;
        }
        throw new IllegalArgumentException("Expected exp, got " + token);

    }

}
