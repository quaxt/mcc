package com.quaxt.mcc.parser;

import com.quaxt.mcc.UnaryOperator;

public record UnaryOp(UnaryOperator op, Exp exp) implements Exp {
}