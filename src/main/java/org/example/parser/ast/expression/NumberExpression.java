package org.example.parser.ast.expression;

public class NumberExpression extends Expression {
    public double value;

    public NumberExpression(double value) {
        this.value = value;
    }
}
