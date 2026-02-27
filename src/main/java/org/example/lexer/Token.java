package org.example.lexer;

import lombok.Getter;
import org.example.lexer.types.TokenType;

@Getter

public class Token {

    public TokenType tokenType;
    public String value;
    public int position;

    public Token(TokenType tokenType, String value, int position) {
        this.tokenType = tokenType;
        this.value = value;
        this.position = position;
    }

    @Override
    public String toString() {
        return "Token{" +
                "tokenType=" + tokenType +
                ", value='" + value + '\'' +
                ", position=" + position +
                '}';
    }
}
