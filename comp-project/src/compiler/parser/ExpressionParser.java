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

    // expr → term exprR
    private String expr() throws IOException {
        String left = term();
        return exprR(left);
    }

    // exprR → op1 term exprR | ε
    private String exprR(String left) throws IOException {
        if (lookahead.type() == TokenType.OP_SOMA || lookahead.type() == TokenType.OP_SUB) {
            String op = op1();
            String right = term();
            // Mantém a notação prefixa do seu código original
            return exprR(op + " " + left + " " + right);
        }
        // ε — nada mais para consumir no nível expr
        return left;
    }

    // term → pow termR
    private String term() throws IOException {
        String left = pow();
        return termR(left);
    }

    // termR → op2 pow termR | ε
    private String termR(String left) throws IOException {
        if (lookahead.type() == TokenType.OP_MULT || 
            lookahead.type() == TokenType.OP_DIV || 
            lookahead.type() == TokenType.OP_MOD) {
            
            String op = op2();
            String right = pow();
            return termR(op + " " + left + " " + right);
        }
        // ε
        return left;
    }

    // pow → unary ('^' pow)?
    private String pow() throws IOException {
        String base = unary();
        if (lookahead.type() == TokenType.OP_POW) {
            match(TokenType.OP_POW);
            String exponent = pow(); // Recursão à direita mantida
            return "^ " + base + " " + exponent;
        }
        return base;
    }

    // unary → ('+' | '-') unary | factor
    private String unary() throws IOException {
        if (lookahead.type() == TokenType.OP_SOMA) {
            match(TokenType.OP_SOMA);
            String operand = unary();
            return "+u " + operand;
        }
        if (lookahead.type() == TokenType.OP_SUB) {
            match(TokenType.OP_SUB);
            String operand = unary();
            return "-u " + operand;
        }
        return factor();
    }

    // factor → '(' expr ')' | ID | NUMBER
    private String factor() throws IOException {
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
            case INT: { // Substitui os antigos INT e FLOAT da Gramática 2
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

    // op1 → '+' | '-'
    private String op1() throws IOException {
        if (lookahead.type() == TokenType.OP_SOMA) {
            match(TokenType.OP_SOMA);
            return "+";
        } else if (lookahead.type() == TokenType.OP_SUB) {
            match(TokenType.OP_SUB);
            return "-";
        }
        throw new RuntimeException("Erro Sintático: esperado operador '+' ou '-'");
    }

    // op2 → '*' | '/' | '%'
    private String op2() throws IOException {
        if (lookahead.type() == TokenType.OP_MULT) {
            match(TokenType.OP_MULT);
            return "*";
        } else if (lookahead.type() == TokenType.OP_DIV) {
            match(TokenType.OP_DIV);
            return "/";
        } else if (lookahead.type() == TokenType.OP_MOD) {
            match(TokenType.OP_MOD);
            return "%";
        }
        throw new RuntimeException("Erro Sintático: esperado operador '*', '/' ou '%'");
    }
}