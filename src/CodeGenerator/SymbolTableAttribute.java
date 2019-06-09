package CodeGenerator;

class SymbolTableAttribute {
    int argumentNumber;
    int dataBlockAddress;
    int jumpAddress;

    SymbolTableAttribute(int argumentNumber, int dataBlockAddress, int jumpAddress) {
        this.argumentNumber = argumentNumber;
        this.dataBlockAddress = dataBlockAddress;
        this.jumpAddress = jumpAddress;
    }

    int getOutputAddress() {
        return dataBlockAddress + argumentNumber * 4;
    }

    int getReturnAddress() {
        return dataBlockAddress + (argumentNumber + 1) * 4;
    }
}
