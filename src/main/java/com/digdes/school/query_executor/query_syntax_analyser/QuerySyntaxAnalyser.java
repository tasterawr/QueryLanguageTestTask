package com.digdes.school.query_executor.query_syntax_analyser;

import com.digdes.school.query_executor.query_lexeme_analyser.Lexeme;
import com.digdes.school.query_executor.query_lexeme_analyser.LexemeClass;
import com.digdes.school.query_executor.query_lexeme_analyser.LexemeType;
import com.digdes.school.query_executor.query_lexeme_analyser.QueryLexemeAnalyser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class QuerySyntaxAnalyser {
    private QueryLexemeAnalyser lexemeAnalyser = new QueryLexemeAnalyser();
    private List<Lexeme> lexemes;
    private int index;
    private String error;
    private List<Entry> entryList;

    public String analyseSyntax(String request){
        lexemes = lexemeAnalyser.analyseLexemes(request);
        index = 0;
        error = "";
        entryList = new ArrayList<>();

        analyse();
        return error;
    }

    private boolean analyse(){
        while (index < lexemes.size()){
            if ((!isSelectRequest()
                    && !isInsertRequest()
                    && !isUpdateRequest()
                    && !isDeleteRequest()) || !error.equals("")){
                return false;
            }
        }

        return true;
    }

    private boolean isSelectRequest(){
        if (index >= lexemes.size() || lexemes.get(index).getLexemeType() != LexemeType.SELECT){
            return false;
        }

        index++;
        if (!isCondition()) return false;

        writeCommand(Command.SEL);
        return true;
    }

    private boolean isUpdateRequest(){
        if (index >= lexemes.size() || lexemes.get(index).getLexemeType() != LexemeType.UPDATE){
            return false;
        }

        index++;
        if (index >= lexemes.size() || lexemes.get(index).getLexemeType() != LexemeType.VALUES){
            int position = index >= lexemes.size() ? getAfterIndex() : lexemes.get(index).getPosition();
            writeError("Оператор VALUES ожидался в позиции " + position + ".");
            return false;
        }

        index++;
        if (!isUpdateValueSequence()) return false;

        index++;
        if (!isCondition()) return false;

        writeCommand(Command.UPD);
        return true;
    }

    private boolean isDeleteRequest(){
        if (index >= lexemes.size() || lexemes.get(index).getLexemeType() != LexemeType.DELETE){
            return false;
        }

        index++;
        if (!isCondition()) return false;

        writeCommand(Command.DEL);
        return true;
    }

    private boolean isInsertRequest(){
        if (index >= lexemes.size() || lexemes.get(index).getLexemeType() != LexemeType.INSERT){
            return false;
        }

        index++;
        if (index >= lexemes.size() || lexemes.get(index).getLexemeType() != LexemeType.VALUES){
            int position = index >= lexemes.size() ? getAfterIndex() : lexemes.get(index).getPosition();
            writeError("Оператор VALUES ожидался в позиции " + position + ".");
            return false;
        }

        index++;
        if (!isUpdateValueSequence()) return false;

        index++;
        writeCommand(Command.INS);

        return true;
    }

    private boolean isUpdateValueSequence(){
        if (!isUpdateValueStatement()) return false;

        index++;
        if (index >= lexemes.size())
            return true;

        while (index < lexemes.size() && lexemes.get(index).getLexemeClass() == LexemeClass.SEPARATOR){
            index++;
            if (!isUpdateValueStatement()) return false;
            index++;
        }

        index--;

        return true;
    }

    private boolean isUpdateValueStatement(){
        if (!isOperand()) return false;

        index++;
        if (!isAssignment()) return false;

        index++;
        if (!isOperand()) return false;

        return true;
    }

    private boolean isAssignment(){
        if (index >= lexemes.size() || lexemes.get(index).getLexemeType() != LexemeType.COMPARISON
                || !lexemes.get(index).getValue().equals("=")){
            int position = index >= lexemes.size() ? getAfterIndex() : lexemes.get(index).getPosition();
            writeError("Оператор присваивания ожидался в позиции " + position + ".");
            return false;
        }

        return true;
    }

    private boolean isCondition(){
        if (index >= lexemes.size()){
            return true;
        }

        if(lexemes.get(index).getLexemeType() != LexemeType.WHERE){
            int position = index >= lexemes.size() ? getAfterIndex() : lexemes.get(index).getPosition();
            writeError("Оператор WHERE ожидался в позиции " + position + ".");
            return false;
        }

        index++;
        if (!isLogExpression()) return false;

        index++;
        if (index >= lexemes.size())
            return true;

        while (lexemes.get(index) != null && lexemes.get(index).getLexemeType() == LexemeType.OR){
            index++;
            if (!isLogExpression()) return false;
            writeCommand(Command.OR);
        }

        return true;
    }

    private boolean isLogExpression(){
        if (!isRelExpression()) return false;

        index++;
        if (index >= lexemes.size())
            return true;

        while (index < lexemes.size() && lexemes.get(index).getLexemeType() == LexemeType.AND){
            index++;
            if (!isRelExpression()) return false;
            writeCommand(Command.AND);
        }

        return true;
    }

    private boolean isRelExpression(){
        if (!isOperand()) return false;

        index++;
        if (index >= lexemes.size() || (lexemes.get(index).getLexemeType() != LexemeType.COMPARISON &&
                lexemes.get(index).getLexemeType() != LexemeType.LIKE &&
                lexemes.get(index).getLexemeType() != LexemeType.ILIKE)){
            writeError("Оператор сравнения ожидался в позиции " +lexemes.get(index).getPosition());
            return false;
        }

        if (lexemes.get(index).getLexemeType() == LexemeType.COMPARISON){
            Command cmd = null;
            String val = lexemes.get(index).getValue();
            switch (val) {
                case "<" -> cmd = Command.CMPL;
                case "<=" -> cmd = Command.CMPLE;
                case "!=" -> cmd = Command.CMPNE;
                case "=" -> cmd = Command.CMPE;
                case ">" -> cmd = Command.CMPG;
                case ">=" -> cmd = Command.CMPGE;
            }
            index++;
            if (!isOperand()) return false;
            writeCommand(cmd);
        }

        LexemeType lexemeType = lexemes.get(index).getLexemeType();
        if (lexemeType == LexemeType.LIKE || lexemeType == LexemeType.ILIKE){
            Command cmd = null;
            if (lexemeType == LexemeType.LIKE){
                cmd = Command.CMP_LIKE;
            } else {
                cmd = Command.CMP_ILIKE;
            }

            index++;
            if (!isOperand()) return false;

            if (lexemes.get(index).getLexemeType() != LexemeType.STRING_VALUE){
                writeError(String.format("Оператор %s может применяться только со строковым типом.", lexemeType));
                return false;
            }
            writeCommand(cmd);
        }
        return true;
    }

    private boolean isOperand(){
        if (index >= lexemes.size() || (lexemes.get(index).getLexemeClass() != LexemeClass.OPERAND)){
            int position = index >= lexemes.size() ? getAfterIndex() : lexemes.get(index).getPosition();
            writeError("Значение или имя столбца ожидалось в позиции " + position + ".");
            return false;
        }

        writeConst(index);
        return true;
    }

    private int getAfterIndex(){
        int position = -1;
        if (index >= lexemes.size())
            position = lexemes.get(index-1).getPosition() + lexemes.get(index-1).getValue().length() + 1;
        else
            position = lexemes.get(index).getPosition();

        return position;
    }

    private void writeError(String message){
        error = message;
    }

    private int writeCommand(Command cmd){
        Entry entry = new Entry();
        entry.setType(EntryType.CMD);
        entry.setCmd(cmd);
        entryList.add(entry);
        return entryList.size()-1;
    }

    private void writeConst(int index){
        Entry con = new Entry();
        con.setType(EntryType.CONST);
        String value = lexemes.get(index).getValue();
        LexemeType lexemeType = lexemes.get(index).getLexemeType();

        if (lexemeType.equals(LexemeType.LONG_VALUE)){
            con.setValue(Long.parseLong(value));
        } else if (lexemeType.equals(LexemeType.DOUBLE_VALUE)){
            con.setValue(Double.parseDouble(value));
        } else if (lexemeType.equals(LexemeType.BOOLEAN_VALUE)){
            con.setValue(Boolean.parseBoolean(value));
        } else if (lexemeType.equals(LexemeType.NULL_VALUE)){
            con.setValue(null);
        } else {
            con.setValue(value);
        }

        con.setLexemeType(lexemeType);
        entryList.add(con);
    }

    private void setCmdPtr(int index, int ptr){
        entryList.get(index).setCmdPtr(ptr);
    }

    public List<Entry> getEntryList(){
        return entryList;
    }
}
