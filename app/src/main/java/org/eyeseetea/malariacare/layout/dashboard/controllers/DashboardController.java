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

package org.eyeseetea.malariacare.layout.dashboard.controllers;

import org.eyeseetea.malariacare.layout.dashboard.config.DashboardOrientation;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by idelcano on 25/02/2016.
 */
public class DashboardController {
    private List<ModuleController> modules;
    private DashboardSettings dashboardSettings;


    private DashboardOrientation orientation;
    private int layout;

    public DashboardController(DashboardSettings dashboardSettings){
        this.dashboardSettings = dashboardSettings;
        this.modules = new ArrayList<>();
    }

    public DashboardController(int layout, DashboardOrientation orientation){
        this.layout=layout;
        this.orientation = orientation;
        modules=new ArrayList<>();
    }

    public DashboardOrientation getOrientation(){
        return this.orientation;
    }

    public  void addModule(ModuleController module){
        modules.add(module);
    }


    public  void removeModule(ModuleController module){
        modules.remove(module);
    }

    public ModuleController getModuleByName(String name){
        for(ModuleController module:modules){
            if(module.getName().equals(name))
                return module;
        }
        return null;
    }

    public List<ModuleController> getModules() {
        return modules;
    }

    public int getLayout() {
        return dashboardSettings.getResLayout();
    }

}
