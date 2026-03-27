package org.example.semantic;

import org.example.lexer.types.TokenType;
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
            ValueType initializerType = ValueType.UNKNOWN;
            if (varStatement.initializer != null) {
                initializerType = visitExpression(varStatement.initializer);
            }

            if (!environment.defineVariable(varStatement.name, varStatement.line, varStatement.column, initializerType)) {
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
            ValueType conditionType = visitExpression(ifStatement.condition);
            requireBooleanCondition(conditionType, "if");
            visitStatement(ifStatement.thenBranch);
            if (ifStatement.elseBranch != null) {
                visitStatement(ifStatement.elseBranch);
            }
            return;
        }

        if (statement instanceof WhileStatement whileStatement) {
            ValueType conditionType = visitExpression(whileStatement.condition);
            requireBooleanCondition(conditionType, "while");
            visitStatement(whileStatement.body);
            return;
        }

        errors.add(String.format("Unsupported statement type: %s", statement.getClass().getSimpleName()));
    }

    private ValueType visitExpression(Expression expression) {
        if (expression instanceof NumberExpression || expression instanceof StringExpression) {
            return expression instanceof NumberExpression ? ValueType.NUMBER : ValueType.STRING;
        }

        if (expression instanceof VariableExpression variableExpression) {
            SemanticEnvironment.VariableInfo variable = environment.getVariable(variableExpression.name);
            if (variable == null) {
                errors.add(String.format("Variable '%s' is not defined.", variableExpression.name));
                return ValueType.ERROR;
            }

            environment.markVariableRead(variableExpression.name);
            return variable.getType();
        }

        if (expression instanceof AssignExpression assignExpression) {
            ValueType valueType = visitExpression(assignExpression.value);
            SemanticEnvironment.VariableInfo variable = environment.getVariable(assignExpression.name);

            if (variable == null) {
                errors.add(String.format("Variable '%s' is not defined.", assignExpression.name));
                return ValueType.ERROR;
            }

            if (valueType == ValueType.ERROR) {
                return ValueType.ERROR;
            }

            if (variable.getType() == ValueType.UNKNOWN) {
                variable.setType(valueType);
                return valueType;
            }

            if (!isAssignable(variable.getType(), valueType)) {
                errors.add(String.format(
                    "Cannot assign value of type '%s' to variable '%s' of type '%s'.",
                    valueType, assignExpression.name, variable.getType()
                ));
                return ValueType.ERROR;
            }

            return variable.getType();
        }

        if (expression instanceof BinaryExpression binaryExpression) {
            ValueType leftType = visitExpression(binaryExpression.left);
            ValueType rightType = visitExpression(binaryExpression.right);
            return resolveBinaryType(binaryExpression.operator, leftType, rightType);
        }

        if (expression instanceof UnaryExpression unaryExpression) {
            ValueType operandType = visitExpression(unaryExpression.right);
            return resolveUnaryType(unaryExpression.operator, operandType);
        }

        errors.add(String.format("Unsupported expression type: %s", expression.getClass().getSimpleName()));
        return ValueType.ERROR;
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

    private void requireBooleanCondition(ValueType conditionType, String statementKind) {
        if (conditionType == ValueType.ERROR) {
            return;
        }

        if (conditionType != ValueType.BOOLEAN) {
            errors.add(String.format(
                "Condition of '%s' must have type 'boolean', but got '%s'.",
                statementKind, conditionType
            ));
        }
    }

    private ValueType resolveBinaryType(TokenType operator, ValueType leftType, ValueType rightType) {
        if (leftType == ValueType.ERROR || rightType == ValueType.ERROR) {
            return ValueType.ERROR;
        }

        return switch (operator) {
            case PLUS -> resolvePlusType(leftType, rightType);
            case MINUS, STAR, SLASH -> requireOperandTypes(operator, leftType, rightType, ValueType.NUMBER, ValueType.NUMBER);
            case LT, LTEQ, GT, GTEQ -> requireComparableNumberTypes(operator, leftType, rightType);
            case EQEQ, NEQ -> resolveEqualityType(leftType, rightType);
            case AND, OR -> requireOperandTypes(operator, leftType, rightType, ValueType.BOOLEAN, ValueType.BOOLEAN);
            default -> {
                errors.add(String.format("Unsupported binary operator '%s'.", operator));
                yield ValueType.ERROR;
            }
        };
    }

    private ValueType resolveUnaryType(TokenType operator, ValueType operandType) {
        if (operandType == ValueType.ERROR) {
            return ValueType.ERROR;
        }

        return switch (operator) {
            case MINUS -> requireUnaryOperandType(operator, operandType, ValueType.NUMBER, ValueType.NUMBER);
            case EXCL -> requireUnaryOperandType(operator, operandType, ValueType.BOOLEAN, ValueType.BOOLEAN);
            default -> {
                errors.add(String.format("Unsupported unary operator '%s'.", operator));
                yield ValueType.ERROR;
            }
        };
    }

    private ValueType resolvePlusType(ValueType leftType, ValueType rightType) {
        if (leftType == ValueType.NUMBER && rightType == ValueType.NUMBER) {
            return ValueType.NUMBER;
        }

        if (leftType == ValueType.STRING && rightType == ValueType.STRING) {
            return ValueType.STRING;
        }

        errors.add(String.format(
            "Operator '+' cannot be applied to operands of types '%s' and '%s'.",
            leftType, rightType
        ));
        return ValueType.ERROR;
    }

    private ValueType requireComparableNumberTypes(TokenType operator, ValueType leftType, ValueType rightType) {
        ValueType result = requireOperandTypes(operator, leftType, rightType, ValueType.NUMBER, ValueType.NUMBER);
        return result == ValueType.ERROR ? ValueType.ERROR : ValueType.BOOLEAN;
    }

    private ValueType resolveEqualityType(ValueType leftType, ValueType rightType) {
        if (leftType == ValueType.UNKNOWN || rightType == ValueType.UNKNOWN) {
            errors.add(String.format(
                "Cannot compare values with unresolved types '%s' and '%s'.",
                leftType, rightType
            ));
            return ValueType.ERROR;
        }

        if (leftType != rightType) {
            errors.add(String.format(
                "Operator '=='/'!=' requires operands of the same type, but got '%s' and '%s'.",
                leftType, rightType
            ));
            return ValueType.ERROR;
        }

        return ValueType.BOOLEAN;
    }

    private ValueType requireOperandTypes(TokenType operator, ValueType leftType, ValueType rightType,
                                          ValueType expectedLeft, ValueType expectedRight) {
        if (leftType == expectedLeft && rightType == expectedRight) {
            return expectedLeft;
        }

        errors.add(String.format(
            "Operator '%s' cannot be applied to operands of types '%s' and '%s'.",
            operator, leftType, rightType
        ));
        return ValueType.ERROR;
    }

    private ValueType requireUnaryOperandType(TokenType operator, ValueType operandType,
                                              ValueType expectedType, ValueType resultType) {
        if (operandType == expectedType) {
            return resultType;
        }

        errors.add(String.format(
            "Operator '%s' cannot be applied to operand of type '%s'.",
            operator, operandType
        ));
        return ValueType.ERROR;
    }

    private boolean isAssignable(ValueType variableType, ValueType valueType) {
        return variableType == ValueType.UNKNOWN || variableType == valueType;
    }
}
