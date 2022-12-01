package com.example.coursecompiler;

import java.util.List;

public class SemanticAnalyzer extends Visitor {
    ScopedSymbolTable currentScope;
    boolean DEBUG = false;
    public static int offset_sum = -999;

    public void semanticAnalysis(List<ASTNode> tree) {
        tree.forEach(node -> {
            if (node != null) node.accept(this);
        });
    }

    public SemanticAnalyzer() {
        currentScope = new ScopedSymbolTable("global", 0, null);
    }

    public SemanticAnalyzer(boolean DEBUG) {
        this.DEBUG = DEBUG;
        currentScope = new ScopedSymbolTable("global", 0, null);
    }

    @Override
    void visit_UnaryOp_Node(ASTNode node) {
        if (node instanceof UnaryOp_Node unaryOp_node) {
            unaryOp_node.right.accept(this);
        } else {
            throw new RuntimeException("SemanticAnalyzer visit_UnaryOp_Node: node is not UnaryOp_Node");
        }
    }

    @Override
    void visit_Return_Node(ASTNode node) {
        if (node instanceof Return_Node return_node) {
            return_node.right.accept(this);
        } else {
            throw new RuntimeException("SemanticAnalyzer visit_Return_Node: node is not Return_Node");
        }
    }

    @Override
    void visit_BinaryOp_Node(ASTNode node) {
        if (node instanceof BinaryOp_Node binaryOp_node) {
            binaryOp_node.left.accept(this);
            binaryOp_node.right.accept(this);
        } else {
            throw new RuntimeException("SemanticAnalyzer visit_BinaryOp_Node: node is not BinaryOp_Node");
        }
    }

    @Override
    void visit_Assign_Node(ASTNode node) {
        if (node instanceof Assign_Node assign_node) {
            assign_node.left.accept(this);
            assign_node.right.accept(this);
        } else {
            throw new RuntimeException("SemanticAnalyzer visit_Assign_Node: node is not Assign_Node");
        }
    }

    @Override
    void visit_If_Node(ASTNode node) {
        if (node instanceof If_Node if_node) {
            if (if_node.then_stat != null) {
                if_node.then_stat.accept(this);
                if (if_node.else_stat != null) {
                    if_node.else_stat.accept(this);
                } else {
                    UnitedLog.warn("SemanticAnalyzer visit_If_Node: else_stat is null");
                }
            } else {
                UnitedLog.warn("SemanticAnalyzer visit_If_Node: then_stat is null");
            }
        } else {
            throw new RuntimeException("SemanticAnalyzer visit_If_Node: node is not If_Node");
        }
    }

    @Override
    void visit_While_Node(ASTNode node) {
        if (node instanceof While_Node while_node) {
            while_node.condition.accept(this);
            if (while_node.statement != null) {
                while_node.statement.accept(this);
            } else {
                UnitedLog.warn("SemanticAnalyzer visit_While_Node: statement is null");
            }
        } else {
            throw new RuntimeException("SemanticAnalyzer visit_While_Node: node is not While_Node");
        }
    }

    @Override
    void visit_Block_Node(ASTNode node) {
        var block_name = currentScope.scope_name + "_block" + currentScope.scope_level + 1;
        if (DEBUG)
            UnitedLog.print("ENTER scope: block_name = " + block_name);
        var pre = currentScope;
        currentScope = new ScopedSymbolTable(block_name, currentScope.scope_level + 1, pre);
        if (node instanceof Block_Node block_node) {
            for (var statement : block_node.state_nodes) {
                statement.accept(this);
            }
            currentScope = pre;
            if (DEBUG)
                UnitedLog.print("LEAVE scope: block_name = " + block_name);
        } else {
            throw new RuntimeException("SemanticAnalyzer visit_Block_Node: node is not Block_Node");
        }
    }

    @Override
    void visit_Num_Node(ASTNode node) {
        if (node instanceof Num_Node num_node) {
            if (DEBUG)
                UnitedLog.print("SemanticAnalyzer visit_Num_Node: " + num_node.token);
            // do nothing here, since we don't need to do anything with numbers
        } else {
            throw new RuntimeException("SemanticAnalyzer visit_Num_Node: node is not Num_Node");
        }
    }

    @Override
    void visit_Var_Node(ASTNode node) {
        if (node instanceof Var_Node var_node) {
            var var_name = var_node.name;
            var var_symbol = currentScope.lookup(var_name);
            var_symbol.ifPresentOrElse(sym -> var_node.symbol = sym, () -> {
                UnitedLog.err("SemanticAnalyzer visit_Var_Node: var_name = " + var_name + " is not defined");
                System.exit(1);
            });
        } else {
            throw new RuntimeException("SemanticAnalyzer visit_Var_Node: node is not Var_Node");
        }
    }

