import compiler.lexer.Lexer;
import compiler.parser.ExpressionParser;
import java.io.*;
import java.nio.file.*;
import java.util.Scanner;


public class App {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            // Modo arquivo: lê todas as linhas do arquivo e processa cada uma
            processFile(args[0]);
        } else {
            // Modo interativo: lê da entrada padrão linha a linha
            processStdin();
        }
    }

    private static void processFile(String fileName) throws Exception {
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            System.err.println("Erro: arquivo '" + fileName + "' não encontrado.");
            System.exit(1);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;
                processExpression(line, lineNumber);
            }
        }
    }

    private static void processStdin() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Compilador de Expressões Aritméticas");
        System.out.println("Digite uma expressão por linha (Ctrl+D para sair):");
        System.out.println("----------------------------------------------");

        int lineNumber = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            lineNumber++;
            processExpression(line, lineNumber);
        }
    }

    private static void processExpression(String expression, int lineNumber) {
        System.out.print("Infixa  [" + lineNumber + "]: " + expression + "\n");

        // Escreve a expressão em um arquivo temporário para o Lexer
        File tempFile = null;
        try {
            tempFile = File.createTempFile("expr_", ".tmp");
            try (PrintWriter writer = new PrintWriter(tempFile)) {
                writer.println(expression);
            }

            try (Lexer lexer = new Lexer(tempFile.getAbsolutePath())) {
                ExpressionParser parser = new ExpressionParser(lexer);
                String prefixResult = parser.parse();
                System.out.println("Prefixa [" + lineNumber + "]: " + prefixResult);
            }

        } catch (RuntimeException e) {
            System.err.println("  ✗ " + e.getMessage());
        } catch (Exception e) {
            System.err.println("  ✗ Erro inesperado: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
        System.out.println();
    }
}