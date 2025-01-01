package com.quaxt.mcc.parser;

sealed public interface Exp permits BinaryOp, Constant, UnaryOp {
}

