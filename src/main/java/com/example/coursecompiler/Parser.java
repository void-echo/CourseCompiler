package com.example.coursecompiler;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    Lexer lexer;
    Token current_token;
    String current_func_name = "";

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        current_token = lexer.nxt_token();
    }

    public Token nxt_token() {
        return lexer.nxt_token();
    }

    public void eat(String token_type) {
        if (current_token.type.equals(token_type)) {
            current_token = nxt_token();
        } else {
            throw new RuntimeException("Expected token of type " + token_type + " but got " + current_token.type);
        }
    }

    // primary := "(" expression ")" | identifier args? | num | identifier "[" expression "]"
    // args := "(" (expression ("," expression)*)? ")"
    public ASTNode primary() {
        var tk = current_token;
        // "(" expr ")"
        if (tk.type.equals("(")) {
            eat("(");
            var node = expr();
            eat(")");
            return node;
        }

        if (tk.type.equals("ID")) {
            tk = current_token;
            eat("ID");
            // func call
            if (current_token.type.equals("(")) {
                var func_name = tk.value;
                eat("(");
                var args = new ArrayList<ASTNode>();
                if (!current_token.type.equals(")")) {
                    var node = expr();
                    args.add(node);
                }

                while (current_token.type.equals(",")) {
                    eat(",");
                    var node = expr();
                    args.add(node);
                }

                eat(")");
                return new functionCalNode(func_name, args, tk);
            }
            // arr_item
            if (current_token.type.equals("[")) {
                eat("[");
                var index = expr();
                eat("]");
                return new varArrayItemNode(tk, index);
            }
            // var
            return new varNode(tk);
        }

        // num or false or true
        if (tk.type.equals("INT_CONST") || tk.type.equals("false") || tk.type.equals("true")) {
            eat(tk.type);
            return new numNode(tk);
        } else {
            throw new RuntimeException("Expected primary but got " + tk.type);
        }
    }

    // unary := ("+" | "-" | "!") unary | primary
    public ASTNode unary() {
        var tk = current_token;
        if (tk.type.equals("+") || tk.type.equals("-") || tk.type.equals("!")) {
            eat(tk.type);
            var node = unary();
            return new unaryOpNode(tk, node);
        } else {
            return primary();
        }
    }

    // mul_div := unary ("*" unary | "/" unary)*
    public ASTNode mul_div() {
        var node = unary();
        while (true) {
            var tk = current_token;
            if (tk.type.equals("*") || tk.type.equals("/")) {
                eat(tk.type);
                node = new binaryOpNode(node, tk, unary());
                continue;
            }
            return node;
        }
    }

    // add-sub := mul_div ("+" mul_div | "-" mul_div)*
    public ASTNode add_sub() {
        var node = mul_div();
        while (true) {
            var tk = current_token;
            if (tk.type.equals("+") || tk.type.equals("-")) {
                eat(tk.type);
                node = new binaryOpNode(node, tk, mul_div());
                continue;
            }
            return node;
        }
    }

    // relational := add_sub ("<" add_sub | "<=" add_sub | ">" add_sub | ">=" add_sub)*
    public ASTNode relational() {
        var node = add_sub();
        while (true) {
            var tk = current_token;
            if (tk.type.equals("<") || tk.type.equals("<=") || tk.type.equals(">") || tk.type.equals(">=")) {
                eat(tk.type);
                node = new binaryOpNode(node, tk, add_sub());
                continue;
            }
            return node;
        }
    }

    // equality := relational ("==" relational | "! =" relational)*
    public ASTNode equality() {
        var node = relational();
        while (true) {
            var tk = current_token;
            if (tk.type.equals("==") || tk.type.equals("!=")) {
                eat(tk.type);
                node = new binaryOpNode(node, tk, relational());
                continue;
            }
            return node;
        }
    }

    // logic := equality ("&&" equality | "||" equality)*
    public ASTNode logic() {    // 对
        var node = equality();
        while (true) {
            var tk = current_token;
            if (tk.type.equals("&&") || tk.type.equals("||")) {
                eat(tk.type);
                node = new binaryOpNode(node, tk, equality());
                continue;
            }
            return node;
        }
    }

    // expression := logic ("=" expression)?
    public ASTNode expr() {
        var node = logic();
        var tk = current_token;
        if (tk.type.equals("=")) {
            eat("=");
            node = new assignNode(node, tk, expr());
        }
        return node;
    }

    public ASTNode expr_stat() {
        var tk = current_token;
        ASTNode node = null;
        if (tk.type.equals(";")) {
            eat(";");
        } else {
            node = expr();
            if (current_token.type.equals(";")) {
                eat(";");
            } else {
                throw new RuntimeException("Expected ; but got " + current_token.type);
            }
        }
        return node;
    }

    // statement := expression-statement
    //            | "return" expression-statement
    //            | block
    //            | "if" "(" expression ")" statement ("else" statement)?
    //            | "while" "(" expression ")" statement
    public ASTNode statement() {
        var tk = current_token;
        switch (tk.type) {
            case "return":
                eat("return");
                return new returnNode(tk, expr_stat(), current_func_name);
            case "{":
                return block();
            case "if": {
                ASTNode else_node = null, then_stat = null;
                eat("if");
                eat("(");
                var cond = expr();
                eat(")");
                if (current_token.type.equals("then")) {
                    eat("then");
                    then_stat = statement();
                    if (current_token.type.equals("else")) {
                        eat("else");
                        else_node = statement();
                    }
                }
                return new ifNode(cond, then_stat, else_node);
            }
            case "while": {
                ASTNode cond = null, stat = null;
                eat("while");
                if (current_token.type.equals("(")) {
                    eat("(");
                    cond = expr();
                    eat(")");
                    stat = statement();
                }
                return new whileNode(cond, stat);
            }
            default:
                return expr_stat();
        }
    }

    // type_specification := int | bool
    public typeNode type_spec() {
        var tk = current_token;
        if (tk.type.equals("int") || tk.type.equals("bool")) {
            eat(tk.type);
            return new typeNode(tk);
        } else {
            throw new RuntimeException("Expected type_spec but got " + tk.type);
        }
    }

    // variable_declaration := type_specification identifier ("," ID)* ";"
    //                      | type_specification identifier "[" num "]" ("=" "{" (num)? ("," num)* "}")? ";"

    public List<varDeclNode> var_decl() {
        var type_n = type_spec();
        List<varDeclNode> vars = new ArrayList<>();
        while (!current_token.type.equals(";")) {
            var tk = current_token;
            if (tk.type.equals("ID")) {
                eat("ID");
                if (current_token.type.equals("[")) {
                    int arr_size = -1;
                    List<Integer> arr_items = new ArrayList<>();
                    eat("[");
                    if (current_token.type.equals("INT_CONST")) {
                        arr_size = Integer.parseInt(current_token.value);
                        eat("INT_CONST");
                        if (current_token.type.equals("]")) {
                            eat("]");
                        } else {
                            throw new RuntimeException("Expected ] but got " + current_token.type);
                        }
                    }
                    if (current_token.type.equals("=")) {
                        eat("=");
                        if (current_token.type.equals("{")) {
                            eat("{");
                            while (!current_token.type.equals("}")) {
                                if (current_token.type.equals("INT_CONST")) {
                                    arr_items.add(Integer.parseInt(current_token.value));
                                    eat("INT_CONST");
                                }
                                if (current_token.type.equals(",")) {
                                    eat(",");
                                    if (current_token.type.equals("INT_CONST")) {
                                        arr_items.add(Integer.parseInt(current_token.value));
                                        eat("INT_CONST");
                                    } else {
                                        throw new RuntimeException("Expected integer const value but got " + current_token.type);
                                    }
                                }
                            }
                            eat("}");
                            if (arr_size == -1) {
                                throw new RuntimeException("Array size is not specified");
                            }
                            if (arr_size != arr_items.size()) {
                                throw new RuntimeException("Array size is not equal to the number of elements");
                            }
                            _array arr = new _array("" + arr_size, arr_items);
                            varNode var_n = new varNode(tk, arr);
                            vars.add(new varDeclNode(type_n, var_n));
                        } else {
                            throw new RuntimeException("Expected { but got " + current_token.type);
                        }
                    }
                } else {
                    varNode varNode = new varNode(tk);
                    varDeclNode decl_node = new varDeclNode(type_n, varNode);
                    vars.add(decl_node);
                    if (current_token.type.equals(",")) {
                        eat(",");
                    }
                    while (!current_token.type.equals(";")) {
                        if (current_token.type.equals("ID")) {
                            varNode = new varNode(current_token);
                            decl_node = new varDeclNode(type_n, varNode);
                            eat("ID");
                            vars.add(decl_node);
                            if (current_token.type.equals(",")) {
                                eat(",");
                            }
                        }
                    }
                }
            }
        }
        eat(";");
        return vars;
    }

    // compound_statement := (variable_declaration | statement)*
    //    def compound_statement(self):
    public List<ASTNode> compound_statement() {
        List<ASTNode> nodes = new ArrayList<>();
        while (!current_token.type.equals("}") && !current_token.type.equals("EOF")) {
            if (current_token.type.equals("int") || current_token.type.equals("bool")) {
                nodes.addAll(var_decl());
            } else {
                ASTNode statement = statement();
                // add if not null
                if (statement != null) nodes.add(statement);
                else UnitedLog.warn("statement is null, maybe it's a `;`. ");
            }
        }
        return nodes;
    }


    // block := "{" compound_statement "}"
    public ASTNode block() {
        if (current_token.type.equals("{")) {
            var left_tk = current_token;
            eat("{");
            var statements = compound_statement();
            var right_tk = current_token;
            eat("}");
            return new blockNode(left_tk, right_tk, statements);
        } else {
            UnitedLog.warn("In block(), expected { but got " + current_token.type);
            return null;
        }
    }

    // formal_parameter := type_specification identifier
    public formalParamNode formal_param() {
        var type_n = type_spec();
        var param_node = new varNode(current_token);
        eat("ID");
        return new formalParamNode(type_n, param_node);
    }

    // formal_parameters := formal_parameter (, formal_parameter)*
    public List<formalParamNode> formal_params() {
        List<formalParamNode> params = new ArrayList<>();
        params.add(formal_param());
        while (!current_token.type.equals(")")) {
            if (current_token.type.equals(",")) {
                eat(",");
                params.add(formal_param());
            }
            else {
                throw new RuntimeException("Parameter list Syntax Error");
            }
        }
        return params;
    }

    // function_definition := type_specification identifier "(" formal_parameters? ")" block
    public functionDefNode func_def() {
        var type_n = type_spec();
        var func_name = current_token.value;
        eat("ID");
        List<formalParamNode> params = new ArrayList<>();
        if (current_token.type.equals("(")) {
            eat("(");

            if (!current_token.type.equals(")")) {
                params.addAll(formal_params());
            }
            eat(")");
        }

        current_func_name = func_name;
        if (current_token.type.equals("{")) {
            var block_node = block();
            return new functionDefNode(type_n, func_name, params, block_node);
        } else {
            throw new RuntimeException("In Function Definition, expected { but got " + current_token.type);
        }
    }

    public List<ASTNode> parse() {
        var func_defs = new ArrayList<ASTNode>();
        while (!current_token.type.equals("EOF")) {
            var func_def = func_def();
            func_defs.add(func_def);
        }
        return func_defs;
    }

    public static void main(String[] args) {
        var text = """
                int main(int ar) {
                    int a;
                    a = 123;
                    
                    return c;
                }
                
                void foo() {
                    int a;
                    a = 123;
                }
                """;
        var lexer = new Lexer(text);
        lexer.tokenize();
        System.out.println(lexer.tokens);
        var parser = new Parser(lexer);
        System.out.println(parser.current_token);
        var ast = parser.parse();
        System.out.println(ast);
    }
}
