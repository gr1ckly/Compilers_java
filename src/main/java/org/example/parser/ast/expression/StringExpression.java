package org.example.parser.ast.expression;

public class StringExpression extends Expression {
    public String value;

    public StringExpression(String value) {
        this.value = value;
    }
}
