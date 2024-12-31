package com.quaxt.mcc.tacky;

import com.quaxt.mcc.Token;

import java.util.List;

public record FunctionIr(String name, Token returnType, List<InstructionIr> statement) {
}
