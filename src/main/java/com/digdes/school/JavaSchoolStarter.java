package com.digdes.school;

import com.digdes.school.query_executor.QueryExecutor;

import java.util.List;
import java.util.Map;

public class JavaSchoolStarter {
    private final QueryExecutor queryExecutor = new QueryExecutor();
    //Дефолтный конструктор
    public JavaSchoolStarter(){

    }

    //На вход запрос, на выход результат выполнения запроса
    public List<Map<String,Object>> execute(String request) throws Exception {
        return queryExecutor.executeRequest(request);
    }
}