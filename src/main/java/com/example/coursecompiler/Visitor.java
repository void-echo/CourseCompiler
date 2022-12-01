package com.example.coursecompiler;

public abstract class Visitor {
    abstract void visit_UnaryOp_Node(ASTNode node);

    abstract void visit_Return_Node(ASTNode node);

    abstract void visit_BinaryOp_Node(ASTNode node);

    abstract void visit_Assign_Node(ASTNode node);

    abstract void visit_If_Node(ASTNode node);

    abstract void visit_While_Node(ASTNode node);

    abstract void visit_Block_Node(ASTNode node);

    abstract void visit_Num_Node(ASTNode node);

    abstract void visit_Var_Node(ASTNode node);

    abstract void visit_VarDecl_Node(ASTNode node);

    abstract void visit_FormalParam_Node(ASTNode node);

    abstract void visit_FunctionDef_Node(ASTNode node);

    abstract void visit_FunctionCall_Node(ASTNode node);

    abstract void visit_Var_array_item_Node(ASTNode node);

}
