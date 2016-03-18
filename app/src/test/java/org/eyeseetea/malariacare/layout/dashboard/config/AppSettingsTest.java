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

package org.eyeseetea.malariacare.layout.dashboard.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.layout.dashboard.controllers.AssessModuleController;
import org.eyeseetea.malariacare.views.filters.MinMaxInputFilter;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by arrizabalaga on 2/06/15.
 */
public class AppSettingsTest {

    /**
     * {"database":%s,"dashboard":%s}
     */
    public static final String JSON_APP_TEMPLATE = "{\"database\":%s,\"dashboard\":%s}";
    /**
     * {"originType":"%s","uri":"%s"}
     */
    public static final String JSON_DATABASE_TEMPLATE = "{\"originType\":\"%s\",\"uri\":\"%s\"}";
    /**
     * {"orientation":"%s","modules":%s}
     */
    public static final String JSON_DASHBOARD_TEMPLATE = "{\"orientation\":\"%s\",\"layout\":\"%s\",\"modules\":[%s]}";
    /**
     * {"name":"%s","icon":"%s","backgroundColor":"%s","layout":"%s","controller":"%s"}
     */
    public static final String JSON_MODULE_TEMPLATE = "{\"name\":\"%s\",\"icon\":\"%s\",\"backgroundColor\":\"%s\",\"layout\":\"%s\",\"controller\":\"%s\"}";

    @Test
    public void parse_full_json(){
        //GIVEN
        ObjectMapper mapper = new ObjectMapper();
        String expectedDatabaseUri="file://lalala";
        String jsonDatabase = String.format(JSON_DATABASE_TEMPLATE,"dhis",expectedDatabaseUri);
        String jsonModule1 = String.format(JSON_MODULE_TEMPLATE,"tab_tag_assess","tab_assess","tab_yellow_assess","dashboard_details_container","AssessModuleController");
        String jsonModules =jsonModule1+","+jsonModule1;
        String jsonDashboard = String.format(JSON_DASHBOARD_TEMPLATE,"vertical","vertical_main",jsonModules);
        String jsonInString = String.format(JSON_APP_TEMPLATE,jsonDatabase,jsonDashboard);

        //WHEN
        AppSettings appSettings=null;
        try {
            appSettings = mapper.readValue(jsonInString, AppSettings.class);
        }catch (Exception ex){
            fail(ex.getMessage());
        }

        //THEN appSettings is ok
        assertNotNull(appSettings);

        //THEN databaseSettings is ok
        DatabaseSettings parsedDatabaseSettings=appSettings.getDatabaseSettings();
        assertNotNull(parsedDatabaseSettings);
        assertEquals(DatabaseOriginType.DHIS, parsedDatabaseSettings.getOriginType());
        assertEquals(expectedDatabaseUri, parsedDatabaseSettings.getUri());

        //THEN dashboardSettings is ok
        DashboardSettings dashboardSettings = appSettings.getDashboardSettings();
        assertNotNull(dashboardSettings);
        assertEquals(DashboardOrientation.VERTICAL,dashboardSettings.getOrientation());
        assertEquals(R.layout.vertical_main,dashboardSettings.getResLayout());

        //THEN modules is ok
        List<ModuleSettings> modules = dashboardSettings.getModules();
        assertEquals(2,modules.size());
        assertModuleSettings(modules.get(0));
    }

    @Test
    public void parse_a_module(){
        //GIVEN
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = String.format(JSON_MODULE_TEMPLATE,"tab_tag_assess","tab_assess","tab_yellow_assess","dashboard_details_container","AssessModuleController");

        //WHEN
        ModuleSettings moduleSettings=null;
        try {
            moduleSettings = mapper.readValue(jsonInString, ModuleSettings.class);
        }catch (Exception ex){
            fail(ex.getMessage());
        }

        //THEN moduleSettings is ok
        assertModuleSettings(moduleSettings);
    }

    @Test
    public void unexpected_orientation_raises_exception(){
        //GIVEN
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "{\"database\":{},\"dashboard\":{\"orientation\":\"diagonal\"}}";

        //WHEN
        AppSettings appSettings=null;
        try {
            appSettings = mapper.readValue(jsonInString, AppSettings.class);
        }catch (Exception ex){
        //THEN: An exception must be raised
            return;
        }

        fail("No exception was raised after reading a malformed orientation");
    }


    private void assertModuleSettings(ModuleSettings moduleSettings) {
        assertNotNull(moduleSettings);
        assertEquals(R.string.tab_tag_assess, moduleSettings.getResName());
        assertEquals(R.drawable.tab_assess,moduleSettings.getResIcon());
        assertEquals(R.color.tab_yellow_assess,moduleSettings.getResBackgroundColor());
        assertEquals(R.id.dashboard_details_container,moduleSettings.getResLayout());
        assertEquals(AssessModuleController.class, moduleSettings.getClassController());
    }
}
