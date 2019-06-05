package CodeGenerator;

import java.util.HashMap;
import java.util.Map;

class SemanticRoutine {
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
            case "#while":
                jumpFalseWhile(codeGenerator);
                break;
            case "#label":
                label(codeGenerator);
                break;
            case "#continue":
                continueWhile(codeGenerator);
                break;
            case "#break":
                breakWhileCase(codeGenerator);
                break;
            case "#pop":
                codeGenerator.semanticStack.pop();
                break;
        }
//        System.out.println(codeGenerator.semanticStack.size() + routineName);
    }

    private static void breakWhileCase(CodeGenerator codeGenerator) {
        String temp = codeGenerator.semanticStack.elementAt(codeGenerator.semanticStack.size() - 4);
        codeGenerator.programBlock.add(String.format("(JP, @%s, , )", temp));
    }

    private static void continueWhile(CodeGenerator codeGenerator) {
        String labelAddress = codeGenerator.semanticStack.elementAt(codeGenerator.semanticStack.size() - 3);
        codeGenerator.programBlock.add(String.format("(JP, %s, , )", labelAddress));
    }

    private static void label(CodeGenerator codeGenerator) {
        codeGenerator.semanticStack.push(String.valueOf(codeGenerator.tempBlockAddress));
        codeGenerator.tempBlockAddress += 4;
        codeGenerator.programBlock.add("");
        codeGenerator.semanticStack.push(String.valueOf(codeGenerator.programBlock.size()));
    }

    private static void jumpFalseWhile(CodeGenerator codeGenerator) {
        String saveAddress = codeGenerator.semanticStack.pop();
        String condition = codeGenerator.semanticStack.pop();
        String labelAddress = codeGenerator.semanticStack.pop();
        String temp = codeGenerator.semanticStack.pop();
        codeGenerator.programBlock.set(Integer.valueOf(labelAddress) - 1, String.format("(ASSIGN, #%s, %s, )",
                codeGenerator.programBlock.size() + 1 , temp));
        codeGenerator.programBlock.set(Integer.valueOf(saveAddress), String.format("(JPF, %s, %s, )", condition,
                codeGenerator.programBlock.size() + 1));
        codeGenerator.programBlock.add(String.format("(JP, %s, , )", labelAddress));
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
    }


    private static void pushInput(CodeGenerator codeGenerator) {
        codeGenerator.semanticStack.push(codeGenerator.currentToken.text);
    }
}
