package org.example.parser.ast.expression;

public class VariableExpression extends Expression {
    public String name;

    public VariableExpression(String name) {
        this.name = name;
    }
}
