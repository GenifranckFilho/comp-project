package compiler.lexer;

public enum TokenType {
    INT, FLOAT, //Literais de números
    ID, // Identificador
    AP, FP, //Símbolo reservado
    OP_SOMA, OP_SUB, OP_MULT, OP_DIV, OP_MOD, OP_POW, //Operadores
    WHITESPACE, EOF;
}
