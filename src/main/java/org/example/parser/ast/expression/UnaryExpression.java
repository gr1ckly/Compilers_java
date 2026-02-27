package org.example.parser.ast.expression;

import org.example.lexer.types.TokenType;

public class UnaryExpression extends Expression {
    public TokenType operator;
    public Expression right;

    public UnaryExpression(TokenType operator, Expression right) {
        this.operator = operator;
        this.right = right;
    }
}
