package org.eyeseetea.malariacare.layout.dashboard.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.eyeseetea.malariacare.layout.dashboard.config.MonitorFilter;

import java.io.IOException;

/**
 * Created by idelcano on 25/08/2016.
 */
public class MonitorFilterDeserializer extends JsonDeserializer {
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        MonitorFilter monitorFilter= MonitorFilter.fromId(p.getValueAsString());
        if (monitorFilter != null) {
            return monitorFilter;
        }
        throw new JsonMappingException("'filter' must be 'all', 'program' or 'orgunit'");
    }
}
