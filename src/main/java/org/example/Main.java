package org.example;

import org.example.lexer.Lexer;
import org.example.lexer.Token;
import org.example.parser.Parser;
import org.example.parser.ast.statement.Statement;
import org.example.semantic.SemanticAnalyzer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    private static final String DEFAULT_PROGRAM = """
        var greeting = "Hello, compiler!";
        var counter = 3;
        print greeting;

        while (counter > 0) {
            print greeting;
            counter = counter - 1;
        }
        """;

    public static void main(String[] args) {
        try {
            String source = readSource(args);

            System.out.println("=== Source ===");
            System.out.println(source);

            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();
            printTokens(tokens);

            Parser parser = new Parser(tokens);
            List<Statement> statements = parser.parse();
            System.out.printf("%nParsed %d top-level statement(s).%n", statements.size());

            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            analyzer.analyze(statements);

            printMessages("Semantic warnings", analyzer.getWarnings());
            printMessages("Semantic errors", analyzer.getErrors());
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            System.exit(1);
        }
    }

    private static String readSource(String[] args) throws Exception {
        if (args.length == 0) {
            return DEFAULT_PROGRAM;
        }

        Path path = Path.of(args[0]);
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static void printTokens(List<Token> tokens) {
        System.out.println("=== Tokens ===");
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    private static void printMessages(String title, List<String> messages) {
        System.out.printf("%n=== %s ===%n", title);
        if (messages.isEmpty()) {
            System.out.println("None");
            return;
        }

        for (String message : messages) {
            System.out.println("- " + message);
        }
    }
}
