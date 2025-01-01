package com.quaxt.mcc.asm;

import java.io.PrintWriter;
import java.util.List;

public record ProgramAsm(FunctionAsm functionAsm) {
    public void emitAsm(PrintWriter out) {
        String name = functionAsm.name();
        out.println("                .text");
        out.println("                .globl	" + name);
        out.println(name + ":");
        List<Instruction> instructions = functionAsm.instructions();

        for (Instruction instruction : instructions) {
            instruction.emitAsm(out);
        }

        out.println("                .ident	\"GCC: (Ubuntu 11.4.0-1ubuntu1~22.04) 11.4.0\"");
        out.println("                .section	.note.GNU-stack,\"\",@progbits");

    }
}
