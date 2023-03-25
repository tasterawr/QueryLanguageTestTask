package com.digdes.school;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws Exception {
//        startUI();

        JavaSchoolStarter starter = new JavaSchoolStarter();
        execute(starter, "INSERT VALUES 'id'=1, 'lastName'='Петров', 'age'=30, 'cost'=5.4, 'active'=true");
        execute(starter, "INSERT VALUES 'id'=2, 'lastName'='Иванов', 'age'=25, 'cost'=3.4, 'active'=false");
        execute(starter, "INSERT VALUES 'id'=3, 'lastName'='Николаева', 'age'=26, 'cost'=4.5, 'active'=true");

        execute(starter, "select");
        execute(starter, "UPDATE values 'cost'=5.5 where 'age'> 25 and 'lastname' like '%%ол%%'");
        execute(starter, "update values 'age'=null where 'active'=false");
        execute(starter, "select where 'ID'>=2 and 'age'!=26");

        execute(starter, "delete where 'lastname' ilike '%%ОВ'");
        execute(starter, "select");
    }

    private static void startUI(){
        JavaSchoolStarter starter = new JavaSchoolStarter();
        Scanner scanner = new Scanner(System.in);
        String request = "";
        while (true){
            System.out.println("Введите запрос (Для завершения работы введите команду 'exit'): ");
            request = scanner.nextLine();
            if (request.equals("exit")) break;

            execute(starter, request);
        }
    }

    private static void execute(JavaSchoolStarter starter, String request) {
        System.out.printf("Запрос: " + request);
        try{
            List<Map<String, Object>> result = starter.execute(request);
            printResult(result);
        } catch (Exception e){
            displayError(e.getMessage());
        }
    }

    private static void printResult(List<Map<String, Object>> result){
        System.out.println("Результат запроса:");
        if (result.isEmpty()){
            System.out.println("Ничего не возвращено.");
        } else {
            result.forEach(System.out::println);
        }
        System.out.println();
    }

    private static void displayError(String error){
        System.out.println("ОШИБКА: " + error);
    }
}
