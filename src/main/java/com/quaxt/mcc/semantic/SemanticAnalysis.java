package com.quaxt.mcc.semantic;

import com.quaxt.mcc.BinaryOperator;
import com.quaxt.mcc.Mcc;
import com.quaxt.mcc.UnaryOperator;
import com.quaxt.mcc.parser.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SemanticAnalysis {


    public static Program loopLabelProgram(Program program) {
        return new Program(loopLabelFunction(program.function()));
    }

    private static Function loopLabelFunction(Function function) {
        return new Function(function.name(),
                function.returnType(),
                loopLabelStatement(function.block(), null));

    }

    private static <T extends Statement> T loopLabelStatement(T statement, String currentLabel) {
        return switch (statement) {
            case null -> null;
            case Block block -> {//update the blockItems in-place
                ArrayList<BlockItem> blockItems = block.blockItems();
                blockItems.replaceAll(blockItem -> switch (blockItem) {
                    case Declaration declaration ->
                            loopLabelDeclaration(declaration, currentLabel);
                    case Statement innerStatement ->
                            loopLabelStatement(innerStatement, currentLabel);
                });
                yield (T) block;
            }
            case Break aBreak -> {
                if (currentLabel == null) {
                    fail("break statement outside of loop");
                }
                aBreak.label = currentLabel;
                yield statement;
            }
            case Compound compound ->
                    throw new RuntimeException("todo: get rid of compound");
            case Continue aContinue -> {
                if (currentLabel == null) {
                    fail("continue statement outside of loop");
                }
                aContinue.label = currentLabel;
                yield statement;
            }
            case DoWhile(Statement body, Exp condition, String _) -> {
                String newLabel = Mcc.makeTemporary("do");
                yield (T) new DoWhile(loopLabelStatement(body, newLabel), condition, newLabel);

            }
            case Exp exp -> statement;
            case For(
                    ForInit init, Exp condition, Exp post, Statement body,
                    String _
            ) -> {
                String newLabel = Mcc.makeTemporary("for");
                ForInit labeledForInit = switch (init) {
                    case null -> null;
                    case Declaration declaration ->
                            loopLabelDeclaration(declaration, newLabel);
                    case Exp exp -> loopLabelStatement(exp, newLabel);
                };
                Exp labeledCondition = loopLabelStatement(condition, newLabel);
                Exp labelledPost = loopLabelStatement(post, newLabel);
                Statement labelledBody = loopLabelStatement(body, newLabel);
                yield (T) new For(labeledForInit, labeledCondition, labelledPost, labelledBody, newLabel);


            }
            case If(Exp condition, Statement ifTrue, Statement ifFalse) ->
                    (T) new If(condition, loopLabelStatement(ifTrue, currentLabel),
                            loopLabelStatement(ifFalse, currentLabel));
            case NullStatement nullStatement -> statement;
            case Return(Exp exp) ->
                    (T) new Return(loopLabelStatement(exp, currentLabel));
            case While(Exp condition, Statement body, String _) -> {
                String newLabel = Mcc.makeTemporary("while");
                yield (T) new While(condition, loopLabelStatement(body, newLabel), newLabel);

            }
        };
    }

    private static Declaration loopLabelDeclaration(Declaration declaration, String currentLabel) {
        Exp init = declaration.init();
        return new Declaration(declaration.name(), loopLabelStatement(init, currentLabel));
    }


    record Entry(String name, boolean fromCurrentBlock) {
    }

    public static Program resolveVars(Program program) {
        Map<String, Entry> variableMap = new HashMap<>();
        return new Program(resolveVars(program.function(), variableMap));
    }

    private static Block resolveBlock(Block block, Map<String, Entry> variableMap) {
        Map<String, Entry> newVariableMap = copyVariableMap(variableMap);
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        for (BlockItem i : block.blockItems()) {
            blockItems.add(resolveVarsBlockItem(i, newVariableMap));
        }
        return new Block(blockItems);
    }

    private static Function resolveVars(Function function, Map<String, Entry> variableMap) {
        return new Function(function.name(), function.returnType(),
                resolveBlock(function.block(), variableMap));
    }

    private static BlockItem resolveVarsBlockItem(BlockItem blockItem, Map<String, Entry> variableMap) {
        return switch (blockItem) {
            case Declaration declaration ->
                    resolveDeclaration(declaration, variableMap);
            case Statement statement ->
                    resolveStatement(statement, variableMap);
        };
    }

    private static Statement resolveStatement(Statement blockItem, Map<String, Entry> variableMap) {
        return switch (blockItem) {
            case null -> null;
            case Exp exp -> resolveExp(exp, variableMap);
            case Return(Exp exp) -> new Return(resolveExp(exp, variableMap));
            case If(
                    Exp condition, Statement ifTrue, Statement ifFalse
            ) ->
                    new If(resolveExp(condition, variableMap), resolveStatement(ifTrue, variableMap),
                             resolveStatement(ifFalse, variableMap));

            case Compound compound ->
                    new Compound(resolveBlock(compound.block(), variableMap));
            case Block block -> resolveBlock(block, variableMap);
            case NullStatement nullStatement -> nullStatement;
            case Break _, Continue _ -> blockItem;
            case DoWhile(Statement body, Exp condition, String label) ->
                    new DoWhile(resolveStatement(body, variableMap),
                            resolveExp(condition, variableMap), label);
            case For(
                    ForInit init, Exp condition, Exp post, Statement body,
                    String label
            ) -> {
                Map<String, Entry> newVariableMap = copyVariableMap(variableMap);
                yield new For(resolveForInit(init, newVariableMap),
                        resolveExp(condition, newVariableMap), resolveExp(post, newVariableMap),
                        resolveStatement(body, newVariableMap), label);
            }
            case While(Exp condition, Statement body, String label) ->
                    new While(resolveExp(condition, variableMap),
                            resolveStatement(body, variableMap), label);
        };

    }

    private static ForInit resolveForInit(ForInit init, Map<String, Entry> variableMap) {
        return switch (init) {
            case Declaration declaration ->
                    resolveDeclaration(declaration, variableMap);
            case Exp exp -> resolveExp(exp, variableMap);
            case null -> null;
        };
    }


    private static Map<String, Entry> copyVariableMap(Map<String, Entry> m) {
        Map<String, Entry> copy = HashMap.newHashMap(m.size());
        for (Map.Entry<String, Entry> e : m.entrySet()) {
            Entry v = e.getValue();
            copy.put(e.getKey(), new Entry(v.name(), false));
        }
        return copy;
    }

    private static Declaration resolveDeclaration(Declaration d, Map<String, Entry> variableMap) {
        if (variableMap.get(d.name()) instanceof Entry e && e.fromCurrentBlock()) {
            fail("Duplicate variable declaration");
        }
        String uniqueName = Mcc.makeTemporary(d.name() + ".");
        variableMap.put(d.name(), new Entry(uniqueName, true));
        Exp init = d.init();
        return new Declaration(uniqueName, resolveExp(init, variableMap));
    }

    private static Exp resolveExp(Exp exp, Map<String, Entry> variableMap) {
        return switch (exp) {
            case null -> null;
            case Assignment(Exp left, Exp right) ->
                    left instanceof Var v ? new Assignment(resolveExp(v, variableMap), resolveExp(right, variableMap)) : fail("Invalid lvalue");
            case BinaryOp(BinaryOperator op, Exp left, Exp right) ->
                    new BinaryOp(op, resolveExp(left, variableMap), resolveExp(right, variableMap));
            case Constant constant -> constant;
            case UnaryOp(UnaryOperator op, Exp arg) ->
                    new UnaryOp(op, resolveExp(arg, variableMap));
            case Var(String name) ->
                    variableMap.get(name) instanceof Entry e ? new Var(e.name()) : fail("Undeclared variable");
            case Conditional(Exp condition, Exp ifTrue, Exp ifFalse) ->
                    new Conditional(resolveExp(condition, variableMap), resolveExp(ifTrue, variableMap), resolveExp(ifFalse, variableMap));
        };
    }

    private static Exp fail(String s) {
        throw new RuntimeException(s);
    }
}
