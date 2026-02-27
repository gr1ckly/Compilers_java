package org.example.parser.ast.statement;

import org.example.parser.ast.expression.Expression;

public class VarStatement extends Statement {
    public String name;
    public Expression initializer;

    public VarStatement(String name, Expression initializer) {
        this.name = name;
        this.initializer = initializer;
    }
}
