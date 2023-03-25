package com.digdes.school.query_executor.query_lexeme_analyser;

import com.digdes.school.query_executor.exception.QueryExecutorInstantiationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class QueryLexemeAnalyser {
    private final String KEYWORDS_PATH =  "src/main/resources/keywords.txt";
    private final String TRANS_TABLE_PATH = "src/main/resources/trans_table.txt";

    private String inputString;
    private int numOfStates;
    private int [][] transFunc;
    private int startState;
    private int [] endStates;
    private List<String> keywords = new ArrayList<>();
    private Map<Integer, String> stateMap = new HashMap<>();
    private List<Lexeme> lexemes = new ArrayList<>();
    private boolean checkCase;

    {
        try {
            loadKeywords();
            loadStateMap();
            prepareData();
        }
        catch (IOException e){
            throw new QueryExecutorInstantiationException("Не удалось инициализировать обработчик запросов.", e);
        }
    }

    private void loadKeywords() throws IOException {
        Path path = Paths.get(KEYWORDS_PATH);
        BufferedReader reader = Files.newBufferedReader(path);

        while (reader.ready()){
            String keyword = reader.readLine();
            keywords.add(keyword);
        }
    }

    private void loadStateMap(){
        stateMap.put(0, "Start");
        stateMap.put(1, "OperatorOrBoolOrNull");
        stateMap.put(3, "Long");
        stateMap.put(4, "Double");
        stateMap.put(5, "Not");
        stateMap.put(6, "LessMoreComparison");
        stateMap.put(7, "Comparison");
        stateMap.put(8, "Comma");
        stateMap.put(9, "String");
        stateMap.put(10, "Final");
    }

    private void resetData(){
        lexemes.clear();
    }

    public List<Lexeme> analyseLexemes(String sourceCode) {
        resetData();
        startAutomate(sourceCode + " ");
        return lexemes;
    }

    private void startAutomate(String input){
        int previousState = startState;
        String [] inputArray = input.split("");
        StringBuilder lexeme = new StringBuilder();
        int lexemeIndex = 0;
        int movingIndex = 0;

        for (String symbol : inputArray){
            movingIndex++;
            int index = getTableIndex(symbol);

            int nextState = transFunc[previousState][index];
            if (nextState == 0 && (previousState == 0 || previousState == 10)) {
                lexemeIndex++;
                continue;
            }

            if (nextState == 10){
                getLexemeInfo(previousState, lexeme.toString(), lexemeIndex);
                lexemeIndex = movingIndex;
                lexeme.setLength(0);
                if (index != 9)
                    lexeme.append(symbol);

                if (transFunc[nextState][index] != 0){
                    previousState = transFunc[nextState][index];
                } else{
                    previousState = nextState;
                }

            } else{
                previousState = nextState;
                lexeme.append(symbol);
            }
        }

        System.out.println();
//        printLexemes(); // для отладки
    }

    private int getTableIndex(String symbol){
        if (Character.isDigit(symbol.charAt(0)))
            return 1;

        if (symbol.charAt(0) >= 65 && symbol.charAt(0) <= 122
                || symbol.charAt(0) >= 1040 && symbol.charAt(0) <= 1103)
            return 0;

        return switch (symbol) {
            case "." -> 2;
            case "<" -> 3;
            case ">" -> 4;
            case "=" -> 5;
            case "!" -> 6;
            case "," -> 7;
            case "'" -> 8;
            default -> 9;
        };
    }

    private void getLexemeInfo(int previousState, String lexeme, int position){
        String state = stateMap.get(previousState);
        LexemeType lexemeType = null;
        LexemeClass lexemeClass = null;
        switch (state) {
            case "OperatorOrBoolOrNull":
                if (lexeme.toLowerCase(Locale.ROOT).equals("true") ||
                        lexeme.toLowerCase(Locale.ROOT).equals("false") ){
                    lexemeClass = LexemeClass.OPERAND;
                    lexemeType = LexemeType.BOOLEAN_VALUE;
                }

                if (lexeme.toLowerCase(Locale.ROOT).equals("null")){
                    lexemeClass = LexemeClass.OPERAND;
                    lexemeType = LexemeType.NULL_VALUE;
                }
                if (keywords.contains(lexeme.toLowerCase(Locale.ROOT))) {
                    lexemeClass = LexemeClass.OPERATOR;
                    for (LexemeType lex : LexemeType.values()) {
                        if (lex.toString().equalsIgnoreCase(lexeme)) {
                            lexemeType = lex;
                            break;
                        }
                    }
                }
                break;
            case "String":
                lexemeClass = LexemeClass.OPERAND;
                lexemeType = LexemeType.STRING_VALUE;
                break;
            case "Long":
                lexemeClass = LexemeClass.OPERAND;
                lexemeType = LexemeType.LONG_VALUE;
                break;
            case "Double":
                lexemeClass = LexemeClass.OPERAND;
                lexemeType = LexemeType.DOUBLE_VALUE;
                break;
            case "LessMoreComparison", "Comparison":
                lexemeClass = LexemeClass.OPERATOR;
                lexemeType = LexemeType.COMPARISON;
                break;
            case "Comma":
                lexemeClass = LexemeClass.SEPARATOR;
                break;
            default:
                return;
        }

        Lexeme lex = new Lexeme(lexemeClass, lexemeType, lexeme, position);
        lexemes.add(lex);
    }

    private void printLexemes(){
        System.out.println("\nLONG константы: ");
        for (Lexeme lex : lexemes.stream()
                .filter(x -> x.getLexemeType() == LexemeType.LONG_VALUE)
                .collect(Collectors.toList())){

            System.out.println(lex);
        }

        System.out.println("\nDOUBLE константы: ");
        for (Lexeme lex : lexemes.stream()
                .filter(x -> x.getLexemeType() == LexemeType.DOUBLE_VALUE)
                .collect(Collectors.toList())){

            System.out.println(lex);
        }

        System.out.println("\nОператоры: ");
        for (Lexeme lex : lexemes.stream()
                .filter(x -> x.getLexemeClass() == LexemeClass.OPERATOR)
                .collect(Collectors.toList())){

            System.out.println(lex);
        }
    }

    private void prepareData() throws IOException {
        checkCase = false;
        numOfStates = 11;
        int alphabetLength = 10;
        //начальное состояние
        startState = 0;
        //конечное состояние
        endStates = new int [] {10};

        Path path = Paths.get(TRANS_TABLE_PATH);
        BufferedReader reader = Files.newBufferedReader(path);

        //матрица переходов
        transFunc = new int[numOfStates][alphabetLength]; //размерность массива функции переходов
        for (int i=0; i < numOfStates; i++){
            String[] states = reader.readLine().split(" ");
            for (int j=0; j<alphabetLength; j++){
                transFunc[i][j] = Integer.parseInt(states[j]);
            }
        }
    }
}
