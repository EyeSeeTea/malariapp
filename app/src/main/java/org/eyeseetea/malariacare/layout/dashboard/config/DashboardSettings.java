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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.layout.dashboard.deserializers.DashboardAdapterDeserializer;
import org.eyeseetea.malariacare.layout.dashboard.deserializers.DashboardOrientationDeserializer;

import java.lang.reflect.Field;
import java.util.List;

/**
 * POJO that holds the info parsed from the settings.json
 * Created by arrizabalaga on 16/03/16.
 */
public class DashboardSettings {
    /**
     * Vertical | Horizontal orientation
     */
    @JsonDeserialize(using = DashboardAdapterDeserializer.class)
    DashboardAdapter adapter;

    /**
     * dynamic | automatic adapter
     */
    @JsonDeserialize(using = DashboardOrientationDeserializer.class)
    DashboardOrientation orientation;
    /**
     * Key that points to the layout id for the general dashboard layout (Ex: R.layout.vertical_main -> 'vertical_main')
     */
    String layout;
    /**
     * The int value of the layout property under the generated R.layout class
     */
    int resLayout;

    List<ModuleSettings> modules;

    public DashboardSettings(){

    }

    public void setAdapter(DashboardAdapter adapter) {
        this.adapter = adapter;
    }

    public DashboardAdapter getAdapter() {
        return adapter;
    }

    public DashboardOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(DashboardOrientation orientation) {
        this.orientation = orientation;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
        this.resLayout = resolve(R.layout.class,layout);
    }

    public int getResLayout() {
        return resLayout;
    }

    public List<ModuleSettings> getModules() {
        return modules;
    }

    public void setModules(List<ModuleSettings> modules) {
        this.modules = modules;
    }

    /**
     * Resolves the value of the given attribute in the given class
     * @param generatedAndroidClass
     * @param attributeName
     * @return
     */
    private int resolve(Class generatedAndroidClass, String attributeName){
        try {
            Field field = generatedAndroidClass.getField(attributeName);
            return field.getInt(null);
        }catch (Exception ex){
            return 0;
        }
    }

    @Override
    public String toString() {
        return "DashboardSettings{" +
                "orientation=" + orientation +
                ", layout='" + layout + '\'' +
                ", modules=" + modules +
                '}';
    }
}
