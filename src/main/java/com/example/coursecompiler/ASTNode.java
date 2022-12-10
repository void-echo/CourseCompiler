package com.example.coursecompiler;

import java.util.Arrays;
import java.util.List;

public abstract class ASTNode {
    String value;
    Token token;
    Symbol symbol;

    abstract void accept(Visitor visitor);
}

class unaryOpNode extends ASTNode {
    Token op;
    ASTNode right;

    public unaryOpNode(Token op, ASTNode right) {
        this.op = op;
        this.token = op;
        this.right = right;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visitUnaryOpNode(this);
    }
}


class ifNode extends ASTNode {
    ASTNode condition, then_stat, else_stat;

    public ifNode(ASTNode condition, ASTNode then_stat, ASTNode else_stat) {
        this.condition = condition;
        this.then_stat = then_stat;
        this.else_stat = else_stat;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visitIfNode(this);
    }
}

class whileNode extends ASTNode {
    ASTNode condition, statement;

    public whileNode(ASTNode condition, ASTNode statement) {
        this.condition = condition;
        this.statement = statement;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visitWhileNode(this);
    }
}

class returnNode extends ASTNode {
    ASTNode right;
    String funcName;

    public returnNode(Token token, ASTNode right, String funcName) {
        this.token = token;
        this.right = right;
        this.funcName = funcName;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visitReturnNode(this);
    }
}


class blockNode extends ASTNode {
    Token lt, rt;
    List<ASTNode> state_nodes;

    public blockNode(Token lt, Token rt, List<ASTNode> state_nodes) {
        this.lt = lt;
        this.rt = rt;
        this.state_nodes = state_nodes;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visitBlockNode(this);
    }
}

class binaryOpNode extends ASTNode {
    Token op;
    ASTNode left, right;

    public binaryOpNode(ASTNode left, Token op, ASTNode right) {
        this.left = left;
        this.token = this.op = op;
        this.right = right;
   }

    @Override
    void accept(Visitor visitor) {
        visitor.visitBinaryOpNode(this);
    }
}


class assignNode extends ASTNode {
    Token op;
    ASTNode left, right;

    public assignNode(ASTNode left, Token op, ASTNode right) {
        this.left = left;
        this.token = this.op = op;
        this.right = right;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visitAssignNode(this);
    }
}

class functionCalNode extends ASTNode {
    String funcName;
    List<ASTNode> actual_param_nodes;

    public functionCalNode(String funcName, List<ASTNode> actual_param_nodes, Token token) {
        this.funcName = funcName;
        this.actual_param_nodes = actual_param_nodes;
        this.token = token;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visitFunctionCallNode(this);
    }
}

class numNode extends ASTNode {

    public numNode(Token token) {
        this.token = token;
        this.value = token.value;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visitNumNode(this);
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

class varNode extends ASTNode {
    String name;
    _array array;


    @Override
    void accept(Visitor visitor) {
        visitor.visitVarNode(this);
    }

    public varNode(Token token) {
        this.token = token;
        this.name = token.value;
    }

    public varNode(Token token, _array array) {
        this.token = token;
        this.name = token.value;
        this.array = array; // stores real array instead of 'Yes' or 'No'
    }
}

class varArrayItemNode extends ASTNode {
    ASTNode index;
    String array;

    public varArrayItemNode(Token token, ASTNode index) {
        this.token = token;
        this.index = index;
        this.array = "Yes";
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visitVarArrayItemNode(this);
    }
}


class typeNode extends ASTNode {

    public typeNode(Token token) {
        this.token = token;
        this.value = token.value;
    }

    @Override
    void accept(Visitor visitor) {
        UnitedLog.warn("Visitor not implemented for BasicType_Node");
    }
}

class varDeclNode extends ASTNode {
    ASTNode basicTypeNode, varNode;

    public varDeclNode(ASTNode basicTypeNode, ASTNode varNode) {
        this.basicTypeNode = basicTypeNode;
        this.varNode = varNode;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visitVarDeclNode(this);
    }
}


class formalParamNode extends ASTNode {
    ASTNode basicTypeNode, paramNode;
    Symbol paramSymbol;

    public formalParamNode(ASTNode basicTypeNode, ASTNode paramNode) {
        this.basicTypeNode = basicTypeNode;
        this.paramNode = paramNode;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visitFormalParamNode(this);
    }
}

class functionDefNode extends ASTNode {
    ASTNode basicTypeNode;
    String funcName;
    List<formalParamNode> formal_param_nodes;
    ASTNode blockNode;
    int offset;

    public functionDefNode(ASTNode basicTypeNode, String funcName, List<formalParamNode> formal_param_nodes, ASTNode blockNode) {
        this.basicTypeNode = basicTypeNode;
        this.funcName = funcName;
        this.formal_param_nodes = formal_param_nodes;
        this.blockNode = blockNode;
        offset = 0;
    }


    @Override
    void accept(Visitor visitor) {
        visitor.visitFunctionDefNode(this);
    }
}
