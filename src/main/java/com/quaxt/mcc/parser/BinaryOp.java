package com.quaxt.mcc.parser;

public record BinaryOp(BinaryOperator op, Exp left, Exp right) implements Exp {
}