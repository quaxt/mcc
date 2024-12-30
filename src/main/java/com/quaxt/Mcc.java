package com.quaxt;

import com.quaxt.asm.Codegen;
import com.quaxt.asm.ProgramAsm;
import com.quaxt.parser.Parser;
import com.quaxt.parser.Program;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Mcc {

    enum Mode {LEX, PARSE, CODEGEN, COMPILE, ASSEMBLE}

    public static int preprocess(Path cFile, Path iFile) throws IOException, InterruptedException {
        ProcessBuilder pb =
                new ProcessBuilder("gcc", "-E", "-P", cFile.toString(), "-o", iFile.toString()).inheritIO();
        return pb.start().waitFor();
    }


    public static int assemble(Path asmFile, Path outputFile) throws IOException, InterruptedException {
        ProcessBuilder pb =
                new ProcessBuilder("gcc", asmFile.toString(), "-o", outputFile.toString()).inheritIO();
        return pb.start().waitFor();
    }

    public static void main(String[] args0) throws Exception {
        ArrayList<String> args = Arrays.stream(args0)
                .collect(Collectors.toCollection(ArrayList::new));
        Mode mode = Mode.ASSEMBLE;
        for (int i = args.size() - 1; i >= 0; i--) {
            Mode newMode = switch (args.get(i)) {
                case "--lex" -> Mode.LEX;
                case "--parse" -> Mode.PARSE;
                case "--codegen" -> Mode.CODEGEN;
                case "-S" -> Mode.COMPILE;
                default -> null;
            };
            if (newMode != null) {
                mode = newMode;
                args.remove(i);
            }
        }
        Path srcFile = Path.of(args.getFirst());
        String bareFileName = removeEnding(srcFile.getFileName().toString());
        Path intermediateFile = srcFile.resolveSibling(bareFileName + ".i");

        int preprocessExitCode = preprocess(srcFile, intermediateFile);
        if (preprocessExitCode != 0) {
            System.exit(preprocessExitCode);
        }
        List<Token> l = Lexer.lex(Files.readString(intermediateFile));
        Files.delete(intermediateFile);
        if (mode == Mode.LEX) {
            return;
        }
        Program program = Parser.parseProgram(l);
        if (!l.isEmpty()) {
            throw new IllegalArgumentException("Unexpected token " + l.getFirst());
        }
        if (mode == Mode.PARSE) {
            return;
        }

        ProgramAsm programAsm = Codegen.codeGenProgram(program);
        if (mode == Mode.CODEGEN) {
            return;
        }
        Path asmFile = srcFile.resolveSibling(bareFileName + ".s");
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(asmFile))) {
            programAsm.emitAsm(pw);
            pw.flush();
        }

        if (mode == Mode.COMPILE) {
            return;
        }
        int exitCode = assemble(asmFile, intermediateFile.resolveSibling(bareFileName));
        Files.delete(asmFile);
        System.exit(exitCode);
    }

    private static String removeEnding(String fileName) {
        String ending = ".c";
        if (fileName.endsWith(ending)) {
            return fileName.substring(0, fileName.length() - ending.length());
        }
        throw new IllegalArgumentException(fileName + " does not have ending " + ending);
    }
}
