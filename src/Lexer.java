import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lexer {
  private byte[] sourceFile;
  private int currentPosition = 0;
  private Automaton automaton;
  private List<String> tokenList;

  private static final List<String> KEYWORDS = Arrays.asList("abstract", "assert", "boolean", "break", "byte", "case", "catch",
      "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally",
      "float", "for", "if", "goto", "implements", "import", "instanceof", "int", "interface", "long", "native",
      "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
      "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while");

  public class LexerException extends Exception {
    private int position;

    public LexerException(int position) {
      this.position = position;
    }

    public int getPosition() {
      return position;
    }

  }

  public Lexer(Path filePath) throws IOException {
    this.sourceFile = Files.readAllBytes(filePath);
    automaton = new Automaton(Paths.get("automat-lexer-java.txt"));
    tokenList = new ArrayList<>();
  }

  public Token getToken() throws LexerException {
    String token = new String();
    TokenType tokenType = null;
    int state = 0;
    boolean accepted = false;
    int acceptedPosition = -1;
    while (currentPosition < sourceFile.length) {
      if (state == 0) {
        token = new String();
        accepted = false;
      }
      try {
        state = automaton.getNextState(state, new Character((char) (sourceFile[currentPosition] & 0xFF)));
        token = token + (char) (sourceFile[currentPosition] & 0xFF);
        currentPosition++;
      } catch (Automaton.TransitionNotFoundException e) {
        if (!accepted) {
          throw new LexerException(currentPosition);
        }
        token = token.substring(0, token.length() - currentPosition + acceptedPosition);
        currentPosition = acceptedPosition;
        if (tokenType == TokenType.IDENTIFIER) {
          if (isKeyword(token)) {
            tokenType = TokenType.KEYWORD;
          } else if (isNullLiteral(token)) {
            tokenType = TokenType.NULL_LITERAL;
          } else if (isBooleanLiteral(token)) {
            tokenType = TokenType.BOOLEAN_LITERAL;
          }
        }
        return new Token(getTokenIndex(token), tokenType);
      }
      if (automaton.isFinalState(state)) {
        try {
          tokenType = automaton.acceptedTokenType(state);
          accepted = true;
          acceptedPosition = currentPosition;
        } catch (Automaton.TokenNotAcceptedException e) {
          /* do nothing */
        }
      }
    }
    if (state == 0) {
      return null;
    }
    if (!accepted) {
      throw new LexerException(currentPosition);
    }
    token = token.substring(0, token.length() - currentPosition + acceptedPosition);
    currentPosition = acceptedPosition + 1;
    if (tokenType == TokenType.IDENTIFIER) {
      if (isKeyword(token)) {
        tokenType = TokenType.KEYWORD;
      } else if (isNullLiteral(token)) {
        tokenType = TokenType.NULL_LITERAL;
      } else if (isBooleanLiteral(token)) {
        tokenType = TokenType.BOOLEAN_LITERAL;
      }
    }
    return new Token(getTokenIndex(token), tokenType);
  }

  public boolean endOfFile() {
    return currentPosition == sourceFile.length;
  }

  public String getTokenStringFromIndex(int i) {
    return tokenList.get(i);
  }

  private boolean isKeyword(String token) {
    if (KEYWORDS.contains(token)) {
      return true;
    }
    return false;
  }

  private boolean isBooleanLiteral(String token) {
    if (token == "true" || token == "false") {
      return true;
    }
    return false;
  }

  private boolean isNullLiteral(String token) {
    if (token == "null") {
      return true;
    }
    return false;
  }

  private int getTokenIndex(String token) {
    if (!tokenList.contains(token)) {
      tokenList.add(token);
    }
    return tokenList.lastIndexOf(token);
  }
}
