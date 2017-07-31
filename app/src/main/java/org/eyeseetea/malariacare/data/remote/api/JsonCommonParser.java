package org.eyeseetea.malariacare.data.remote.api;


import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eyeseetea.malariacare.domain.exception.PullApiParsingException;
import org.json.JSONObject;

public class JsonCommonParser {


    public static JsonNode toJsonNode(JSONObject jsonObject) throws PullApiParsingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = jsonObject.toString();
        try {
            return objectMapper.readValue(jsonString, JsonNode.class);
        }catch(Exception ex){
            throw new PullApiParsingException();
        }
    }

    public static JsonNode parseResponse(String responseData)throws Exception{
        try{
            JSONObject jsonResponse=new JSONObject(responseData);
            Log.i("JsonCommonParser", "parseResponse: " + jsonResponse);
            return toJsonNode(jsonResponse);
        }catch(Exception ex){
            throw new PullApiParsingException();
        }
    }
}
