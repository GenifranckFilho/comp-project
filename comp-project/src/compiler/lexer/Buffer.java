package compiler.lexer;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Buffer implements Closeable{
    private BufferedReader reader;

    private int row, col;
    private String currentLine;

    public Buffer(String fileName) throws IOException{
        this.reader = new BufferedReader(new FileReader(fileName));
        row = 0;
        readNextLine();
    }

    public void readNextLine() throws IOException{
        currentLine = reader.readLine();
        row++;
        col = 0;
    }

    public String getCurrentLine() {
        return currentLine;
    }

    public Character getCurrentChar(){
        return currentLine.charAt(col);
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public boolean isEOF(){
        return currentLine == null;
    }

    public boolean isEOL(){
        return currentLine != null && col >= currentLine.length();
    }

    public void consume(String lexema){
        col += lexema.length();
    }

    public String tryMatch(Pattern pattern){
        if(isEOF()){
            return null;
        }
        Matcher matcher = pattern.matcher(currentLine.substring(col));
        if(matcher.lookingAt()){
            return matcher.group(0);
        }

        return null;
    }

    @Override
    public void close() throws IOException {
        if(reader != null){
            reader.close();
        }
    }
}
