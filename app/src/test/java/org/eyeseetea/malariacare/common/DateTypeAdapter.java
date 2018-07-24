package org.eyeseetea.malariacare.common;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTypeAdapter implements JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {

        Date date;

        try{
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(json.getAsString());
        }catch (Exception ex){
            try {
                date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(json.getAsString());
            } catch (Exception e) {
                date = null;
            }
        }

        return date;
    }


}