    @Override
    void visit_VarDecl_Node(ASTNode node) {
        if (node instanceof VarDecl_Node varDecl_node) {
            var __var_node = varDecl_node.varNode;
            if (__var_node == null) {
                UnitedLog.err("SemanticAnalyzer visit_VarDecl_Node: var_node is null");
                System.exit(1);
            } else {
                if (__var_node instanceof Var_Node var_node) {
                    var var_name = var_node.name;
                    if (varDecl_node.basicTypeNode == null) {
                        UnitedLog.err("SemanticAnalyzer visit_VarDecl_Node: basicTypeNode is null");
                        System.exit(1);
                    } else {
                        if (varDecl_node.basicTypeNode instanceof BasicType_Node basicType_node) {
                            // real logic
                            var type = basicType_node.value;
                            if (var_node.array != null) {
                                // array
                                int size = Integer.parseInt(var_node.array.size);
                                offset_sum += 8 * size;
                                var var_offset = -offset_sum;
                                var var_symbol = new Var_Symbol(var_name, type, var_offset);
                                var_node.symbol = var_symbol;
                                currentScope.insert(var_symbol);
                            } else {
                                // not array
                                offset_sum += 8;
                                var var_offset = -offset_sum;
                                var var_symbol = new Var_Symbol(var_name, type, var_offset);
                                currentScope.insert(var_symbol);
                            }
                        } else throw new RuntimeException("SemanticAnalyzer visit_VarDecl_Node: basicTypeNode is not BasicType_Node");
                    }
                } else throw new RuntimeException("SemanticAnalyzer visit_VarDecl_Node: var_node is not Var_Node");
            }
        } else throw new RuntimeException("SemanticAnalyzer visit_VarDecl_Node: node is not VarDecl_Node");

    }

    @Override
    void visit_FormalParam_Node(ASTNode node) {
        if (node instanceof FormalParam_Node formalParam_node) {
            var __par_n = formalParam_node.paramNode;
            if (__par_n == null) {
                UnitedLog.err("SemanticAnalyzer visit_FormalParam_Node: paramNode is null");
                System.exit(1);
            } else {
                if (__par_n instanceof Var_Node param_node) {
                    var par_name = param_node.name;
                    if (formalParam_node.basicTypeNode == null) {
                        UnitedLog.err("SemanticAnalyzer visit_FormalParam_Node: basicTypeNode is null");
                        System.exit(1);
                    } else {
                        if (formalParam_node.basicTypeNode instanceof BasicType_Node basicType_node) {
                            // real logic
                            var type = basicType_node.value;
                            offset_sum += 8;
                            var par_offset = -offset_sum;
                            var par_symbol = new Param_Symbol(par_name, type, par_offset);
                            currentScope.insert(par_symbol);
                            formalParam_node.paramSymbol = par_symbol;
                        } else {
                            throw new RuntimeException("SemanticAnalyzer visit_FormalParam_Node: basicTypeNode is not BasicType_Node");
                        }
                    }
                } else {
                    throw new RuntimeException("SemanticAnalyzer visit_FormalParam_Node: paramNode is not Param_Node");
                }
            }
        } else {
            throw new RuntimeException("SemanticAnalyzer visit_FormalParam_Node: node is not FormalParam_Node");
        }
    }

    @Override
    void visit_FunctionDef_Node(ASTNode node) {
        if (node instanceof FunctionDef_Node functionDef_node) {
            var func_name = functionDef_node.funcName;
            offset_sum = 0;
            var func_symbol = new Func_Symbol(func_name);
            currentScope.insert(func_symbol);
            currentScope = new ScopedSymbolTable(func_name, currentScope.scope_level + 1, currentScope);
            for (var formalParam : functionDef_node.formal_param_nodes) {
                formalParam.accept(this);
            }
            functionDef_node.blockNode.accept(this);
            functionDef_node.offset = offset_sum;
            currentScope = currentScope.enclosing_scope;
            func_symbol.block_ast = functionDef_node.blockNode;
        } else {
            throw new RuntimeException("SemanticAnalyzer visit_FunctionDef_Node: node is not FunctionDef_Node");
        }
    }

    @Override
    void visit_FunctionCall_Node(ASTNode node) {
        if (node instanceof FunctionCal_Node) {
            UnitedLog.warn("SemanticAnalyzer visit_FunctionCall_Node: Doing nothing");
        } else {
            throw new RuntimeException("SemanticAnalyzer visit_FunctionCall_Node: node is not FunctionCall_Node");
        }
    }

    @Override
    void visit_Var_array_item_Node(ASTNode node) {
        if (node instanceof Var_array_item_Node var_array_item_node) {
            var arr_name = var_array_item_node.token.value;
            var arr_symbol = currentScope.lookup(arr_name);
            arr_symbol.ifPresentOrElse(sym -> var_array_item_node.symbol = sym, () -> {
                UnitedLog.err("SemanticAnalyzer visit_Var_array_item_Node: " + arr_name + " is not defined");
                System.exit(1);
            });
        } else {
            throw new RuntimeException("SemanticAnalyzer visit_Var_array_item_Node: node is not Var_array_item_Node");
        }
    }
}
