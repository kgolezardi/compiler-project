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
        }
    }

    private static void declareVariable(CodeGenerator codeGenerator) {
        String name = (String) codeGenerator.semanticStack.pop();
        SymbolTableEntry.TypeSpecifier type = SymbolTableEntry.TypeSpecifier.valueOf(
                ((String) codeGenerator.semanticStack.pop()).toUpperCase());
        codeGenerator.symbolTable.add(new SymbolTableEntry(codeGenerator.dataBlockAddress, name, type));
        codeGenerator.dataBlockAddress++;
    }

    private static void pinput(CodeGenerator codeGenerator) {
        codeGenerator.semanticStack.push(codeGenerator.currentToken.text);
    }
}
