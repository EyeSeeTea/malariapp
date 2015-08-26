/*
 * Copyright (c) 2015.
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
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Button;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.utils.PreferencesState;

/**
 * TODO: document your custom view class.
 */
public class CustomButton extends Button implements IEyeSeeView{
    private Context context = getContext();
    private String mfontName = context.getString(R.string.normal_font);
    private String mScale = context.getString(R.string.settings_array_values_font_sizes_def);
    private String mDimension = context.getString(R.string.settings_array_values_font_sizes_def);
    private AssetManager assetManager = context.getAssets();
    private TypedArray a;
    private Typeface font;

    public CustomButton(Context context) {
        super(context);
        init(null, 0);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }



    public void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        if (attrs != null) {
            a = context.obtainStyledAttributes(attrs, R.styleable.CustomButton, defStyle, 0);
            try {
                mfontName = a.getString(R.styleable.CustomButton_bFontName);
                if (mfontName != null) {
                    font = Typeface.createFromAsset(assetManager, "fonts/" + mfontName);
                    setTypeface(font);
                }

                mDimension = a.getString(R.styleable.CustomButton_bDimension);
                mScale = a.getString(R.styleable.CustomButton_bScale);
                if (mDimension == null)
                    mDimension = context.getString(R.string.settings_array_values_font_sizes_def);
                if (mScale == null) mScale = PreferencesState.getInstance().getScale();
                if (!mScale.equals(context.getString(R.string.font_size_system)))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, PreferencesState.getInstance().getFontSize(mScale, mDimension));
            } finally {
                a.recycle();
            }
        }
    }

    /**
     * Sets the view's mFontName attribute value. This is intended to be a String that represents the font filename.
     *
     * @param mFontName The example getmDimension attribute value to use.
     */
    public void setmFontName(String mFontName) {
        this.mDimension = mDimension;
    }

    /**
     * Gets the mDimension attribute value.
     *
     * @return The mDimension attribute value.
     */
    public String getmFontName() {
        return mfontName;
    }

    /**
     * Sets the view's mDimension attribute value. In this case, this is the fontSize divided into some dicrete levels
     *
     * @param mDimension The example getmDimension attribute value to use.
     */
    public void setmDimension(String mDimension) {
        this.mDimension = mDimension;
        if (getmScale() != getContext().getString(R.string.font_size_system))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, PreferencesState.getInstance().getFontSize(mScale, mDimension));
    }

    /**
     * Gets the mDimension attribute value.
     *
     * @return The dimension attribute value.
     */
    public String getmDimension() {
        return mDimension;
    }

    /**
     * Sets the view's mDimension attribute value. In this case, this is the fontSize scale separated into some discrete levels
     *
     * @param mScale The example scale attribute value to use.
     */
    public void setmScale(String mScale) {
        if (!mScale.equals(this.mScale))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, PreferencesState.getInstance().getFontSize(mScale, mDimension));
        this.mScale = mScale;
    }

    /**
     * Gets the mDimension attribute value.
     *
     * @return The scale attribute value.
     */
    public String getmScale() {
        return this.mScale;
    }
}

