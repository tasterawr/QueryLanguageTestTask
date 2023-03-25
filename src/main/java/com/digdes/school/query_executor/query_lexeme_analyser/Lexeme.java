package com.digdes.school.query_executor.query_lexeme_analyser;

public class Lexeme {
    private LexemeClass lexemeClass;
    private LexemeType lexemeType;
    private String value;
    private int position;

    public Lexeme(LexemeClass lexemeClass, LexemeType lexemeType, String value, int position) {
        this.lexemeClass = lexemeClass;
        this.lexemeType = lexemeType;
        this.value = value;
        this.position = position;
    }

    public LexemeClass getLexemeClass() {
        return lexemeClass;
    }

    public void setLexemeClass(LexemeClass lexemeClass) {
        this.lexemeClass = lexemeClass;
    }

    public LexemeType getLexemeType() {
        return lexemeType;
    }

    public void setLexemeType(LexemeType lexemeType) {
        this.lexemeType = lexemeType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "Lexeme{" +
                "lexemeClass=" + lexemeClass +
                ", lexemeType=" + lexemeType +
                ", value='" + value + '\'' +
                ", position=" + position +
                '}';
    }
}
