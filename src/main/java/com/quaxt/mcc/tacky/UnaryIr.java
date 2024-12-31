package com.quaxt.mcc.tacky;

import com.quaxt.mcc.Op;

public record UnaryIr(Op op, ValIr src, ValIr dst) implements InstructionIr {

}
