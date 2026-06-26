import compiler.lexer.Lexer;
import compiler.parser.ExpressionParser;

public class App {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("É necessário passar um arquivo de texto no args[0]");
        }

        System.out.println("--- INICIANDO TESTE COM O PARSER CONSTRUÍDO ---");

        try (var lexer = new Lexer(args[0])) {
            ExpressionParser parser = new ExpressionParser(lexer);
            
            String resultado = parser.parse();
            
            System.out.println("\n[SINTÁTICO] Execução concluída com sucesso!");
            System.out.println("[SINTÁTICO] Resultado retornado pelo Parser: " + resultado);
            
        } catch (RuntimeException e) {
            System.err.println("\n[FALHA NO PARSER] " + e.getMessage());
        }
    }
}