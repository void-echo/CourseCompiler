package com.example.coursecompiler;

import java.util.ArrayList;
import java.util.List;

public abstract class Symbol {
    public Symbol(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public Symbol(String name) {
        this(name, null);
    }

    public String name, type;
    public int offset;
}

class Func_Symbol extends Symbol {
    List<String> formal_params;
    ASTNode block_ast;

    public Func_Symbol(String name, String type, List<String> formal_params) {
        super(name, type);
        this.formal_params = formal_params;
    }

    public Func_Symbol(String name, String type) {
        this(name, type, new ArrayList<>());
    }

    public Func_Symbol(String name) {
        super(name);
        this.formal_params = new ArrayList<>();
    }
}

class Var_Symbol extends Symbol {
    Symbol symbol;

    public Var_Symbol(String name, String type, int offset, Symbol symbol) {
        super(name, type);
        this.offset = offset;
        this.symbol = symbol;
    }

    public Var_Symbol(String name, String type, int offset) {
        super(name, type);
        this.offset = offset;
    }
}


class Param_Symbol extends Symbol {

    public Param_Symbol(String name, String type, int offset) {
        super(name, type);
        this.offset = offset;
    }
}
