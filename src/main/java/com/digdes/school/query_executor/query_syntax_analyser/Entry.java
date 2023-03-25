package com.digdes.school.query_executor.query_syntax_analyser;

import com.digdes.school.query_executor.query_lexeme_analyser.LexemeType;

public class Entry {
    private EntryType type;
    private int index;
    private Command cmd;
    private Object value;
    private int cmdPtr;
    private int curValue;
    private LexemeType lexemeType;

    public EntryType getType() {
        return type;
    }

    public void setType(EntryType type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Command getCmd() {
        return cmd;
    }

    public void setCmd(Command cmd) {
        this.cmd = cmd;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getCmdPtr() {
        return cmdPtr;
    }

    public void setCmdPtr(int cmdPtr) {
        this.cmdPtr = cmdPtr;
    }

    public int getCurValue() {
        return curValue;
    }

    public void setCurValue(int curValue) {
        this.curValue = curValue;
    }

    public LexemeType getLexemeType() {
        return lexemeType;
    }

    public void setLexemeType(LexemeType lexemeType) {
        this.lexemeType = lexemeType;
    }

    @Override
    public String  toString() {
        if (cmd != null)
            return cmd.toString();
        else if (value != null)
            return value.toString();
        else return String.valueOf(cmdPtr);
    }
}