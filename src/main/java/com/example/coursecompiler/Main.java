package com.example.coursecompiler;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String article = """
                int main() { int x[3]={7,9,11}; x[3] = 13; return x[3];}
                 """;
        Lexer lexer = new Lexer(article);
        Parser parser = new Parser(lexer);
        System.out.println(lexer.tokens);
        List<ASTNode> parse = parser.parse();
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.semanticAnalysis(parse);
        CodeGenerator generator = new CodeGenerator();
        generator.codegen(parse);
    }
}
