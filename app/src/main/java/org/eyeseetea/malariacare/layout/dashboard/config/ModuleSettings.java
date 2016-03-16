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

import android.util.Log;

import org.eyeseetea.malariacare.R;

import java.lang.reflect.Field;

/**
 * POJO that holds the info for a module
 * Created by arrizabalaga on 16/03/16.
 */
public class ModuleSettings {

    private static final String TAG = ".ModuleSettings";
    public static final String MODULE_CONTROLLERS_PACKAGE = "org.eyeseetea.malariacare.layout.dashboard.controllers.";

    /**
     * Key in the strings resources to get the String name of the module (Ex: R.string.tab_tag_assess -> 'tab_tag_assess')
     */
    String name;
    /**
     * Key in the drawable resources to get the Drawable for the tabs (Ex: R.drawable.tab_assess -> 'tab_assess')
     */
    String icon;
    /**
     * Key in the color resources to get the int (color) for the tabs (Ex: R.drawable.tab_yellow_assess -> 'tab_yellow_assess')
     */
    String backgroundColor;
    /**
     * Key that points to the layout id for the general module layout (Ex: R.id.tab_assess_layout -> 'tab_assess_layout')
     */
    String layout;
    /**
     * Name of moduleController class that will be in charge of the behaviour of this module
     */
    String controller;

    /**
     * The int value of the name property under the generated R.strings class
     */
    int resName;
    /**
     *  The int value of the icon property under the generated R.drawable class
     */
    int resIcon;
    /**
     * The int value of the backgroundColor property under the generated R.color class
     */
    int resBackgroundColor;
    /**
     * The int value of the layout property under the generated R.id class
     */
    int resLayout;
    /**
     * R.strings.name value for the name (an int)
     */
    Class classController;

    ModuleSettings(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.resName = resolve(R.string.class,name);
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
        this.resIcon = resolve(R.drawable.class,icon);
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.resBackgroundColor= resolve(R.color.class,backgroundColor);
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
        this.resLayout= resolve(R.id.class,layout);
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
        this.classController = resolveClass(controller);
    }

    public int getResName() {
        return resName;
    }

    public int getResIcon() {
        return resIcon;
    }

    public int getResBackgroundColor() {
        return resBackgroundColor;
    }

    public int getResLayout() {
        return resLayout;
    }

    public Class getClassController() {
        return classController;
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
//            Log.e(TAG, String.format("%s does NOT exist in %s", attributeName, generatedAndroidClass.getCanonicalName()));
            return 0;
        }
    }

    /**
     * Turns a "AssessModuleController" string into its AssessModuleController class
     * @param className
     * @return
     */
    private Class resolveClass(String className){
        try{
            return Class.forName(MODULE_CONTROLLERS_PACKAGE +className);
        }catch(Exception ex){
//            Log.e(TAG,String.format("Class %s can NOT be resolved",className));
            return null;
        }
    }
}
