package org.example.lexer;

import org.example.lexer.types.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String input;
    private final int length;
    private int position;
    private int line;
    private int column;

    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();
    private static final Map<String, TokenType> OPERATORS = new HashMap<>();

    static {
        KEYWORDS.put("var", TokenType.VAR);
        KEYWORDS.put("print", TokenType.PRINT);
        KEYWORDS.put("if", TokenType.IF);
        KEYWORDS.put("else", TokenType.ELSE);
        KEYWORDS.put("while", TokenType.WHILE);

        OPERATORS.put("==", TokenType.EQEQ);
        OPERATORS.put("!=", TokenType.NEQ);
        OPERATORS.put("<=", TokenType.LTEQ);
        OPERATORS.put(">=", TokenType.GTEQ);
        OPERATORS.put("&&", TokenType.AND);
        OPERATORS.put("||", TokenType.OR);
        OPERATORS.put("+", TokenType.PLUS);
        OPERATORS.put("-", TokenType.MINUS);
        OPERATORS.put("*", TokenType.STAR);
        OPERATORS.put("/", TokenType.SLASH);
        OPERATORS.put("=", TokenType.EQ);
        OPERATORS.put("<", TokenType.LT);
        OPERATORS.put(">", TokenType.GT);
        OPERATORS.put("!", TokenType.EXCL);
        OPERATORS.put("(", TokenType.LPAREN);
        OPERATORS.put(")", TokenType.RPAREN);
        OPERATORS.put("{", TokenType.LBRACE);
        OPERATORS.put("}", TokenType.RBRACE);
        OPERATORS.put(";", TokenType.SEMICOLON);
    }

    public Lexer(String input) {
        this.input = input != null ? input : "";
        this.length = this.input.length();
        this.position = 0;
        this.line = 1;
        this.column = 1;
    }

    public List<Token> tokenize() throws Exception {
        List<Token> result = new ArrayList<>();

        while (position < length) {
            char current = peek();

            if (Character.isWhitespace(current)) {
                if (current == '\n') {
                    line++;
                    column = 1;
                } else {
                    column++;
                }
                next();
                continue;
            }

            if (Character.isDigit(current)) {
                result.add(tokenizeNumber());
                continue;
            }

            if (Character.isLetter(current)) {
                result.add(tokenizeWord());
                continue;
            }

            result.add(tokenizeOperatorOrPunctuation());
        }

        result.add(new Token(TokenType.EOF, "\0", position));
        return result;
    }

    private Token tokenizeNumber() {
        int start = position;

        while (Character.isDigit(peek())) {
            column++;
            next();
        }

        String value = input.substring(start, position);
        return new Token(TokenType.NUMBER, value, start);
    }

    private Token tokenizeWord() {
        int start = position;

        while (Character.isLetterOrDigit(peek())) {
            column++;
            next();
        }

        String word = input.substring(start, position);

        TokenType type = KEYWORDS.getOrDefault(word, TokenType.ID);

        return new Token(type, word, start);
    }

    private Token tokenizeOperatorOrPunctuation() throws Exception {
        int start = position;
        int startColumn = column;

        if (position + 1 < length) {
            String twoChars = input.substring(position, position + 2);
            if (OPERATORS.containsKey(twoChars)) {
                column += 2;
                next();
                next();
                return new Token(OPERATORS.get(twoChars), twoChars, start);
            }
        }

        String oneChar = String.valueOf(peek());
        if (OPERATORS.containsKey(oneChar)) {
            column++;
            next();
            return new Token(OPERATORS.get(oneChar), oneChar, start);
        }

        char badChar = peek();
        throw new Exception(String.format(
            "[Lexer Error] Unexpected character '%c' at position %d (line %d, column %d)",
            badChar, position, line, startColumn
        ));
    }

    private char peek() {
        if (position >= length) {
            return '\0';
        }
        return input.charAt(position);
    }

    private char next() {
        if (position >= length) {
            return '\0';
        }
        return input.charAt(position++);
    }
}
