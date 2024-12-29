package com.quaxt.asm;

import com.quaxt.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Codegen {
    public static ProgramAsm codeGenProgram(Program program) {
        ProgramAsm prog = new ProgramAsm(codeGenFunction(program.function()));
        return prog;
    }

    private static FunctionAsm codeGenFunction(Function function) {

        return new FunctionAsm(function.name(), codeGenInstructions(function.statement()));
    }

    private static List<Instruction> codeGenInstructions(Node statement) {
        if (statement instanceof Return r) {
            List<Instruction> instructions = new ArrayList<>();
            instructions.addAll(codeGenExpr(r.expr()));
            instructions.add(new ReturnAsm());
            return instructions;
        } else throw new IllegalArgumentException("not done: Expr");

    }

    private static List<Instruction> codeGenExpr(Expr expr) {
        if (expr instanceof Int i) {
            List<Instruction> instructions = new ArrayList<>();
            instructions.add(new Mov(i.i()));
            return instructions;
        } else throw new IllegalArgumentException("not done: Expr");

    }

    public static void main(String[] args) throws IOException {
        Program prog = Parser.parseProgram(Lexer.lex(Files.readString(Paths.get("return_2.c"))));
        ProgramAsm progAsm = Codegen.codeGenProgram(prog);
        System.out.println(progAsm);
    }
}
