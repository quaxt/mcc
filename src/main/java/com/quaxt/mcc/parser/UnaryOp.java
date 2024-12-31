package com.quaxt.mcc.parser;

import com.quaxt.mcc.Op;

public record UnaryOp(Op op, Exp exp) implements Exp {
}