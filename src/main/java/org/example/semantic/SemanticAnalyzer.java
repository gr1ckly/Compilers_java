package org.example.semantic;

import org.example.parser.ast.expression.*;
import org.example.parser.ast.statement.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SemanticAnalyzer {
    private SemanticEnvironment environment = new SemanticEnvironment();
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public void analyze(List<Statement> statements) {
        environment = new SemanticEnvironment();
        errors.clear();
        warnings.clear();

        for (Statement statement : statements) {
            visitStatement(statement);
        }

        collectUnusedVariablesWarnings();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    private void visitStatement(Statement statement) {
        if (statement instanceof VarStatement varStatement) {
            if (varStatement.initializer != null) {
                visitExpression(varStatement.initializer);
            }

            if (!environment.defineVariable(varStatement.name, varStatement.line, varStatement.column)) {
                errors.add(String.format("Variable '%s' is already defined.", varStatement.name));
            }
            return;
        }

        if (statement instanceof PrintStatement printStatement) {
            visitExpression(printStatement.expression);
            return;
        }

        if (statement instanceof ExpressionStatement expressionStatement) {
            visitExpression(expressionStatement.expression);
            return;
        }

        if (statement instanceof BlockStatement blockStatement) {
            SemanticEnvironment previous = environment;
            environment = new SemanticEnvironment(previous);
            for (Statement inner : blockStatement.statements) {
                visitStatement(inner);
            }
            collectUnusedVariablesWarnings();
            environment = previous;
            return;
        }

        if (statement instanceof IfStatement ifStatement) {
            visitExpression(ifStatement.condition);
            visitStatement(ifStatement.thenBranch);
            if (ifStatement.elseBranch != null) {
                visitStatement(ifStatement.elseBranch);
            }
            return;
        }

        if (statement instanceof WhileStatement whileStatement) {
            visitExpression(whileStatement.condition);
            visitStatement(whileStatement.body);
            return;
        }

        errors.add(String.format("Unsupported statement type: %s", statement.getClass().getSimpleName()));
    }

    private void visitExpression(Expression expression) {
        if (expression instanceof NumberExpression || expression instanceof StringExpression) {
            return;
        }

        if (expression instanceof VariableExpression variableExpression) {
            if (!environment.markVariableRead(variableExpression.name)) {
                errors.add(String.format("Variable '%s' is not defined.", variableExpression.name));
            }
            return;
        }

        if (expression instanceof AssignExpression assignExpression) {
            visitExpression(assignExpression.value);
            if (!environment.isVariableDefined(assignExpression.name)) {
                errors.add(String.format("Variable '%s' is not defined.", assignExpression.name));
            }
            return;
        }

        if (expression instanceof BinaryExpression binaryExpression) {
            visitExpression(binaryExpression.left);
            visitExpression(binaryExpression.right);
            return;
        }

        if (expression instanceof UnaryExpression unaryExpression) {
            visitExpression(unaryExpression.right);
            return;
        }

        errors.add(String.format("Unsupported expression type: %s", expression.getClass().getSimpleName()));
    }

    private void collectUnusedVariablesWarnings() {
        for (SemanticEnvironment.VariableInfo variable : environment.getUnusedVariablesInCurrentScope()) {
            if (variable.getLine() > 0 && variable.getColumn() > 0) {
                warnings.add(String.format(
                    "Variable '%s' declared at line %d, column %d is never used.",
                    variable.getName(), variable.getLine(), variable.getColumn()
                ));
            } else {
                warnings.add(String.format("Variable '%s' is never used.", variable.getName()));
            }
        }
    }
}
