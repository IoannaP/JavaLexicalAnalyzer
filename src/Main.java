import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;


public class Main {
  public static void main(String args[]) throws IOException {
    Lexer lexer = new Lexer(Paths.get("f.txt"));
    Token token;
    File file = new File("tokens.txt");

    if (!file.exists()) {
      file.createNewFile();
    }

    FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

    while (!lexer.endOfFile()) {
      try {
        token = lexer.getToken();
        if (token != null)
        bufferedWriter.write(token.getTokenType().toString() + " " + lexer.getTokenStringFromIndex(token.getToken())
            + "\n");
      } catch (Lexer.LexerException e) {
        bufferedWriter.write("Lexer Error at character number " + e.getPosition());
        bufferedWriter.close();
        return;
      }
    }
    bufferedWriter.close();
  }
}
