package org.example.parser.ast.statement;

import org.example.parser.ast.expression.Expression;

public class PrintStatement extends Statement {
    public Expression expression;

    public PrintStatement(Expression expression) {
        this.expression = expression;
    }
}
