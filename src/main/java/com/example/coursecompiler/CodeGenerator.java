package com.example.coursecompiler;

import java.util.List;

public class CodeGenerator extends Visitor {
    static int _count = 0;
    static final String[] parameter_registers = {"rdi", "rsi", "rdx", "rcx", "r8", "r9"};


    public void codegen(List<ASTNode> node) {
        node.forEach(n -> {
            // accept if not null
            if (n != null) {
                n.accept(this);
            }
        });
    }

    public static int align_to(int n, int alignment) {
        return ((n + alignment - 1) / alignment) * alignment;
    }

    public static Object getAttr(Object obj, String attrName) {
        // DO NOT USE THIS FUNCTION WHEN DYNAMIC CLASS TYPE IS USED!
        return ClassUtil.getFieldValue(obj, attrName);
    }

    public void generate_array_item_address(ASTNode node) {
        if (node instanceof varArrayItemNode var_array_item_node) {
            if (var_array_item_node.symbol instanceof Var_Symbol var_symbol) {
                var arr_offset = var_symbol.offset;
//                System.out.println("************************************" + var_array_item_node.index.token.type);
                if (var_array_item_node.index.token.type.equals("INT_CONST")) {
                    int i = Integer.parseInt(var_array_item_node.index.value);
                    var arr_item_offset = (i - 1) * 8;
                    System.out.println("    mov $" + arr_item_offset + ", %rax");
                    System.out.println("    push %rax");
                    System.out.println("    lea " + arr_offset + "(%rbp), %rax");
                } else {
                    var_array_item_node.index.accept(this);
                    System.out.println("    sub $1, %rax");
                    System.out.println("    imul $8, %rax");
                    System.out.println("    push %rax");
                    System.out.println("    lea " + var_symbol.offset + "(%rbp), %rax");
                }
                System.out.println("    pop %rdi");
                System.out.println("    add %rdi, %rax");
            }
        } else {
            throw new RuntimeException("generate_array_item_address: node is not Var_array_item_Node");
        }
    }

    @Override
    void visitUnaryOpNode(ASTNode node) {
        if (node instanceof unaryOpNode unaryOp_node) {
            var tk = unaryOp_node.op;
            if (tk.type.equals("-")) {
                unaryOp_node.right.accept(this);
                System.out.println("    neg %rax");
            } else if (tk.type.equals("!")) {
                unaryOp_node.right.accept(this);
                System.out.println("    not %rax");
            } else {
                UnitedLog.err("CodeGenerator visit_UnaryOp_Node: unknown operator: " + tk.type + ". Do nothing.");
                System.exit(1);
            }
        } else {
            throw new RuntimeException("CodeGenerator visit_UnaryOp_Node: node is not UnaryOp_Node. Expected UnaryOp_Node, got " + node.getClass().getName());
        }
    }

    @Override
    void visitReturnNode(ASTNode node) {
        if (node instanceof returnNode return_node) {
            return_node.right.accept(this);
            var tk = return_node.token;
            if (tk.type.equals("return")) {
                System.out.println("    jmp ." + return_node.funcName + ".return");
            } else {
                UnitedLog.err("CodeGenerator visit_Return_Node: unknown token type: " + tk.type + ". Do nothing.");
            }
        } else {
            throw new RuntimeException("CodeGenerator visit_Return_Node: node is not Return_Node. Expected Return_Node, got " + node.getClass().getName());
        }
    }

