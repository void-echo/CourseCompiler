package com.example.coursecompiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ScopedSymbolTable {
    Map<String, Symbol> symbols;
    String scope_name;
    int scope_level;
    ScopedSymbolTable enclosing_scope;

    public ScopedSymbolTable(String scope_name, int scope_level) {
        this(scope_name, scope_level, null);
    }

    public ScopedSymbolTable(String scope_name, int scope_level, ScopedSymbolTable enclosing_scope) {
        symbols = new HashMap<>();
        this.scope_name = scope_name;
        this.scope_level = scope_level;
        this.enclosing_scope = enclosing_scope;
    }

    void insert(Symbol symbol) {
        symbols.put(symbol.name, symbol);
    }

    Optional<Symbol> lookup(String name) {
        return lookup(name, false);
    }

    Optional<Symbol> lookup(String name, boolean current_scope_only) {
        Symbol symbol = symbols.get(name);
        if (symbol != null) return Optional.of(symbol);
        if (current_scope_only) return Optional.empty();
        if (enclosing_scope != null) return enclosing_scope.lookup(name);
        UnitedLog.err("Symbol not found: %s".formatted(name));
        return Optional.empty();
    }

    public void show() {
        System.out.printf("Scope name: %s%n", scope_name);
        for (Symbol symbol : symbols.values()) {
            System.out.println(symbol);
        }
    }
}
