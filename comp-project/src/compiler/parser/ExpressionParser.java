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
            throw new RuntimeException(
                "Erro Sintático: esperado '" + type +
                "' mas encontrado '" + lookahead.lexema() +
                "' (tipo: " + lookahead.type() + ")"
            );
        }
    }

    public String parse() throws IOException {
        String result = expr();
        if (lookahead.type() != TokenType.EOF) {
            throw new RuntimeException(
                "Erro Sintático: token inesperado '" + lookahead.lexema() +
                "' após o fim da expressão"
            );
        }
        return result;
    }

    private String expr() throws IOException {
        String left = term();
        return exprTail(left);
    }

    private String exprTail(String left) throws IOException {
        if (lookahead.type() == TokenType.OP_SOMA) {
            match(TokenType.OP_SOMA);
            String right = term();
            return exprTail("+ " + left + " " + right);
        }
        if (lookahead.type() == TokenType.OP_SUB) {
            match(TokenType.OP_SUB);
            String right = term();
            return exprTail("- " + left + " " + right);
        }
        // ε — nada mais para consumir no nível Expr
        return left;
    }


    private String term() throws IOException {
        String left = pow();
        return termTail(left);
    }

   
    private String termTail(String left) throws IOException {
        if (lookahead.type() == TokenType.OP_MULT) {
            match(TokenType.OP_MULT);
            String right = pow();
            return termTail("* " + left + " " + right);
        }
        if (lookahead.type() == TokenType.OP_DIV) {
            match(TokenType.OP_DIV);
            String right = pow();
            return termTail("/ " + left + " " + right);
        }
        if (lookahead.type() == TokenType.OP_MOD) {
            match(TokenType.OP_MOD);
            String right = pow();
            return termTail("% " + left + " " + right);
        }
        // ε
        return left;
    }


    private String pow() throws IOException {
        String base = unary();
        if (lookahead.type() == TokenType.OP_POW) {
            match(TokenType.OP_POW);
            // Recursão à direita: parse o restante da cadeia de ^
            String exponent = pow();
            return "^ " + base + " " + exponent;
        }
        return base;
    }


    private String unary() throws IOException {
        if (lookahead.type() == TokenType.OP_SOMA) {
            match(TokenType.OP_SOMA);
            // Unário positivo: em notação prefixa representa-se como "+u operando"
            // mas semanticamente não altera o valor; convenção: prefixamos com "+u"
            String operand = unary();
            return "+u " + operand;
        }
        if (lookahead.type() == TokenType.OP_SUB) {
            match(TokenType.OP_SUB);
            String operand = unary();
            return "-u " + operand;
        }
        return base();
    }


    private String base() throws IOException {
        switch (lookahead.type()) {
            case AP: {
                match(TokenType.AP);
                String inner = expr();
                match(TokenType.FP);
                return inner;
            }
            case ID: {
                String lexema = lookahead.lexema();
                match(TokenType.ID);
                return lexema;
            }
            case INT: {
                String lexema = lookahead.lexema();
                match(TokenType.INT);
                return lexema;
            }
            case FLOAT: {
                String lexema = lookahead.lexema();
                match(TokenType.FLOAT);
                return lexema;
            }
            default:
                throw new RuntimeException(
                    "Erro Sintático: token inesperado '" + lookahead.lexema() +
                    "' (tipo: " + lookahead.type() + ") — " +
                    "esperado identificador, número ou '('"
                );
        }
    }
}