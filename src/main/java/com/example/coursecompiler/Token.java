package com.example.coursecompiler;

public class Token {
    public String type;
    public String value;
    public int lineNum, colNum, width;

    @Override
    public String toString() {
        return ((value == null) || value.equals(type)) ? "%s%s%s ".formatted(UnitedLog.GREEN, type, UnitedLog.RESET) : "%s%s%s:%s%s%s ".formatted(UnitedLog.GREEN, type, UnitedLog.RESET, UnitedLog.YELLOW, value, UnitedLog.RESET);
    }

    public Token(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public Token(String type, String value, int lineNum, int colNum, int width) {
        this.type = type;
        this.value = value;
        this.lineNum = lineNum;
        this.colNum = colNum;
        this.width = width;
    }

    public Token(String type, String value, int lineNum, int colNum) {
        this.type = type;
        this.value = value;
        this.lineNum = lineNum;
        this.colNum = colNum;
    }

    public Token(String type) {
        this(type, type);
    }
}
