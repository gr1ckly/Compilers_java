package org.example.parser.ast.statement;

import java.util.List;

public class BlockStatement extends Statement {
    public List<Statement> statements;

    public BlockStatement(List<Statement> statements) {
        this.statements = statements;
    }
}
