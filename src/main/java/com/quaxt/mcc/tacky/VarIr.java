package com.quaxt.mcc.tacky;

import java.util.concurrent.atomic.AtomicLong;

public record VarIr(String identifier) implements ValIr{
    static AtomicLong tempCount = new AtomicLong(0L);
    public static VarIr newTemprary() {
        return new VarIr("temp"+tempCount.getAndIncrement());
    }
}
