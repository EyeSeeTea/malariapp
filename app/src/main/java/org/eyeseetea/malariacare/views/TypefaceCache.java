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

package org.eyeseetea.malariacare.views;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache for typefaces to avoid building them from resources each time
 * Created by arrizabalaga on 27/07/16.
 */
public class TypefaceCache {
    public static final String FONTS_PATH = "fonts/";
    private AssetManager assetManager;
    private Map<String, Typeface> fontsCache;
    private static TypefaceCache instance;

    TypefaceCache(){
        fontsCache = new HashMap<>();
    }

    public static TypefaceCache getInstance(){
        if(instance==null){
            instance = new TypefaceCache();
        }
        return instance;
    }

    public void init(Context context){
        this.assetManager = context.getAssets();
    }

    public Typeface getTypeface(String fontName){
        Typeface typeface = fontsCache.get(fontName);
        if(typeface==null){
            typeface = Typeface.createFromAsset(assetManager, FONTS_PATH + fontName);
            fontsCache.put(fontName,typeface);
        }
        return typeface;
    }
}