    @Override
    void visitBinaryOpNode(ASTNode node) {
        if (node instanceof binaryOpNode binaryOp_node) {
            binaryOp_node.right.accept(this);
            System.out.println("    push %rax");
            binaryOp_node.left.accept(this);
            System.out.println("    pop %rdi");
            var tk_tp = binaryOp_node.op.type;
            switch (tk_tp) {
                case "+" -> System.out.println("    add %rdi, %rax");
                case "-" -> System.out.println("    sub %rdi, %rax");
                case "*" -> System.out.println("    imul %rdi, %rax");
                case "/" -> {
                    System.out.println("    cqo");
                    System.out.println("    idiv %rdi");
                }
                case "==" -> {
                    System.out.println("    cmp %rdi, %rax");
                    System.out.println("    sete %al");
                    System.out.println("    movzb %al, %rax");
                }
                case "!=" -> {
                    System.out.println("    cmp %rdi, %rax");
                    System.out.println("    setne %al");
                    System.out.println("    movzb %al, %rax");
                }
                case ">" -> {
                    System.out.println("    cmp %rdi, %rax");
                    System.out.println("    setg %al");
                    System.out.println("    movzb %al, %rax");
                }
                case "<" -> {
                    System.out.println("    cmp %rdi, %rax");
                    System.out.println("    setl %al");
                    System.out.println("    movzb %al, %rax");
                }
                case ">=" -> {
                    System.out.println("    cmp %rdi, %rax");
                    System.out.println("    setge %al");
                    System.out.println("    movzb %al, %rax");
                }
                case "<=" -> {
                    System.out.println("    cmp %rdi, %rax");
                    System.out.println("    setle %al");
                    System.out.println("    movzb %al, %rax");
                }
                case "&&" -> System.out.println("    and %rdi, %rax");
                case "||" -> System.out.println("    or %rdi, %rax");
                default -> {
//                    UnitedLog.warn("CodeGenerator visit_BinaryOp_Node: unknown operator: " + tk_tp + ". Do nothing.");
                }
            }
        } else {
            throw new RuntimeException("CodeGenerator visit_BinaryOp_Node: node is not BinaryOp_Node. Expected BinaryOp_Node, got " + node.getClass().getName());
        }
    }

    @Override
    void visitAssignNode(ASTNode node) {
        if (node instanceof assignNode assign_node) {
            if (assign_node.left.token.type.equals("ID")) {
                var var_offset = assign_node.left.symbol.offset;
                System.out.println("    lea " + var_offset + "(%rbp), %rax");
                System.out.println("    push %rax");
                if (assign_node.left instanceof varArrayItemNode varArrayItem_node) {
                    if (varArrayItem_node.array != null) {
                        generate_array_item_address(varArrayItem_node);
                        System.out.println("    push %rax");
                    }
                }
                assign_node.right.accept(this);
                System.out.println("    pop %rdi");
                System.out.println("    mov %rax, (%rdi)");
            }
        } else {
            throw new RuntimeException("CodeGenerator visit_Assign_Node: node is not Assign_Node");
        }
    }

    @Override
    void visitIfNode(ASTNode node) {
        if (node instanceof ifNode if_node) {
            _count++;
            var localLabel = _count;
            if_node.condition.accept(this);
            System.out.println("    cmp $0, %rax");
            System.out.println("    je  .L.else." + localLabel);
            if (if_node.then_stat != null) {
                if_node.then_stat.accept(this);
            }
            System.out.println("    jmp .L.end." + localLabel);
            System.out.println(".L.else." + localLabel + ":");
            if (if_node.else_stat != null) {
                if_node.else_stat.accept(this);
            }
            System.out.println(".L.end." + localLabel + ":");
        } else {
            throw new RuntimeException("CodeGenerator visit_If_Node: node is not If_Node");
        }
    }

    @Override
    void visitWhileNode(ASTNode node) {
        if (node instanceof whileNode while_node) {
            _count++;
            var localLabel = _count;
            System.out.println(".L.condition." + localLabel + ":");
            while_node.condition.accept(this);
            System.out.println("    cmp $0, %rax");
            System.out.println("    je  .L.end." + localLabel);
            if (while_node.statement != null) {
                while_node.statement.accept(this);
            }
            System.out.println("    jmp .L.condition." + localLabel);
            System.out.println(".L.end." + localLabel + ":");
        } else {
            throw new RuntimeException("CodeGenerator visit_While_Node: node is not While_Node");
        }
    }

    @Override
    void visitBlockNode(ASTNode node) {
        if (node instanceof blockNode block_node) {
            for (var stat : block_node.state_nodes) {
                stat.accept(this);
            }
        } else {
            throw new RuntimeException("CodeGenerator visit_Block_Node: node is not Block_Node");
        }
    }

    @Override
    void visitNumNode(ASTNode node) {
        if (node instanceof numNode num_node) {
            if (num_node.value.equals("true")) {
                System.out.println("    mov $1, %rax"); // 1 is true
            } else if (num_node.value.equals("false")) {
                System.out.println("    mov $0, %rax"); // 0 is false
            } else {
                System.out.println("    mov $" + num_node.value + ", %rax");
            }
        } else {
            throw new RuntimeException("CodeGenerator visit_Num_Node: node is not Num_Node");
        }
    }

