import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Automaton for java lexer.
 */
public class Automaton {
  private List<Integer> finalStates;
  private List<HashMap<Character, Integer>> transitions;
  private char[] endOfToken = {'(', ')', '{', '}', '[', ']', ';', ',', '.', ' ', '/', '=', '>', '<', '!', '~', '?', ':', '&',
      '|', '+', '-', '*', '/', '^', '%'};
  private Integer[] identifierFinalStates = {57, 58};
  private Integer[] integerLiteralFinalStates = {1, 4, 6, 7, 20, 21, 23, 24, 25, 27, 39, 42, 59, 65};
  private Integer[] floatLiteralFinalStates = {2, 14, 15, 17, 28, 29, 30, 32, 35, 37, 38, 40};
  private Integer[] characterLiteralFinalStates = {66};
  private Integer[] stringLiteralFinalStates = {67};
  private Integer[] operatorFinalStates = {53, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 80};
  private Integer[] separatorFinalStates = {79};

  public class TransitionNotFoundException extends Exception {
    public TransitionNotFoundException() {
    }
  }

  public class TokenNotAcceptedException extends Exception {
    public TokenNotAcceptedException() {
    }
  }

  public Automaton(Path filePath) throws IOException {
    transitions = new ArrayList<>();
    Files.lines(filePath, Charset.forName("UTF-8")).forEach(line -> processTransition(line));
    addRemainingElementsToTransitions();
  }

  private void processTransition(String line) {
    String[] result = line.split("\\s");
    Integer fromState = Integer.parseInt(result[0]);
    Integer toState = Integer.parseInt(result[1]);
    String rule = result[2];
    if (rule.length() == 1) {
      addTransition(fromState, toState, rule.charAt(0));
    } else {
      switch (rule) {
        case "LF":
          addTransition(fromState, toState, new Character((char) 10));
          break;
        case "FF":
          addTransition(fromState, toState, new Character((char) 12));
          break;
        case "CR":
          addTransition(fromState, toState, new Character((char) 13));
          break;
        case "space":
          addTransition(fromState, toState, new Character((char) 32));
          break;
        case "HT":
          addTransition(fromState, toState, new Character((char) 9));
          break;
        case "VT":
          addTransition(fromState, toState, new Character((char) 11));
          break;
        case "JL":
          for (int i = 0; i < 256; i++) {
            if (Character.isJavaLetter((char) i)) {
              addTransition(fromState, toState, new Character((char) (i & 0xFF)));
            }
          }
          break;
        case "JD":
          for (int i = 0; i < 256; i++) {
            if (Character.isJavaLetterOrDigit((char) i) && !Character.isJavaLetter((char) (i & 0xFF))) {
              addTransition(fromState, toState, new Character((char) (i & 0xFF)));
            }
          }
          break;
        case "sep":
          for (int i = 0; i < endOfToken.length; i++) {
            addTransition(fromState, toState, new Character(endOfToken[i]));
          }
          break;
        case "no":
          char primitiveCharacter = result[3].charAt(0);
          if (result[3].length() > 1) {
            primitiveCharacter = (char) getCharacterCode(result[3]);
          }
          Character character = new Character(primitiveCharacter);
          if (transitions.size() > fromState && transitions.get(fromState).get(character) == toState) {
            transitions.get(fromState).remove(character);
          } else {
            for (int i = 0; i < 256; i++) {
              if ((char) i != primitiveCharacter) {
                addTransition(fromState, toState, new Character((char) (i & 0xFF)));
              }
            }
          }
          break;
        default:
          /* do nothing */
      }
    }
  }

  public Integer getNextState(Integer fromState, Character character) throws TransitionNotFoundException {
    Integer toState = transitions.get(fromState).get(character);
    if (toState == null) {
      throw new TransitionNotFoundException();
    }
    return toState;
  }

  public boolean isFinalState(Integer state) {
    return belongsTo(identifierFinalStates, state) || belongsTo(integerLiteralFinalStates, state) ||
        belongsTo(floatLiteralFinalStates, state) || belongsTo(characterLiteralFinalStates, state) ||
        belongsTo(stringLiteralFinalStates, state) || belongsTo(operatorFinalStates, state) ||
        belongsTo(separatorFinalStates, state);

  }

  public TokenType acceptedTokenType(Integer state) throws TokenNotAcceptedException {
    if (belongsTo(identifierFinalStates, state)) {
      return TokenType.IDENTIFIER;
    }
    if (belongsTo(integerLiteralFinalStates, state)) {
      return TokenType.INTEGER_LITERAL;
    }
    if (belongsTo(floatLiteralFinalStates, state)) {
      return TokenType.FLOATING_POINT_LITERAL;
    }
    if (belongsTo(characterLiteralFinalStates, state)) {
      return TokenType.CHARACTER_LITERAL;
    }
    if (belongsTo(stringLiteralFinalStates, state)) {
      return TokenType.STRING_LITERAL;
    }
    if (belongsTo(operatorFinalStates, state)) {
      return TokenType.OPERATOR;
    }
    if (belongsTo(separatorFinalStates, state)) {
      return  TokenType.SEPARATOR;
    }
    throw new TokenNotAcceptedException();
  }

  private boolean belongsTo(Integer[] array, Integer value) {
    for (int i = 0; i < array.length; i++) {
      if (array[i] == value) {
        return true;
      }
    }
    return false;
  }

  private void addTransition(Integer fromState, Integer toState, Character symbol) {
    while (transitions.size() <= fromState) {
      transitions.add(new HashMap<>());
    }
    if (transitions.get(fromState).get(symbol) == null) {
      transitions.get(fromState).put(symbol, toState);
    }
  }

  private void addRemainingElementsToTransitions() {
    for (int i = 0; i <= 79; i++) {
      if (transitions.size() <= i) {
        transitions.add(new HashMap<>());
      }
    }
  }

  private int getCharacterCode(String character) {
    switch (character) {
      case "LF":
        return 10;
      case "FF":
        return 12;
      case "CR":
        return 13;
      case "space":
        return 32;
      case "HT":
        return 9;
      case "VT":
        return 11;
      case "BS":
        return 8;
    }
    throw new IllegalArgumentException();
  }
}
