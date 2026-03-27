package org.example.semantic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SemanticEnvironment {
    public static class VariableInfo {
        private final String name;
        private final int line;
        private final int column;
        private ValueType type;
        private boolean used;

        public VariableInfo(String name, int line, int column, ValueType type) {
            this.name = name;
            this.line = line;
            this.column = column;
            this.type = type;
            this.used = false;
        }

        public String getName() {
            return name;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }

        public boolean isUsed() {
            return used;
        }

        public ValueType getType() {
            return type;
        }

        public void setType(ValueType type) {
            this.type = type;
        }

        private void markUsed() {
            this.used = true;
        }
    }

    private final SemanticEnvironment parent;
    private final Map<String, VariableInfo> variables;

    public SemanticEnvironment() {
        this(null);
    }

    public SemanticEnvironment(SemanticEnvironment parent) {
        this.parent = parent;
        this.variables = new LinkedHashMap<>();
    }

    public boolean defineVariable(String name) {
        return defineVariable(name, -1, -1, ValueType.UNKNOWN);
    }

    public boolean defineVariable(String name, int line, int column) {
        return defineVariable(name, line, column, ValueType.UNKNOWN);
    }

    public boolean defineVariable(String name, int line, int column, ValueType type) {
        if (variables.containsKey(name)) {
            return false;
        }
        variables.put(name, new VariableInfo(name, line, column, type));
        return true;
    }

    public boolean isVariableDefined(String name) {
        return resolveVariable(name) != null;
    }

    public boolean markVariableRead(String name) {
        VariableInfo info = resolveVariable(name);
        if (info == null) {
            return false;
        }
        info.markUsed();
        return true;
    }

    public VariableInfo getVariable(String name) {
        return resolveVariable(name);
    }

    public List<VariableInfo> getUnusedVariablesInCurrentScope() {
        List<VariableInfo> unused = new ArrayList<>();
        for (VariableInfo variable : variables.values()) {
            if (!variable.isUsed()) {
                unused.add(variable);
            }
        }
        return unused;
    }

    private VariableInfo resolveVariable(String name) {
        VariableInfo local = variables.get(name);
        if (local != null) {
            return local;
        }
        return parent != null ? parent.resolveVariable(name) : null;
    }
}
