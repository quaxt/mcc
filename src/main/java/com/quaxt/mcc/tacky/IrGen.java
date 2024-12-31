package com.quaxt.mcc.tacky;

import com.quaxt.mcc.parser.*;

import java.util.ArrayList;
import java.util.List;


public class IrGen {
    public static ProgramIr programIr(Program program) {
        List<InstructionIr> instructions = new ArrayList<>();
        emitInstructions(program.function(), instructions);
        //
        FunctionIr f = new FunctionIr(program.function().name(), program.function().returnType(), instructions);
        return new ProgramIr(f);
    }

    private static void emitInstructions(Function function, List<InstructionIr> instructions) {
        emitInstructions(function.statement(), instructions);
        FunctionIr f = new FunctionIr(function.name(), function.returnType(), instructions);

    }

    private static void emitInstructions(Statement statement, List<InstructionIr> instructions) {
        switch (statement) {
            case Return r: {
                ValIr retVal = emitInstructions(r.exp(), instructions);
                ReturnInstructionIr ret = new ReturnInstructionIr(retVal);
                instructions.add(ret);
            }
        }
    }

    private static ValIr emitInstructions(Exp exp, List<InstructionIr> instructions) {
        switch (exp) {
            case Constant c: {
                return switch (c) {
                    case Int(int i)  -> new IntIr(i);
                };
            }
            case UnaryOp unaryOp: {
                ValIr src = emitInstructions(unaryOp.exp(), instructions);
                VarIr dst = makeTemporary();
                instructions.add(new UnaryIr(unaryOp.op(), src, dst));
                return dst;

            }
        }
    }

    private static VarIr makeTemporary() {
        return VarIr.newTemprary();
    }

}
