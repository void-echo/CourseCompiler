package com.example.coursecompiler;

public abstract class Visitor {
    abstract void visitUnaryOpNode(ASTNode node);

    abstract void visitReturnNode(ASTNode node);

    abstract void visitBinaryOpNode(ASTNode node);

    abstract void visitAssignNode(ASTNode node);

    abstract void visitIfNode(ASTNode node);

    abstract void visitWhileNode(ASTNode node);

    abstract void visitBlockNode(ASTNode node);

    abstract void visitNumNode(ASTNode node);

    abstract void visitVarNode(ASTNode node);

    abstract void visitVarDeclNode(ASTNode node);

    abstract void visitFormalParamNode(ASTNode node);

    abstract void visitFunctionDefNode(ASTNode node);

    abstract void visitFunctionCallNode(ASTNode node);

    abstract void visitVarArrayItemNode(ASTNode node);

}
