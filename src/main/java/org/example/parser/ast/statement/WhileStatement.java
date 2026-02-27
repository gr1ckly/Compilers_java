package org.example.parser.ast.statement;

import org.example.parser.ast.expression.Expression;

public class WhileStatement extends Statement {
    public Expression condition;
    public Statement body;

    public WhileStatement(Expression condition, Statement body) {
        this.condition = condition;
        this.body = body;
    }
}
