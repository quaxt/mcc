package com.quaxt.asm;

import java.util.List;

public record FunctionAsm(String name, List<Instruction> instructions) implements AsmNode {
}

