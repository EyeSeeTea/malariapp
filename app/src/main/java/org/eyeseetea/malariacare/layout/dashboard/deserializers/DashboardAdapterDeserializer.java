package org.eyeseetea.malariacare.layout.dashboard.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.eyeseetea.malariacare.layout.dashboard.config.DashboardAdapter;

import java.io.IOException;

/**
 * Created by idelcano on 16/05/2016.
 */
public class DashboardAdapterDeserializer extends JsonDeserializer {
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        DashboardAdapter dashboardAdapter = DashboardAdapter.fromId(p.getValueAsString());
        if (dashboardAdapter != null) {
            return dashboardAdapter;
        }
        throw new JsonMappingException("'orientation' must be 'horizontal' or 'vertical'");
    }
}