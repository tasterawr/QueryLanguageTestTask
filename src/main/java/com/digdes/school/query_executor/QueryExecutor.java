package com.digdes.school.query_executor;

import com.digdes.school.query_executor.exception.NullValueException;
import com.digdes.school.query_executor.exception.TypeMismatchException;
import com.digdes.school.query_executor.exception.UnsupportedColumnException;
import com.digdes.school.query_executor.query_lexeme_analyser.LexemeType;
import com.digdes.school.query_executor.query_syntax_analyser.Command;
import com.digdes.school.query_executor.query_syntax_analyser.Entry;
import com.digdes.school.query_executor.query_syntax_analyser.EntryType;
import com.digdes.school.query_executor.query_syntax_analyser.QuerySyntaxAnalyser;
import com.digdes.school.query_executor.storage.DataStorage;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryExecutor {
    private final QuerySyntaxAnalyser syntaxAnalyser = new QuerySyntaxAnalyser();
    private final DataStorage dataStorage = new DataStorage();

    private int pos = 0;
    private int postfixPos;
    private List<Entry> entryList;
    private final Deque<Entry> stack = new LinkedList<>();
    private final List<List<Map<String,Object>>> filters = new ArrayList<>();
    private List<Map<String, Object>> result = new ArrayList<>();
    private final Map<String, Class<?>> keyTypes = Map.of("id", Long.class,
            "lastName", String.class,
            "active", Boolean.class,
            "age", Long.class,
            "cost", Double.class);

    private void resetData(){
        pos = 0;
        stack.clear();
        filters.clear();
        result.clear();
    }

    public List<Map<String, Object>> executeRequest(String request) throws TypeMismatchException, UnsupportedColumnException, NullValueException {
        resetData();
        String error = syntaxAnalyser.analyseSyntax(request);
        if (!error.equals("")){
            displayError(error);
            return Collections.emptyList();
        }

        entryList = syntaxAnalyser.getEntryList();
        postfixPos = entryList.size();
        processEntryList();
        return result;
    }

    private void processEntryList() throws TypeMismatchException, UnsupportedColumnException, NullValueException {
        while (pos < postfixPos){
            if (entryList.get(pos).getType() == EntryType.CMD) {
                checkEntry();
            } else pushElm(entryList.get(pos++));
        }
    }

    private void checkEntry() throws TypeMismatchException, UnsupportedColumnException, NullValueException {
        Command cmd = entryList.get(pos).getCmd();
        switch (cmd) {
            case UPD -> executeUpdateCommand();
            case SEL -> executeSelectCommand();
            case INS -> executeInsertCommand();
            case DEL -> executeDeleteCommand();
            case OR -> executeOrCommand();
            case AND -> executeAndCommand();
            case CMPE -> executeEqualsCommand();
            case CMPNE -> executeNotEqualsCommand();
            case CMPL -> executeLessCommand();
            case CMPLE -> executeLessEqualsCommand();
            case CMPG -> executeGreaterCommand();
            case CMPGE -> executeGreaterEqualsCommand();
            case CMP_LIKE -> executeLikeCommand(false);
            case CMP_ILIKE -> executeLikeCommand(true);
        }
    }

    private void executeUpdateCommand() throws UnsupportedColumnException, TypeMismatchException, NullValueException {
        if (filters.size() == 0){
            result.addAll(dataStorage.getData());
        } else if (result.size() == 0)
            result = filters.get(0);

        while (!stack.isEmpty()) {
            Entry value = popVal();
            String colName = getColKey(popVal().toString());
            if (value.getValue() != null){
                validateTypes(colName, value);
            }
            for (Map<String, Object> row : result) {
                row.put(colName, value.getValue());
            }
        }
        pos++;
    }

    private void executeSelectCommand(){
        if (filters.size() == 0){
            result.addAll(dataStorage.getData());
        } else if (result.size() == 0)
            result = filters.get(0);

        pos++;
    }

    private void executeInsertCommand() throws UnsupportedColumnException, TypeMismatchException, NullValueException {
        Map<String, Object> newRow = new HashMap<>();
        while (!stack.isEmpty()) {
            Entry value = popVal();
            String colName = getColKey(popVal().toString());
            if (value.getValue() != null){
                validateTypes(colName, value);
            }
            newRow.put(colName, value.getValue());
        }
        fillEmptyValues(newRow);
        dataStorage.getData().add(newRow);
        result.add(newRow);
        pos++;
    }

    private void fillEmptyValues(Map<String, Object> row){
        keyTypes.keySet().forEach(colName -> {
            if (!row.containsKey(colName)){
                row.put(colName, null);
            }
        });
    }

    private void executeDeleteCommand(){
        if (filters.size() == 0){
            result.addAll(dataStorage.getData());
        } else if (result.size() == 0)
            result = filters.get(0);

        List<Map<String, Object>> data = dataStorage.getData();
        for (Map<String, Object> row : result){
            data.remove(row);
        }
        pos++;
    }

    private void executeOrCommand(){
        Set<Map<String, Object>> set = new HashSet<>();
        for (List<Map<String, Object>> filter : filters){
            set.addAll(filter);
        }
        result = new ArrayList<>(set);
        pos++;
    }

    private void executeAndCommand(){
        result = filters.get(0);
        for (List<Map<String, Object>> filter : filters){
            result = result.stream().distinct()
                    .filter(filter::contains)
                    .collect(Collectors.toList());
        }
        pos++;
    }

    private void executeEqualsCommand() throws TypeMismatchException, UnsupportedColumnException, NullValueException {
        Entry val = popVal();
        String colName = getColKey(popVal().toString());
        validateTypes(colName, val);

        AtomicBoolean nullValue = new AtomicBoolean(false);
        Stream<Map<String, Object>> stream = dataStorage.getDataStream().filter(x -> {
            if (x.get(colName) == null){
                nullValue.set(true);
                return false;
            }
            return  x.get(colName).equals(val.getValue());
        });
        if (nullValue.get()){
            throw new NullValueException(String.format("Нельзя выполнить операцию сравнения. В столбце %s значение NULL.", colName));
        }
        filters.add(stream.collect(Collectors.toList()));

        pos++;
    }

    private void executeNotEqualsCommand() throws TypeMismatchException, UnsupportedColumnException, NullValueException {
        Entry val = popVal();
        String colName = getColKey(popVal().toString());
        validateTypes(colName, val);

        filters.add(dataStorage.getDataStream().filter(x -> {
            if (x.get(colName) == null){
                return true;
            }
            return  !x.get(colName).equals(val.getValue());
        }).collect(Collectors.toList()));
        pos++;
    }

    private void executeLessCommand() throws TypeMismatchException, UnsupportedColumnException, NullValueException {
        Entry val = popVal();
        String colName = getColKey(popVal().toString());
        validateTypes(colName, val);

        Stream<Map<String, Object>> stream = null;
        Object value = val.getValue();
        AtomicBoolean nullValue = new AtomicBoolean(false);
        stream = dataStorage.getDataStream().filter(x -> {
            if (x.get(colName) == null){
                nullValue.set(true);
                return false;
            }
            if (value instanceof Long)
                return (Long) x.get(colName) < (Long) value;
            else if (value instanceof Double)
                return (Double) x.get(colName) < (Double) value;
            return false;
        });
        if (nullValue.get()){
            throw new NullValueException(String.format("Нельзя выполнить операцию сравнения. В столбце %s значение NULL.", colName));
        }
        filters.add(stream.collect(Collectors.toList()));
        pos++;
    }

    private void executeLessEqualsCommand() throws TypeMismatchException, UnsupportedColumnException, NullValueException {
        Entry val = popVal();
        String colName = getColKey(popVal().toString());
        validateTypes(colName, val);

        Stream<Map<String, Object>> stream = null;
        Object value = val.getValue();
        AtomicBoolean nullValue = new AtomicBoolean(false);
        stream = dataStorage.getDataStream().filter(x -> {
            if (x.get(colName) == null){
                nullValue.set(true);
                return false;
            }
            if (value instanceof Long)
                return (Long) x.get(colName) <= (Long) value;
            else if (value instanceof Double)
                return (Double) x.get(colName) <= (Double) value;
            return false;
        });
        if (nullValue.get()){
            throw new NullValueException(String.format("Нельзя выполнить операцию сравнения. В столбце %s значение NULL.", colName));
        }
        filters.add(stream.collect(Collectors.toList()));
        pos++;
    }

    private void executeGreaterCommand() throws TypeMismatchException, UnsupportedColumnException, NullValueException {
        Entry val = popVal();
        String colName = getColKey(popVal().toString());
        validateTypes(colName, val);

        Stream<Map<String, Object>> stream = null;
        Object value = val.getValue();
        AtomicBoolean nullValue = new AtomicBoolean(false);
        stream = dataStorage.getDataStream().filter(x -> {
            if (x.get(colName) == null){
                nullValue.set(true);
                return false;
            }
            if (value instanceof Long)
                return (Long) x.get(colName) > (Long) value;
            else if (value instanceof Double)
                return (Double) x.get(colName) > (Double) value;
            return false;
        });
        if (nullValue.get()){
            throw new NullValueException(String.format("Нельзя выполнить операцию сравнения. В столбце %s значение NULL.", colName));
        }
        filters.add(stream.collect(Collectors.toList()));
        pos++;
    }

    private void executeGreaterEqualsCommand() throws TypeMismatchException, UnsupportedColumnException, NullValueException {
        Entry val = popVal();
        String colName = getColKey(popVal().toString());
        validateTypes(colName, val);

        Stream<Map<String, Object>> stream = null;
        Object value = val.getValue();
        AtomicBoolean nullValue = new AtomicBoolean(false);
        stream = dataStorage.getDataStream().filter(x -> {
            if (x.get(colName) == null){
                nullValue.set(true);
                return false;
            }
            if (value instanceof Long)
                return (Long) x.get(colName) >= (Long) value;
            else if (value instanceof Double)
                return (Double) x.get(colName) >= (Double) value;
            return false;
        });
        if (nullValue.get()){
            throw new NullValueException(String.format("Нельзя выполнить операцию сравнения. В столбце %s значение NULL.", colName));
        }
        filters.add(stream.collect(Collectors.toList()));
        pos++;
    }

    private void executeLikeCommand(boolean caseSensitive) throws TypeMismatchException, UnsupportedColumnException, NullValueException {
        Entry val = popVal();
        String colName = getColKey(popVal().toString());
        validateTypes(colName, val);

        String pattern = caseSensitive ? val.toString().toLowerCase(Locale.ROOT) : val.toString();
        String regex = pattern.replace("%", "(.+)");

        List<Map<String, Object>> filter = dataStorage.getDataStream().filter(x -> {
            String value = caseSensitive ? x.get(colName).toString().toLowerCase(Locale.ROOT) : x.get(colName).toString();
            return value.matches(regex);
        }).collect(Collectors.toList());

        filters.add(filter);
        pos++;
    }

    private void validateTypes(String colName, Object value) throws TypeMismatchException, NullValueException {
        Entry entry = (Entry) value;
        if (entry.getValue() == null){
            throw new NullValueException("Нельзя выполнять операцию сравнения со значением NULL.");
        }

        Class<?> requiredType = keyTypes.get(colName);
        Class<?> providedType = (entry.getValue().getClass());
        if (requiredType != providedType){
            throw new TypeMismatchException(String.format("Несочитаемые типы. Значение '%s', столбец '%s'. Тип переданного значения: %s, ожидаемый тип: %s.",
                    entry.getValue(), colName, providedType.getSimpleName(), requiredType.getSimpleName()));
        }
    }

    private String getColKey(String requestCol) throws UnsupportedColumnException {
        String plainStr = requestCol.replace("'", "");
        Optional<String> first = keyTypes.keySet().stream()
                .filter(x -> x.toLowerCase(Locale.ROOT).equals(plainStr.toLowerCase(Locale.ROOT)))
                .findFirst();

        if (first.isPresent()){
            return first.get();
        } else {
            throw new UnsupportedColumnException(String.format("Неподдерживаемое значение столбца: %s", requestCol));
        }
    }

    private Entry popVal(){
        Entry entry = stack.pop();
        if (entry.getLexemeType().equals(LexemeType.STRING_VALUE)){
            entry.setValue(entry.getValue().toString().replace("'",""));
        }
        return entry;
    }

    private void pushElm(Entry entry){
        stack.push(entry);
    }

    private static void displayError(String error){
        System.out.println("ОШИБКА: " + error);
    }
}
