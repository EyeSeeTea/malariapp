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

package org.eyeseetea.malariacare.layout;

import android.app.Fragment;
import android.graphics.drawable.Drawable;

/**
 * Created by idelcano on 15/02/2016.
 */
public class Module{

    Fragment fragment;
    Boolean visible;
    String tagName;
    String type;
    int layout;
    Drawable icon;


    public Module(Fragment fragment, Boolean visible, int layout, String tagName, Drawable icon){
        this.fragment=fragment;
        this.tagName=tagName;
        this.visible=visible;
        this.layout=layout;
        this.icon=icon;
    }

    public Fragment getFragment(){
        return fragment;
    }

    public void setFragment(Fragment fragment){
        this.fragment=fragment;
    }

    public Boolean getVisible(){
        return visible;
    }

    public void setVisible(Boolean visible){
        this.visible=visible;
    }

    public String getTagName(){
        return tagName;
    }

    public void setTagName(String tagName){
        this.tagName=tagName;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type=type;
    }

    public int getLayout(){
        return layout;
    }

    public void setLayout(int layout){
        this.layout=layout;
    }

    public Drawable getIcon(){
        return icon;
    }

    public void setIcon(Drawable icon){
        this.icon=icon;
    }
}
