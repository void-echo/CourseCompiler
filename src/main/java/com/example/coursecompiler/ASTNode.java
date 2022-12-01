package com.example.coursecompiler;

import java.util.Arrays;
import java.util.List;

public abstract class ASTNode {
    String value;
    Token token;
    Symbol symbol;
    abstract void accept(Visitor visitor);
}

class UnaryOp_Node extends ASTNode {
    Token op;
    ASTNode right;

    public UnaryOp_Node(Token op, ASTNode right) {
        this.op = op;
        this.token = op;
        this.right = right;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visit_UnaryOp_Node(this);
    }
}


class If_Node extends ASTNode {
    ASTNode condition, then_stat, else_stat;

    public If_Node(ASTNode condition, ASTNode then_stat, ASTNode else_stat) {
        this.condition = condition;
        this.then_stat = then_stat;
        this.else_stat = else_stat;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visit_If_Node(this);
    }
}

class While_Node extends ASTNode {
    ASTNode condition, statement;

    public While_Node(ASTNode condition, ASTNode statement) {
        this.condition = condition;
        this.statement = statement;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visit_While_Node(this);
    }
}

class Return_Node extends ASTNode {
    ASTNode right;
    String funcName;

    public Return_Node(Token token, ASTNode right, String funcName) {
        this.token = token;
        this.right = right;
        this.funcName = funcName;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visit_Return_Node(this);
    }
}


class Block_Node extends ASTNode {
    Token lt, rt;
    List<ASTNode> state_nodes;

    public Block_Node(Token lt, Token rt, List<ASTNode> state_nodes) {
        this.lt = lt;
        this.rt = rt;
        this.state_nodes = state_nodes;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visit_Block_Node(this);
    }
}

class BinaryOp_Node extends ASTNode {
    Token op;
    ASTNode left, right;

    public BinaryOp_Node(ASTNode left, Token op, ASTNode right) {
        this.left = left;
        this.token = this.op = op;
        this.right = right;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visit_BinaryOp_Node(this);
    }
}


class Assign_Node extends ASTNode {
    Token op;
    ASTNode left, right;

    public Assign_Node(ASTNode left, Token op, ASTNode right) {
        this.left = left;
        this.token = this.op = op;
        this.right = right;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visit_Assign_Node(this);
    }
}

class FunctionCal_Node extends ASTNode {
    String funcName;
    List<ASTNode> actual_param_nodes;

    public FunctionCal_Node(String funcName, List<ASTNode> actual_param_nodes, Token token) {
        this.funcName = funcName;
        this.actual_param_nodes = actual_param_nodes;
        this.token = token;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visit_FunctionCall_Node(this);
    }
}

class Num_Node extends ASTNode {

    public Num_Node(Token token) {
        this.token = token;
        this.value = token.value;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visit_Num_Node(this);
    }
}

class _array {
    String size;
    int[] items;

    public _array(String size, int[] items) {
        this.size = size;
        this.items = items;
    }

    public _array(String size, List<Integer> items) {
        this.size = size;
        this.items = items.stream().mapToInt(i -> i).toArray();
        // this code style seems complicated. I don't know how to do it in a better way.
    }

    @Override
    public String toString() {
        return "_array{" +
                "size='" + size + '\'' +
                ", items=" + Arrays.toString(items) +
                '}';
    }
}

class Var_Node extends ASTNode {
    String name;
    _array array;


    @Override
    void accept(Visitor visitor) {
        visitor.visit_Var_Node(this);
    }

    public Var_Node(Token token) {
        this.token = token;
        this.name = token.value;
    }

    public Var_Node(Token token, _array array) {
        this.token = token;
        this.name = token.value;
        this.array = array; // stores real array instead of 'Yes' or 'No'
    }
}

class Var_array_item_Node extends ASTNode {
    ASTNode index;
    String array;

    public Var_array_item_Node(Token token, ASTNode index) {
        this.token = token;
        this.index = index;
        this.array = "Yes";
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visit_Var_array_item_Node(this);
    }
}


class BasicType_Node extends ASTNode {

    public BasicType_Node(Token token) {
        this.token = token;
        this.value = token.value;
    }

    @Override
    void accept(Visitor visitor) {
        UnitedLog.warn("Visitor not implemented for BasicType_Node");
    }
}

class VarDecl_Node extends ASTNode {
    ASTNode basicTypeNode, varNode;

    public VarDecl_Node(ASTNode basicTypeNode, ASTNode varNode) {
        this.basicTypeNode = basicTypeNode;
        this.varNode = varNode;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visit_VarDecl_Node(this);
    }
}


class FormalParam_Node extends ASTNode {
    ASTNode basicTypeNode, paramNode;
    Symbol paramSymbol;

    public FormalParam_Node(ASTNode basicTypeNode, ASTNode paramNode) {
        this.basicTypeNode = basicTypeNode;
        this.paramNode = paramNode;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visit_FormalParam_Node(this);
    }
}

class FunctionDef_Node extends ASTNode {
    ASTNode basicTypeNode;
    String funcName;
    List<FormalParam_Node> formal_param_nodes;
    ASTNode blockNode;
    int offset;

    public FunctionDef_Node(ASTNode basicTypeNode, String funcName, List<FormalParam_Node> formal_param_nodes, ASTNode blockNode) {
        this.basicTypeNode = basicTypeNode;
        this.funcName = funcName;
        this.formal_param_nodes = formal_param_nodes;
        this.blockNode = blockNode;
        offset = 0;
    }


    @Override
    void accept(Visitor visitor) {
        visitor.visit_FunctionDef_Node(this);
    }
}
