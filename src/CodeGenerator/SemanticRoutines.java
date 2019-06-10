package CodeGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SemanticRoutines {
    static void call(String routineName, CodeGenerator codeGenerator) {
        switch (routineName) {
            case "#pinput":
                pushInput(codeGenerator);
                break;
            case "#declare_var":
                declareVariable(codeGenerator);
                break;
            case "#declare_array":
                declareArray(codeGenerator);
                break;
            case "#pid":
                pushId(codeGenerator);
                break;
            case "#assign":
                assign(codeGenerator);
                break;
            case "#calc_cell":
                calculateArrayAddress(codeGenerator);
                break;
            case "#calcop":
                calculateOperation(codeGenerator);
                break;
            case "#pnum":
                pushNumber(codeGenerator);
                break;
            case "#calc_signed_factor":
                calculateSignedFactor(codeGenerator);
                break;
            case "#save":
                save(codeGenerator);
                break;
            case "#jpf_save":
                jumpFalseSave(codeGenerator);
                break;
            case "#jp":
                jump(codeGenerator);
                break;
            case "#end_while":
                endWhile(codeGenerator);
                break;
            case "#continue":
                continueWhile(codeGenerator);
                break;
            case "#break":
                breakWhileSwitch(codeGenerator);
                break;
            case "#check_case":
                checkCase(codeGenerator);
                break;
            case "#jpf_case":
                jumpFalseCase(codeGenerator);
                break;
            case "#end_switch":
                endSwitch(codeGenerator);
                break;
            case "#ptemp":
                pushTemp(codeGenerator);
                break;
            case "#case_undone_true":
                caseUndoneTrue(codeGenerator);
                break;
            case "#case_undone_false":
                caseUndoneFalse(codeGenerator);
                break;
            case "#pfunkeyword":
                pushFunctionKeyword(codeGenerator);
                break;
            case "#declare_fun":
                declareFunction(codeGenerator);
                break;
            case "#function_call":
                functionCall(codeGenerator);
                break;
            case "#return_expression":
                returnExpression(codeGenerator);
                break;
            case "#return_void":
                returnVoid(codeGenerator);
                break;
            case "#end_function":
                endFunction(codeGenerator);
                break;
            case "#init":
                init(codeGenerator);
                break;
            case "#finalize":
                finalize(codeGenerator);
                break;
            case "#pop":
                codeGenerator.semanticStack.pop();
                break;
            default:
                System.err.printf("No semantic routine found for '%s'\n", routineName);
        }
//        System.out.println(codeGenerator.semanticStack.size() + routineName);
    }

    private static void finalize(CodeGenerator codeGenerator) {
        for (SymbolTableEntry entry : codeGenerator.symbolTable) {
            if (entry.lexeme.equals("main") && entry.type.equals(SymbolTableEntry.TypeSpecifier.VOID)) {
                codeGenerator.programBlock.set(0, String.format("(JP, %d, , )", entry.attribute.jumpAddress));
                codeGenerator.programBlock.set(1, String.format("(JP, %d, , )", codeGenerator.programBlock.size()));
                return;
            }
        }

        codeGenerator.errors.add(String.format("%d: main function not found!", codeGenerator.lexer.getLineNumber()));
    }

    private static void init(CodeGenerator codeGenerator) {
        codeGenerator.programBlock.add("");
        codeGenerator.programBlock.add("");

        codeGenerator.symbolTable.add(new SymbolTableEntry(0, "output",
                SymbolTableEntry.TypeSpecifier.VOID,
                new SymbolTableAttribute(1, codeGenerator.dataBlockAddress, 2)));
        codeGenerator.programBlock.add(String.format("(PRINT, %s, , )", codeGenerator.dataBlockAddress));
        codeGenerator.programBlock.add(String.format("(JP, @%s, , )",
                codeGenerator.symbolTable.get(0).attribute.getReturnAddress()));
        codeGenerator.semanticStack.push("#function");
        codeGenerator.semanticStack.push("0");
        codeGenerator.dataBlockAddress += 12;
    }

    private static void endFunction(CodeGenerator codeGenerator) {
        SymbolTableEntry entry = codeGenerator.symbolTable.get(Integer.valueOf(codeGenerator.semanticStack.pop()));
        codeGenerator.semanticStack.pop(); // #declare_function (#function)
        if (!(entry.lexeme.equals("main") && entry.type.equals(SymbolTableEntry.TypeSpecifier.VOID)))
            codeGenerator.programBlock.add(String.format("(JP, @%s, , )", entry.attribute.getReturnAddress()));
        else
            codeGenerator.programBlock.add(String.format("(JP, %d, , )", 1));
    }

    private static void returnVoid(CodeGenerator codeGenerator) {
        SymbolTableEntry entry = codeGenerator.symbolTable.get(Integer.valueOf(
                codeGenerator.semanticStack.elementAt(getStackPointer(codeGenerator, "#function") + 1)));
        if (!(entry.lexeme.equals("main") && entry.type.equals(SymbolTableEntry.TypeSpecifier.VOID)))
            codeGenerator.programBlock.add(String.format("(JP, @%s, , )", entry.attribute.getReturnAddress()));
        else
            codeGenerator.programBlock.add(String.format("(JP, %d, , )", 1));
    }

    private static void returnExpression(CodeGenerator codeGenerator) {
        SymbolTableEntry entry = codeGenerator.symbolTable.get(Integer.valueOf(
                codeGenerator.semanticStack.elementAt(getStackPointer(codeGenerator, "#function") + 1)));
        codeGenerator.programBlock.add(String.format("(ASSIGN, %s, %d, )", codeGenerator.semanticStack.pop(),
                entry.attribute.getOutputAddress()));
        codeGenerator.programBlock.add(String.format("(JP, @%s, , )", entry.attribute.getReturnAddress()));
    }

    private static void functionCall(CodeGenerator codeGenerator) {
        List<String> arguments = new ArrayList<>();
        while (!codeGenerator.semanticStack.peek().equals("#function")) {
            arguments.add(codeGenerator.semanticStack.pop());
        }
        codeGenerator.semanticStack.pop();

        SymbolTableEntry entry = codeGenerator.symbolTable.get(Integer.valueOf(codeGenerator.semanticStack.pop()));
        if (arguments.size() != entry.attribute.argumentNumber)
            codeGenerator.errors.add(String.format("%d: Mismatch in numbers of arguments of '%s'",
                    codeGenerator.lexer.getLineNumber(), entry.lexeme));

        for (int i = 0; i < arguments.size(); i++) {
            codeGenerator.programBlock.add(String.format("(ASSIGN, %s, %d, )", arguments.get(i),
                    entry.attribute.dataBlockAddress + i * 4));
        }

        codeGenerator.programBlock.add(String.format("(ASSIGN, #%s, %s, )", codeGenerator.programBlock.size() + 2,
                entry.attribute.getReturnAddress()));
        codeGenerator.programBlock.add(String.format("(JP, %s, , )", entry.attribute.jumpAddress));
        if (entry.type == SymbolTableEntry.TypeSpecifier.VOID)
            codeGenerator.semanticStack.push("#void");
        else
            codeGenerator.semanticStack.push(String.valueOf(entry.attribute.getOutputAddress()));
    }

    private static void declareFunction(CodeGenerator codeGenerator) {
        int dbAddress = codeGenerator.dataBlockAddress;

        int argumentNumber = 0;
        while (!codeGenerator.semanticStack.peek().equals("#function")) {
            String name = codeGenerator.semanticStack.pop();
            SymbolTableEntry.TypeSpecifier type = SymbolTableEntry.TypeSpecifier.valueOf(
                    (codeGenerator.semanticStack.pop()).toUpperCase());
            codeGenerator.symbolTable.add(new SymbolTableEntry(codeGenerator.dataBlockAddress, name, type));
            codeGenerator.dataBlockAddress += 4;
            argumentNumber++;
        }
        codeGenerator.semanticStack.pop();

        String name = codeGenerator.semanticStack.pop();
        SymbolTableEntry.TypeSpecifier type = SymbolTableEntry.TypeSpecifier.valueOf(
                (codeGenerator.semanticStack.pop()).toUpperCase());
        SymbolTableEntry entry = new SymbolTableEntry(codeGenerator.symbolTable.size(), name, type,
                new SymbolTableAttribute(argumentNumber, dbAddress, codeGenerator.programBlock.size()));
        codeGenerator.symbolTable.add(entry);

        // Output and return address
        codeGenerator.dataBlockAddress += 8;

        codeGenerator.semanticStack.push("#function");
        codeGenerator.semanticStack.push(String.valueOf(codeGenerator.symbolTable.size() - 1));
    }

    private static void pushFunctionKeyword(CodeGenerator codeGenerator) {
        codeGenerator.semanticStack.push("#function");
    }

    private static void caseUndoneFalse(CodeGenerator codeGenerator) {
        int undone = Integer.valueOf(codeGenerator.semanticStack
                .elementAt(codeGenerator.semanticStack.size() - 4));
        codeGenerator.programBlock.add(String.format("(ASSIGN, #0, %d, )", undone));
    }

    private static void caseUndoneTrue(CodeGenerator codeGenerator) {
        codeGenerator.programBlock.add(String.format("(ASSIGN, #1, %d, )", codeGenerator.tempBlockAddress));
        codeGenerator.semanticStack.push(String.valueOf(codeGenerator.tempBlockAddress));
        codeGenerator.tempBlockAddress += 4;
    }

    private static void checkCase(CodeGenerator codeGenerator) {
        String number = codeGenerator.semanticStack.pop();
        String expression = codeGenerator.semanticStack.peek();
        int undone = Integer.valueOf(codeGenerator.semanticStack
                .elementAt(codeGenerator.semanticStack.size() - 2));
        codeGenerator.programBlock.add(String.format("(JPF, %d, %d, )", undone, codeGenerator.programBlock.size() + 4));
        codeGenerator.programBlock.add(String.format("(EQ, %s, %s, %s)", expression, number,
                codeGenerator.tempBlockAddress));
        codeGenerator.semanticStack.push(String.valueOf(codeGenerator.tempBlockAddress));
        codeGenerator.tempBlockAddress += 4;
    }

    private static void endSwitch(CodeGenerator codeGenerator) {
        codeGenerator.semanticStack.pop(); // EXPRESSION
        codeGenerator.semanticStack.pop(); // Case Undone Temp
        int tempAssignAddress = Integer.valueOf(codeGenerator.semanticStack.pop());
        String breakTemp = codeGenerator.semanticStack.pop();
        codeGenerator.semanticStack.pop(); // #pinput switch

        codeGenerator.programBlock.set(tempAssignAddress, String.format("(ASSIGN, #%s, %s, )",
                codeGenerator.programBlock.size(), breakTemp));
    }

    private static void jumpFalseCase(CodeGenerator codeGenerator) {
        int programBlockAddress = Integer.valueOf(codeGenerator.semanticStack.pop());
        String eqCheck = codeGenerator.semanticStack.pop();
        codeGenerator.programBlock.set(programBlockAddress, String.format("(JPF, %s, %s, )",
                eqCheck, codeGenerator.programBlock.size()));
    }

    private static int getStackPointer(CodeGenerator codeGenerator, String str) {
        for (int i = codeGenerator.semanticStack.size() - 1; i >= 0; i--) {
            String element = codeGenerator.semanticStack.elementAt(i);
            if (element.equals(str))
                return i;
        }
        return -1;
    }

    private static int getBreakPointer(CodeGenerator codeGenerator) {
        int breakPointer = Math.max(getStackPointer(codeGenerator, "while"),
                getStackPointer(codeGenerator, "switch"));
        if (breakPointer == -1)
            codeGenerator.errors.add(String.format("%d: No 'while' or 'switch' found for 'break'.",
                    codeGenerator.lexer.getLineNumber()));
        return breakPointer;
    }

    private static int getContinuePointer(CodeGenerator codeGenerator) {
        int continuePointer = getStackPointer(codeGenerator, "while");
        if (continuePointer == -1)
            codeGenerator.errors.add(String.format("%d: No 'while' found for 'continue'.",
                    codeGenerator.lexer.getLineNumber()));
        return continuePointer;
    }

    private static void breakWhileSwitch(CodeGenerator codeGenerator) {
        codeGenerator.programBlock.add(String.format("(JP, @%s, , )",
                codeGenerator.semanticStack.elementAt(getBreakPointer(codeGenerator) + 1)));
    }

    private static void continueWhile(CodeGenerator codeGenerator) {
        codeGenerator.programBlock.add(String.format("(JP, %s, , )",
                Integer.valueOf(codeGenerator.semanticStack.elementAt(
                        getContinuePointer(codeGenerator) + 2)) + 1));
    }

    private static void pushTemp(CodeGenerator codeGenerator) {
        codeGenerator.semanticStack.push(String.valueOf(codeGenerator.tempBlockAddress));
        codeGenerator.tempBlockAddress += 4;
    }

    private static void endWhile(CodeGenerator codeGenerator) {
        int jpfAddress = Integer.valueOf(codeGenerator.semanticStack.pop());
        String condition = codeGenerator.semanticStack.pop();
        int tempAssignAddress = Integer.valueOf(codeGenerator.semanticStack.pop());
        String temp = codeGenerator.semanticStack.pop();
        codeGenerator.semanticStack.pop(); // #pinput while

        codeGenerator.programBlock.set(tempAssignAddress, String.format("(ASSIGN, #%s, %s, )",
                codeGenerator.programBlock.size() + 1, temp));
        codeGenerator.programBlock.set(jpfAddress, String.format("(JPF, %s, %s, )", condition,
                codeGenerator.programBlock.size() + 1));
        codeGenerator.programBlock.add(String.format("(JP, %s, , )", tempAssignAddress + 1));
    }

    private static void jump(CodeGenerator codeGenerator) {
        String programBlockAddress = codeGenerator.semanticStack.pop();
        codeGenerator.programBlock.set(Integer.valueOf(programBlockAddress), String.format("(JP, %s, , )",
                codeGenerator.programBlock.size()));
    }

    private static void jumpFalseSave(CodeGenerator codeGenerator) {
        String programBlockAddress = codeGenerator.semanticStack.pop();
        String condition = codeGenerator.semanticStack.pop();
        codeGenerator.programBlock.set(Integer.valueOf(programBlockAddress), String.format("(JPF, %s, %s, )", condition,
                codeGenerator.programBlock.size() + 1));

        codeGenerator.semanticStack.push(String.valueOf(codeGenerator.programBlock.size()));
        codeGenerator.programBlock.add("");
    }

    private static void save(CodeGenerator codeGenerator) {
        codeGenerator.semanticStack.push(String.valueOf(codeGenerator.programBlock.size()));
        codeGenerator.programBlock.add("");
    }

    private static void calculateSignedFactor(CodeGenerator codeGenerator) {
        String operand = codeGenerator.semanticStack.pop();
        String sign = codeGenerator.semanticStack.pop();
        if (sign.equals("+"))
            codeGenerator.semanticStack.push(operand);
        else {
            codeGenerator.programBlock.add(String.format("(SUB, #0, %s, %s)", operand, codeGenerator.tempBlockAddress));
            codeGenerator.semanticStack.push(String.valueOf(codeGenerator.tempBlockAddress));
            codeGenerator.tempBlockAddress += 4;
        }
    }

    private static void pushNumber(CodeGenerator codeGenerator) {
        codeGenerator.semanticStack.push("#" + codeGenerator.currentToken.text);
    }

    private static void calculateOperation(CodeGenerator codeGenerator) {
        String operand2 = codeGenerator.semanticStack.pop();
        String operator = codeGenerator.semanticStack.pop();
        String operand1 = codeGenerator.semanticStack.pop();

        Map<String, String> command = new HashMap<>();
        command.put("+", "ADD");
        command.put("-", "SUB");
        command.put("*", "MULT");
        command.put("<", "LT");
        command.put("==", "EQ");

        // TODO: Handle void operand
        if (operand1.equals("#void") || operand2.equals("#void"))
            codeGenerator.errors.add(String.format("%d: Type mismatch in operands.",
                    codeGenerator.lexer.getLineNumber()));

        codeGenerator.programBlock.add(String.format("(%s, %s, %s, %s)", command.get(operator), operand1, operand2,
                codeGenerator.tempBlockAddress));
        codeGenerator.semanticStack.push(String.valueOf(codeGenerator.tempBlockAddress));
        codeGenerator.tempBlockAddress += 4;
    }

    private static void calculateArrayAddress(CodeGenerator codeGenerator) {
        String index = codeGenerator.semanticStack.pop();
        String arrayAddress = codeGenerator.semanticStack.pop();
        codeGenerator.programBlock.add(String.format("(MULT, #4, %s, %s)", index, codeGenerator.tempBlockAddress));
        codeGenerator.programBlock.add(String.format("(ADD, %s, %s, %s)", codeGenerator.tempBlockAddress, arrayAddress,
                codeGenerator.tempBlockAddress));
        codeGenerator.semanticStack.push("@" + codeGenerator.tempBlockAddress);
        codeGenerator.tempBlockAddress += 4;
    }

    private static void assign(CodeGenerator codeGenerator) {
        String s = codeGenerator.semanticStack.pop();
        String d = codeGenerator.semanticStack.peek();
        codeGenerator.programBlock.add(String.format("(ASSIGN, %s, %s, )", s, d));
    }

    private static void pushId(CodeGenerator codeGenerator) {
        for (int i = codeGenerator.symbolTable.size() - 1; i >= 0; i--) {
            SymbolTableEntry entry = codeGenerator.symbolTable.get(i);
            if (entry.lexeme.equals(codeGenerator.currentToken.text)) {
                codeGenerator.semanticStack.push(String.valueOf(entry.address));
                break;
            }
        }
    }

    private static void declareVariable(CodeGenerator codeGenerator) {
        String name = codeGenerator.semanticStack.pop();
        SymbolTableEntry.TypeSpecifier type = SymbolTableEntry.TypeSpecifier.valueOf(
                (codeGenerator.semanticStack.pop()).toUpperCase());
        codeGenerator.symbolTable.add(new SymbolTableEntry(codeGenerator.dataBlockAddress, name, type));
        codeGenerator.dataBlockAddress += 4;

        if (type == SymbolTableEntry.TypeSpecifier.VOID)
            codeGenerator.errors.add(String.format("%d: Illegal type of void.", codeGenerator.lexer.getLineNumber()));
    }

    private static void declareArray(CodeGenerator codeGenerator) {
        int size = Integer.valueOf(codeGenerator.semanticStack.pop());
        String name = codeGenerator.semanticStack.pop();
        SymbolTableEntry.TypeSpecifier type = SymbolTableEntry.TypeSpecifier.valueOf(
                (codeGenerator.semanticStack.pop()).toUpperCase());
        codeGenerator.symbolTable.add(new SymbolTableEntry(codeGenerator.dataBlockAddress, name, type));
        codeGenerator.programBlock.add(String.format("(ASSIGN, #%d, %d, )", codeGenerator.dataBlockAddress + 4,
                codeGenerator.dataBlockAddress));
        codeGenerator.dataBlockAddress += (size + 1) * 4;

        if (type == SymbolTableEntry.TypeSpecifier.VOID)
            codeGenerator.errors.add(String.format("%d: Illegal type of void.", codeGenerator.lexer.getLineNumber()));
    }


    private static void pushInput(CodeGenerator codeGenerator) {
        codeGenerator.semanticStack.push(codeGenerator.currentToken.text);
    }
}
