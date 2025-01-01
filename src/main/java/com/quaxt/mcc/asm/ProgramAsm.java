package com.quaxt.mcc.asm;

import com.quaxt.mcc.UnaryOperator;

import java.io.PrintWriter;
import java.util.List;

public record ProgramAsm(FunctionAsm functionAsm) {
    private static void printIndent(PrintWriter out, String s) {
        out.println("\t" + s);
    }

    private static String formatOperand(Operand o) {
        return switch (o) {
            case Imm(int i) -> "$" + i;
            case Pseudo _ ->
                    throw new IllegalArgumentException("broken compiler error: pseudo instructions should not occur here");
            case Reg reg -> "%" + reg;
            case Stack(int offset) -> offset + "(%rbp)";
        };
    }

    public void emitAsm(PrintWriter out) {
        String name = functionAsm.name();
        out.println("                .text");
        out.println("                .globl	" + name);
        out.println(name + ":");
        List<Instruction> instructions = functionAsm.instructions();
        printIndent(out, "pushq\t%rbp");
        printIndent(out, "movq\t%rsp, %rbp");
        for (Instruction instruction : instructions) {
            String s = switch (instruction) {
                case AllocateStack(int i) -> "subq\t$" + i + ", %rsp";
                case Mov(Operand src, Operand dst) ->
                        formatMov(dst) + "\t" + formatOperand(src) + ", " + formatOperand(dst);
                case Nullary.RET -> {
                    printIndent(out, "movq\t%rbp, %rsp");
                    printIndent(out, "popq\t%rbp");
                    yield "ret";
                }
                case Unary(UnaryOperator op, Operand operand) -> op.toString().toLowerCase() + "l\t" + formatOperand(operand);
            };
            printIndent(out, s);
        }

        out.println("                .ident	\"GCC: (Ubuntu 11.4.0-1ubuntu1~22.04) 11.4.0\"");
        out.println("                .section	.note.GNU-stack,\"\",@progbits");
    }

    private static String formatMov(Operand o) {
        return "mov" + (o == Reg.EAX ? 'l' : 'q');
    }
}
