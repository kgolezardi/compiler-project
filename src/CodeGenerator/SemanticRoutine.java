package CodeGenerator;

class SemanticRoutine {
    static void call(String routineName, CodeGenerator codeGenerator) {
        switch (routineName) {
            case "#pinput":
                pinput(codeGenerator);
                break;
            case "#declare_var":
                declareVariable(codeGenerator);
                break;
            case "#declare_array":
                declareArray(codeGenerator);
                break;
        }
    }

    private static void declareVariable(CodeGenerator codeGenerator) {
        String name = (String) codeGenerator.semanticStack.pop();
        SymbolTableEntry.TypeSpecifier type = SymbolTableEntry.TypeSpecifier.valueOf(
                ((String) codeGenerator.semanticStack.pop()).toUpperCase());
        codeGenerator.symbolTable.add(new SymbolTableEntry(codeGenerator.dataBlockAddress, name, type));
        codeGenerator.dataBlockAddress += 4;
    }

    private static void declareArray(CodeGenerator codeGenerator) {
        int size = Integer.valueOf((String) codeGenerator.semanticStack.pop());
        String name = (String) codeGenerator.semanticStack.pop();
        SymbolTableEntry.TypeSpecifier type = SymbolTableEntry.TypeSpecifier.valueOf(
                ((String) codeGenerator.semanticStack.pop()).toUpperCase());
        codeGenerator.symbolTable.add(new SymbolTableEntry(codeGenerator.dataBlockAddress, name, type));
        codeGenerator.programBlock.add(String.format("(ASSIGN, #%d, %d, )", codeGenerator.dataBlockAddress + 4,
                codeGenerator.dataBlockAddress));
        codeGenerator.dataBlockAddress += (size + 1) * 4;
    }



    private static void pinput(CodeGenerator codeGenerator) {
        codeGenerator.semanticStack.push(codeGenerator.currentToken.text);
    }
}
