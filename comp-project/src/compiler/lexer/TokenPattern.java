package compiler.lexer;

import java.util.regex.Pattern;

public enum TokenPattern {
    FLOAT("[0-9]+\\.[0-9]+", TokenType.FLOAT),
    INT("[0-9]+", TokenType.INT),
    ID("[a-zA-Z][a-zA-Z0-9]*", TokenType.ID),
    AP("\\(", TokenType.AP),
    FP("\\)", TokenType.FP),
    OP_POW("\\^", TokenType.OP_POW),
    OP_MULT("\\*", TokenType.OP_MULT),
    OP_DIV("/", TokenType.OP_DIV),
    OP_MOD("%", TokenType.OP_MOD),
    OP_SOMA("\\+", TokenType.OP_SOMA),
    OP_SUB("-", TokenType.OP_SUB),
    WHITESPACE("\\s+", TokenType.WHITESPACE)
    ;

    private Pattern pattern;
    private TokenType type;

    TokenPattern(String regex, TokenType type){
        pattern = Pattern.compile(regex);
        this.type = type;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public TokenType getType() {
        return type;
    }
}
