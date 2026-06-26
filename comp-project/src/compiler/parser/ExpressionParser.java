package compiler.parser;

import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.lexer.TokenType;
import java.io.IOException;

/**
 * Analisador Sintático Preditivo por Descendência Recursiva
 * com Tradução Dirigida pela Sintaxe (TDS) — saída em notação prefixa.
 *
 * Gramática LL(1) (recursão à esquerda removida, estratificada por precedência):
 *
 *   Expr     → Term ExprTail
 *   ExprTail → ('+' | '-') Term ExprTail  |  ε
 *
 *   Term     → Pow TermTail
 *   TermTail → ('*' | '/' | '%') Pow TermTail  |  ε
 *
 *   Pow      → Unary ('^' Pow)?          (associatividade direita)
 *
 *   Unary    → '+' Unary  |  '-' Unary  |  Base
 *
 *   Base     → '(' Expr ')'  |  ID  |  INT  |  FLOAT
 *
 * TDS: atributos sintetizados — cada método retorna a subexpressão
 * correspondente já em notação prefixa.
 *
 * Associatividade:
 *   - +, -, *, /, %  →  esquerda  (via tail com acumulador herdado)
 *   - ^              →  direita   (via recursão direta em Pow)
 *   - unário + e -   →  direita   (via recursão direta em Unary)
 */

public class ExpressionParser {

    private Lexer lexer;
    private Token lookahead;

    public ExpressionParser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        this.lookahead = lexer.readNextToken();
    }

    // -----------------------------------------------------------------------
    // Utilitário: consome o token atual se for do tipo esperado, avança lookahead
    // -----------------------------------------------------------------------
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

    // -----------------------------------------------------------------------
    // Ponto de entrada público
    // -----------------------------------------------------------------------

    /**
     * Analisa a expressão inteira e retorna sua representação prefixa.
     * Após o parse, verifica se o token restante é EOF.
     */
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

    // -----------------------------------------------------------------------
    // Regra: Expr → Term ExprTail
    // -----------------------------------------------------------------------
    private String expr() throws IOException {
        String left = term();
        return exprTail(left);
    }

    /**
     * ExprTail → ('+' | '-') Term ExprTail  |  ε
     *
     * TDS (atributo herdado `left`):
     *   Ao encontrar um operador, constrói "op left right" e passa como novo
     *   acumulador para a chamada recursiva — garantindo associatividade à esquerda.
     *
     *   Exemplo: A + B + C
     *     exprTail("A")
     *       op="+", right=term()="B" -> novo acumulador = "+ A B"
     *       exprTail("+ A B")
     *         op="+", right=term()="C" -> novo acumulador = "+ + A B C"
     *         exprTail("+ + A B C") -> ε -> retorna "+ + A B C"
     */
    
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

    // -----------------------------------------------------------------------
    // Regra: Term → Pow TermTail
    // -----------------------------------------------------------------------
    private String term() throws IOException {
        String left = pow();
        return termTail(left);
    }

    /**
     * TermTail → ('*' | '/' | '%') Pow TermTail  |  ε
     *
     * Mesma estratégia de acumulador herdado que ExprTail.
     */
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

    // -----------------------------------------------------------------------
    // Regra: Pow → Unary ('^' Pow)?
    //
    // Associatividade DIREITA: 2^3^2 = 2^(3^2) = 512
    // Implementada com recursão à direita — não é recursão à esquerda,
    // portanto não viola LL(1).
    // -----------------------------------------------------------------------
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

    // -----------------------------------------------------------------------
    // Regra: Unary → '+' Unary  |  '-' Unary  |  Base
    //
    // Operadores unários têm maior precedência que qualquer binário.
    // São associativos à direita por natureza (recursão direta).
    // Exemplos: -A, --A, +-A, -(A+B)
    // -----------------------------------------------------------------------
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

    // -----------------------------------------------------------------------
    // Regra: Base → '(' Expr ')'  |  ID  |  INT  |  FLOAT
    // -----------------------------------------------------------------------
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