package com.example.coursecompiler;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder article = new StringBuilder();
        while (scanner.hasNextLine()) {
            article.append(scanner.nextLine());
        }

        Lexer lexer = new Lexer(article.toString());
        Parser parser = new Parser(lexer);
        var ast = parser.parse();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        semanticAnalyzer.semanticAnalysis(ast);
        CodeGenerator codeGenerator = new CodeGenerator();
        codeGenerator.codegen(ast);

    }
}

class Test {
    String article;
    String expected;

    public Test(String expected, String article) {
        this.article = article;
        this.expected = expected;
    }
}
