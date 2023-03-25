package com.digdes.school.query_executor.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DataStorage {
    private List<Map<String, Object>> data = new ArrayList<>();

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public Stream<Map<String, Object>> getDataStream(){
        return this.data.stream();
    }

}
