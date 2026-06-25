package compiler.lexer;

public record Token(
    String lexema,
    TokenType type
) {
    @Override
    public final String toString() {
        return String.format("<%s, %s>", lexema(), type().name());
    }
}