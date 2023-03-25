package com.digdes.school;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws Exception {
        JavaSchoolStarter starter = new JavaSchoolStarter();
        Scanner scanner = new Scanner(System.in);


        String request = "";
        while (true){
            System.out.println("Введите запрос (Для завершения работы введите команду 'exit'): ");
            request = scanner.nextLine();
            if (request.equals("exit")) break;

            List<Map<String, Object>> result = starter.execute(request);
            printResult(result);
        }
    }

    private static void printResult(List<Map<String, Object>> result){
        System.out.println("Результат запроса");
        if (result.isEmpty()){
            System.out.println("Ничего не возвращено.");
        } else {
            result.forEach(System.out::println);
        }
    }
}
