import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.lexer.TokenType;
public class App {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("É necessário passar um arquivo de texto no args[0]");
        }
        try(var lexer = new Lexer(args[0])) {
            Token tk = lexer.readNextToken();
            while(tk.type() != TokenType.EOF) {
                System.err.print(tk + " ");
                tk = lexer.readNextToken();
            }
        }
    }
}
