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

package org.eyeseetea.malariacare.layout.dashboard.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.eyeseetea.malariacare.layout.dashboard.config.DashboardListFilter;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardOrientation;

import java.io.IOException;

/**
 * Created by idelcano on 30/05/2016.
 */
public class DashboardListFilterDeserializer extends JsonDeserializer {
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        DashboardListFilter dashboardListFilter = DashboardListFilter.fromId(p.getValueAsString());
        if (dashboardListFilter != null) {
            return dashboardListFilter;
        }
        throw new JsonMappingException("'listFilter' must be 'lastForOU' or 'none'");
    }
}
