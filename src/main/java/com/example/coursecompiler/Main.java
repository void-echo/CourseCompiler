package com.example.coursecompiler;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        var article = new StringBuilder();
        while (scanner.hasNextLine()) article.append(scanner.nextLine());
        var lexer = new Lexer(article.toString());
        var parser = new Parser(lexer);
        var ast = parser.parse();
        var semanticAnalyzer = new SemanticAnalyzer();
        semanticAnalyzer.semanticAnalysis(ast);
        var codeGenerator = new CodeGenerator();
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