    @Override
    void visitVarNode(ASTNode node) {
        if (node instanceof varNode var_node) {
            var var_offset = var_node.symbol.offset;
            System.out.println("    lea " + var_offset + "(%rbp), %rax");
            System.out.println("    mov (%rax), %rax");
        } else {
            throw new RuntimeException("CodeGenerator visit_Var_Node: node is not Var_Node");
        }
    }

    @Override
    void visitVarDeclNode(ASTNode node) {
        if (node instanceof varDeclNode varDecl_node) {
            if (varDecl_node.varNode != null) {
                if (varDecl_node.varNode instanceof varNode var_node) {
                    if (var_node.array != null) {
                        var arr_offset = var_node.symbol.offset;
                        var arr_size = Integer.parseInt(var_node.array.size);
                        int __i__ = 0;
                        while (__i__ < arr_size) {
                            int arr_item_offset = __i__ * 8;
                            System.out.println("    mov $" + arr_item_offset + ", %rax");
                            System.out.println("    push %rax");
                            System.out.println("    lea " + arr_offset + "(%rbp), %rax");
                            System.out.println("    pop %rdi");
                            System.out.println("    add %rdi, %rax");
                            var _val_i = var_node.array.items[__i__];
                            System.out.println("    mov $" + _val_i + ", %rdi");
                            System.out.println("    mov %rdi, (%rax)");
                            __i__++;
                        }
                    }
                } else throw new RuntimeException("CodeGenerator visit_VarDecl_Node: varNode is not Var_Node");
            } else throw new RuntimeException("CodeGenerator visit_VarDecl_Node: varNode is null");
        } else throw new RuntimeException("CodeGenerator visit_VarDecl_Node: node is not VarDecl_Node");
    }

    @Override
    void visitFormalParamNode(ASTNode node) {
//        UnitedLog.warn("CodeGenerator visit_FormalParam_Node: not implemented");
    }

    @Override
    void visitFunctionDefNode(ASTNode node) {
        SemanticAnalyzer.offset_sum = 0;
        if (node instanceof functionDefNode funcDef_node) {
            System.out.println("    .text");
            System.out.println("    .global " + funcDef_node.funcName);
            System.out.println(funcDef_node.funcName + ":");
            // 开场白
            System.out.println("    push %rbp");
            System.out.println("    mov %rsp, %rbp");
            int stack_size = align_to(funcDef_node.offset, 16);
            System.out.println("    sub $" + stack_size + ", %rsp");

            int __i__ = 0;
            for (var para : funcDef_node.formal_param_nodes) {
                var param_offset = para.paramSymbol.offset;
                System.out.println("    mov %" + parameter_registers[__i__] + ", " + param_offset + "(%rbp)");
                __i__++;
            }
            funcDef_node.blockNode.accept(this);
            System.out.println("." + funcDef_node.funcName + ".return:");
            System.out.println("    mov %rbp, %rsp");
            System.out.println("    pop %rbp");
            System.out.println("    ret");
        } else {
            throw new RuntimeException("CodeGenerator visit_FunctionDef_Node: node is not FunctionDef_Node");
        }
    }

    @Override
    void visitFunctionCallNode(ASTNode node) {
        int num_args = 0;
        if (node instanceof functionCalNode funcCall_node) {
            for (var arg : funcCall_node.actual_param_nodes) {
                // actual_param_nodes must not be null. This had been verified.
                arg.accept(this);
                System.out.println("    push %rax");
                num_args++;
            }
            for (int i = num_args - 1; i >= 0; i--) {
                System.out.println("    pop %" + parameter_registers[i]);
            }
            System.out.println("    mov $0, %rax");
            System.out.println("    call " + funcCall_node.funcName);
        } else {
            throw new RuntimeException("CodeGenerator visit_FunctionCall_Node: node is not FunctionCall_Node");
        }
    }

    @Override
    void visitVarArrayItemNode(ASTNode node) {
        if (node instanceof varArrayItemNode varArrayItem_node) {
            if (varArrayItem_node.array != null && !"".equals(varArrayItem_node.array)) {
                generate_array_item_address(varArrayItem_node);
                System.out.println("    mov (%rax), %rax");
            }
        } else {
            throw new RuntimeException("CodeGenerator visit_Var_array_item_Node: node is not Var_array_item_Node");
        }
    }
}
