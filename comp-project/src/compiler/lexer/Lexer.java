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
                for(var tp : TokenPattern.values()){
                    var lexema = buffer.tryMatch(tp.getPattern());
                    if(lexema != null){
                        buffer.consume(lexema);
                        if (tp.getType() == TokenType.WHITESPACE) break; //ignora o espaço em branco
                        return new Token(lexema, tp.getType());
                    }
                }
                throw new RuntimeException("Caractere não reconhecido");
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
