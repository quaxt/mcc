package com.quaxt.mcc.asm;

import com.quaxt.mcc.UnaryOperator;

public record Unary(UnaryOperator op, Operand operand) implements Instruction {
}
