package org.example.parser.ast.statement;

import org.example.parser.ast.expression.Expression;

public class ExpressionStatement extends Statement {
    public Expression expression;

    public ExpressionStatement(Expression expression) {
        this.expression = expression;
    }
}
