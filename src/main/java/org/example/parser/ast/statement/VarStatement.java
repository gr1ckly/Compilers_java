package org.example.parser.ast.statement;

import org.example.parser.ast.expression.Expression;

public class VarStatement extends Statement {
    public String name;
    public Expression initializer;
    public int line;
    public int column;

    public VarStatement(String name, Expression initializer) {
        this(name, initializer, -1, -1);
    }

    public VarStatement(String name, Expression initializer, int line, int column) {
        this.name = name;
        this.initializer = initializer;
        this.line = line;
        this.column = column;
    }
}
