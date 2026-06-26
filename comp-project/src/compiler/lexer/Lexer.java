package compiler.lexer;

import java.io.Closeable;
import java.io.IOException;

public class Lexer implements Closeable{
    private Buffer buffer;

    public Lexer(String fileName) throws IOException{
        this.buffer = new Buffer(fileName);
    }

    public Token readNextToken() throws IOException{
        while(!buffer.isEOF()){
            if(buffer.isEOL()){
                buffer.readNextLine();
            }else{
                boolean matched = false;
                for(var tp : TokenPattern.values()){
                    var lexema = buffer.tryMatch(tp.getPattern());
                    if(lexema != null){
                        buffer.consume(lexema);
                        matched = true;
                        if (tp.getType() != TokenType.WHITESPACE){
                            return new Token(lexema, tp.getType());
                        }  
                        break;
                    }
                }
                if (!matched) {
                    throw new RuntimeException("Erro léxico: caractere '" + buffer.getCurrentChar() + "' não reconhecido na coluna " + (buffer.getCol() + 1) + ".");
                }
            }
        }
        return new Token("", TokenType.EOF);
    }

    @Override
    public void close() throws IOException {
        if(buffer != null){
            buffer.close();
        }
    }
}
