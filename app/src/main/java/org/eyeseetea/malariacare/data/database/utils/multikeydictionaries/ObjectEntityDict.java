package org.eyeseetea.malariacare.data.database.utils.multikeydictionaries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectEntityDict {
    Map<String, Map<String, Object>> modelMap;

    public ObjectEntityDict(){
        modelMap = new HashMap<>();
    }

    public void put(String key1, String key2, Object model) {
        Map<String, Object> modelSubMap = modelMap.get(key1);
        if (modelSubMap == null)
            modelSubMap = new HashMap<>();
        modelSubMap.put(key2, model);
        modelMap.put(key1, modelSubMap);
    }

    public Object get(String key1, String key2){
        Map<String, Object> modelSubMap = modelMap.get(key1);
        if (modelSubMap == null) return null;
        else return modelSubMap.get(key2);
    }

    public void clear(){
        modelMap.clear();
    }

    public boolean containsKey(String key1, String key2){
        Map<String, Object> modelSubMap = modelMap.get(key1);
        return (modelSubMap==null) ? false : (modelSubMap.containsKey(key2));
    }

    public List<?extends  Object> values(){
        List<Object> modelSubMap = new ArrayList<>();
        for (Map<String, Object> map : modelMap.values()){
            modelSubMap.addAll(map.values());
        }
        return modelSubMap;
    }
}
