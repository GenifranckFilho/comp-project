package compiler.parser;

import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.lexer.TokenType;
import java.io.IOException;

public class ExpressionParser {
    
    private Lexer lexer;
    private Token lookahead;

    public ExpressionParser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        this.lookahead = lexer.readNextToken(); 
    }

    private void match(TokenType type) throws IOException {
        if (lookahead.type() == type) {
            lookahead = lexer.readNextToken();
        } else {
            throw new RuntimeException("Erro Sintático: Esperado o token " + type + 
                                       " mas foi encontrado '" + lookahead.lexema() + "'");
        }
    }

    public String parse() throws IOException {
        String resultado = E(); 

        match(TokenType.EOF); 

        return resultado;
    }

    private String E() throws IOException {
        if (lookahead.type() == TokenType.ID) {
            String lexema = lookahead.lexema();
            match(TokenType.ID);
            return lexema;
        }
        return ""; 
    }
}