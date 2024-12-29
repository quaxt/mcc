package com.quaxt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Mcc {

    enum Mode {LEX, PARSE, CODEGEN, CODE_EMIT}

    public static void main(String[] args0) throws IOException {
        ArrayList<String> args = Arrays.stream(args0)
                .collect(Collectors.toCollection(ArrayList::new));
        Mode mode = Mode.CODE_EMIT;
        boolean emitAssembly = false;
        for (int i = args.size() - 1; i >= 0; i--) {
            Mode newMode = switch (args.get(i)) {
                case "--lex" -> Mode.LEX;
                case "--parse" -> Mode.PARSE;
                case "--codegen" -> Mode.CODEGEN;
                default -> null;
            };
            if (newMode == null) {
                if ("-S".equals(args.get(i))) {
                    emitAssembly = true;
                    args.remove(i);
                }
            } else {
                mode = newMode;
                args.remove(i);
            }
        }
        String srcFile = args.getFirst();
        System.out.println("mode=" + mode + "\nemitAssembly=" + emitAssembly + "\nsrcFile=" + srcFile);
        List<Token> l = Lexer.lex(Files.readString(Path.of(srcFile)));
    }
}
