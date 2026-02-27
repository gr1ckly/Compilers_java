package org.example.parser.ast.expression;

import org.example.lexer.types.TokenType;

public class BinaryExpression extends Expression {
    public Expression left;
    public TokenType operator;
    public Expression right;

    public BinaryExpression(Expression left, TokenType operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
}
