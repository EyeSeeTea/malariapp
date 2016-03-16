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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by idelcano on 25/02/2016.
 */
public class DashboardController {
    private List<ModuleController> modules;
    public static int VERTICAL=0;
    public static int HORIZONTAL=1;
    private int style;
    private int layout;

    public DashboardController(int layout, int style){
        this.layout=layout;
        this.style=style;
        modules=new ArrayList<>();
    }

    public  void addModule(ModuleController module){
        modules.add(module);
    }


    public  void removeModule(ModuleController module){
        modules.remove(module);
    }

    public void showModules(){
        if(style==VERTICAL){

        }
        else if(style==HORIZONTAL){

        }
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

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }
}
