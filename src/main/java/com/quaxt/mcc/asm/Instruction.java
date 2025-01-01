package com.quaxt.mcc.asm;

import java.io.PrintWriter;

public sealed interface Instruction permits AllocateStack, Mov, Nullary, Unary {


}
