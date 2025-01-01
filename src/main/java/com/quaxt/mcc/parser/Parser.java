package com.quaxt.mcc.parser;


import com.quaxt.mcc.Lexer;
import com.quaxt.mcc.UnaryOperator;
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
        Exp exp = parseExp(tokens, 0);
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


    private static Exp parseFactor(List<Token> tokens) {
        Token token = tokens.removeFirst();
        if (TokenType.NUMERIC == token.type()) {
            return new Int(Integer.parseInt(token.value()));
        } else if (TokenType.MINUS == token.type()) {
            return new UnaryOp(UnaryOperator.NEG, parseFactor(tokens));
        } else if (TokenType.COMPLIMENT == token.type()) {
            return new UnaryOp(UnaryOperator.NOT, parseFactor(tokens));
        } else if (TokenType.OPEN_PAREN == token.type()) {
            Exp r = parseExp(tokens, 0);
            expect(Token.CLOSE_PAREN, tokens);
            return r;
        }
        throw new IllegalArgumentException("Expected exp, got " + token);
    }


    private static Exp parseExp(List<Token> tokens, int minPrec) {
        Exp left = parseFactor(tokens);
        if (tokens.isEmpty()){
         return left;
        }
        Token nextToken = tokens.getFirst();
        while (nextToken.type().isBinaryOperator() && getPrecedence(toBinaryOperator(nextToken.type())) >= minPrec) {
            BinaryOperator operator = parseBinaryOperator(tokens);
            Exp right = parseExp(tokens, getPrecedence(operator) + 1);
            left = new BinaryOp(operator, left, right);
            if (tokens.isEmpty()){
                break;
            }
            nextToken = tokens.getFirst();
        }
        return left;
    }

    private static BinaryOperator parseBinaryOperator(List<Token> tokens) {
        Token t = tokens.removeFirst();
        TokenType type = t.type();
        return toBinaryOperator(type);
    }

    private static BinaryOperator toBinaryOperator(TokenType type) {
        return switch (type) {
            case MINUS -> BinaryOperator.SUBTRACT;
            case PLUS -> BinaryOperator.ADD;
            case MULTIPLY -> BinaryOperator.MULTIPLY;
            case DIVIDE -> BinaryOperator.DIVIDE;
            case REMAINDER -> BinaryOperator.REMAINDER;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    private static int getPrecedence(BinaryOperator operator) {
        return switch (operator) {
            case SUBTRACT, ADD -> 45;
            case MULTIPLY, DIVIDE, REMAINDER -> 50;
            default -> throw new IllegalStateException("Unexpected value: " + operator);
        };
    }


    public static void main(String[] args) {
        List<Token> tokens = Lexer.lex("1 * 2 - 3 * (4 + 5)");
        Exp foo = parseExp(tokens, 0);
        System.out.println(foo);
    }

}
