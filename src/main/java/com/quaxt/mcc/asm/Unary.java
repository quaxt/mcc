package com.quaxt.mcc.asm;

import com.quaxt.mcc.Op;

public record Unary(Op op, Operand operand) implements Instruction {
}
