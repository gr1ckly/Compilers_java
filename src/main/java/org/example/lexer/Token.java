package org.example.lexer;

import lombok.Getter;
import org.example.lexer.types.TokenType;

@Getter

public class Token {

    public TokenType tokenType;
    public String value;
    public int position;
    public int line;
    public int column;

    public Token(TokenType tokenType, String value, int position) {
        this(tokenType, value, position, -1, -1);
    }

    public Token(TokenType tokenType, String value, int position, int line, int column) {
        this.tokenType = tokenType;
        this.value = value;
        this.position = position;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return "Token{" +
                "tokenType=" + tokenType +
                ", value='" + value + '\'' +
                ", position=" + position +
                ", line=" + line +
                ", column=" + column +
                '}';
    }
}
