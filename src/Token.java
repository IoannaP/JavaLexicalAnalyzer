public class Token {
  int token;
  TokenType type;

  public Token(int token, TokenType type) {
    this.token = token;
    this.type = type;
  }

  public int getToken() {
    return token;
  }

  public TokenType getTokenType() {
    return type;
  }
}
