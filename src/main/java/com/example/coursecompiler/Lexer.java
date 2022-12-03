package com.example.coursecompiler;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    public static void main(String[] args) {
        Lexer lexer = new Lexer("int main() { int x[3]={7,9,11}; x[3] = 13; return x[3];}");
        List<Token> tokens = lexer.tokenize();
        for (Token token : tokens) {
            System.out.print(token);
        }
    }

    public String article;
    public List<Token> tokens;

    public Token nxt_token() {
        if (tokens == null)
            tokens = tokenize();
        return tokens.remove(0);
    }

    public Lexer(String article) {
        this.article = article;
        this.allTokenTypes = new ArrayList<>();
        String[] singleCharTokenTypes = new String[]{"(", ")", "{", "}", ",", ";", "+", "-", "*", "/", "[", "]", "!"};
        List<String> singleCharTokens = new ArrayList<>(List.of(singleCharTokenTypes));
        allTokenTypes.addAll(singleCharTokens);
        allTokenTypes.addAll(List.of("==", "!=", ">=", "<=", "&&", "||", ">", "<", "="));
        allTokenTypes.addAll(List.of("if", "else", "while", "int", "bool", "return", "EOF", "then"));
        allTokenTypes.addAll(List.of("true", "false", "ID", "INT_CONST"));
    }

    public final List<String> allTokenTypes;

    public boolean checkValid(Token token) {
        return allTokenTypes.contains(token.type);
    }

    public List<Token> tokenize() {
        var li = new ArrayList<Token>();
        int line = 1, pos = 1;
        try {
            for (int i = 0; i < article.length(); i++) {
                char now_char = article.charAt(i);
                switch (now_char) {
                    // skip space or tab or newline
                    case ' ' -> pos++;
                    case '\t' -> pos += 4;
                    // this '\r' char is ignored, because on WINDOWS, '\r' is followed by '\n'.
                    // so '\r' is not a newline, it is a carriage return.
                    // newline processing is done in '\n' case.
                    case '\r' -> {
                    }
                    // ignore newline
                    case '\n' -> {
                        line++;
                        pos = 1;
                    }
                    // skip comment
                    case '/' -> {
                        if (article.charAt(i + 1) == '/') {
                            while (article.charAt(i) != '\n') {
                                i++;
                            }
                            line++;
                            pos = 1;
                        } else if (article.charAt(i + 1) == '*') {
                            while (article.charAt(i) != '*' && article.charAt(i + 1) != '/') {
                                if (article.charAt(i) == '\n') {
                                    line++;
                                    pos = 1;
                                }
                                i++;
                            }
                            i++;
                        } else {
                            li.add(new Token("/"));
                            pos++;
                        }
                    }

                    case ',', ';', '{', '}', '(', ')', '*', '+', '-', '[', ']' -> {
                        li.add(new Token("" + now_char));
                        pos++;
                    }

                    case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                        StringBuilder num = new StringBuilder();
                        while (Character.isDigit(now_char)) {
                            num.append(now_char);
                            i++;
                            now_char = article.charAt(i);
                        }
                        i--;
                        li.add(new Token("INT_CONST", num.toString()));
                        pos += num.length();
                    }

                    case '&', '|' -> {
                        // expect next char to be '&' or '|'
                        if (article.charAt(i + 1) == now_char) {
                            li.add(new Token("" + now_char + now_char));
                            i++;
                            pos += 2;
                        } else {
                            UnitedLog.err("Invalid token: %s at line %d, pos %d".formatted("" + now_char, line, pos));
                            System.exit(1);
                        }
                    }

                    case '<', '>', '=', '!' -> {
                        if (article.charAt(i + 1) == '=') {
                            li.add(new Token("" + now_char + '='));
                            i++;
                            pos += 2;
                        } else {
                            li.add(new Token("" + now_char));
                            pos++;
                        }
                    }

                    // identifier case.
                    // it is possible that the identifier is a keyword.
                    // the keyword is checked in the constructor of Token class.
                    case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                            'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_' -> {
                        StringBuilder id = new StringBuilder();
                        while (Character.isLetterOrDigit(now_char) || now_char == '_') {
                            id.append(now_char);
                            i++;
                            now_char = article.charAt(i);
                        }
                        i--;
                        checkTokenIsIdAndAdd("ID", li, id.toString());
                        pos += id.length();
                    }


                    default -> {
                        UnitedLog.err("unknown char " + UnitedLog.RED_BOLD_BRIGHT + '\"' + now_char + '\"' + UnitedLog.RESET +
                                " at line " + UnitedLog.RED_BOLD_BRIGHT + line + UnitedLog.RESET +
                                " pos " + UnitedLog.RED_BOLD_BRIGHT + pos + UnitedLog.RESET);
                        System.exit(1);
                    }
                }
            }
        } catch (IndexOutOfBoundsException ignored) {

        } finally {
            // check all tokens is type in allTokenTypes
            li.add(new Token("EOF"));
            for (var token : li) {
                if (!checkValid(token)) {
                    UnitedLog.err("Unknown token type: %s".formatted(token.type));
                    System.exit(1);
                }
            }
//            UnitedLog.print("Lexer finished.");
        }
        tokens = li;
        return li;
    }

    private void checkTokenIsIdAndAdd(String type, List<Token> tokens, String maybeId) {
        if (type.equals("ID")) {
            // if "EOF", raise error
            if (maybeId.equals("EOF")) {
                UnitedLog.err("Cannot use EOF as an identifier. Program exit.");
                System.exit(1);
            }
            for (var keyword : List.of("if", "else", "while", "int", "bool", "return", "true", "false", "then")) {
                if (keyword.equals(maybeId)) {
                    type = keyword;
                    tokens.add(new Token(type));
                    return;
                }
            }
            tokens.add(new Token(type, maybeId));
        } else {
            UnitedLog.err("Unknown token type: %s".formatted(type));
        }
    }
}
