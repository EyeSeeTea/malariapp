/*
 * Copyright (c) 2016.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.data.database.utils.multikeydictionaries;

import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by idelcano on 19/09/2016.
 */
public class ObjectModelDict {
    Map<String, Map<String, BaseModel>> modelMap;

    public ObjectModelDict(){
        modelMap = new HashMap<>();
    }

    public void put(String key1, String key2, BaseModel model) {
        Map<String, BaseModel> modelSubMap = modelMap.get(key1);
        if (modelSubMap == null)
            modelSubMap = new HashMap<>();
        modelSubMap.put(key2, model);
        modelMap.put(key1, modelSubMap);
    }

    public BaseModel get(String key1, String key2){
        Map<String, BaseModel> modelSubMap = modelMap.get(key1);
        if (modelSubMap == null) return null;
        else return modelSubMap.get(key2);
    }

    public void clear(){
        modelMap.clear();
    }

    public boolean containsKey(String key1, String key2){
        Map<String, BaseModel> modelSubMap = modelMap.get(key1);
        return (modelSubMap==null) ? false : (modelSubMap.containsKey(key2));
    }

    public List<?extends  BaseModel> values(){
        List<BaseModel> modelSubMap = new ArrayList<>();
        for (Map<String, BaseModel> map : modelMap.values()){
            modelSubMap.addAll(map.values());
        }
        return modelSubMap;
    }
}
