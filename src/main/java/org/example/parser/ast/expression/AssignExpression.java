package org.example.parser.ast.expression;

public class AssignExpression extends Expression {
    public String name;
    public Expression value;

    public AssignExpression(String name, Expression value) {
        this.name = name;
        this.value = value;
    }
}
