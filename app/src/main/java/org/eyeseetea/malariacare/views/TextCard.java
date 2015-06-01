/*
 * Copyright (c) 2015.
 *
 * This file is part of Health Network QIS App.
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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: document your custom view class.
 */
public class TextCard extends TextView {
    private String mfontName = getContext().getString(R.string.normal_font);
    private String mScale = getContext().getString(R.string.settings_array_values_font_sizes_def);
    private String mDimension = getContext().getString(R.string.settings_array_values_font_sizes_def);

    private  Map<String, Map<String, Float>> fonts = null;

    public TextCard(Context context) {
        super(context);
        initMap();
        init(null, 0);
    }

    public TextCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMap();
        init(attrs, 0);
    }

    public TextCard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMap();
        init(attrs, defStyle);
    }

    private void initMap(){
        Map<String, Float> xsmall = new HashMap<>();
        xsmall.put(Constants.FONTS_XSMALL, getContext().getResources().getDimension(R.dimen.xsmall_xsmall_text_size));
        xsmall.put(Constants.FONTS_SMALL, getContext().getResources().getDimension(R.dimen.xsmall_small_text_size));
        xsmall.put(Constants.FONTS_MEDIUM, getContext().getResources().getDimension(R.dimen.xsmall_medium_text_size));
        xsmall.put(Constants.FONTS_LARGE, getContext().getResources().getDimension(R.dimen.xsmall_large_text_size));
        xsmall.put(Constants.FONTS_XLARGE, getContext().getResources().getDimension(R.dimen.xsmall_xlarge_text_size));
        Map<String, Float> small = new HashMap<>();
        small.put(Constants.FONTS_XSMALL, getContext().getResources().getDimension(R.dimen.small_xsmall_text_size));
        small.put(Constants.FONTS_SMALL, getContext().getResources().getDimension(R.dimen.small_small_text_size));
        small.put(Constants.FONTS_MEDIUM, getContext().getResources().getDimension(R.dimen.small_medium_text_size));
        small.put(Constants.FONTS_LARGE, getContext().getResources().getDimension(R.dimen.small_large_text_size));
        small.put(Constants.FONTS_XLARGE, getContext().getResources().getDimension(R.dimen.small_xlarge_text_size));
        Map<String, Float> medium = new HashMap<>();
        medium.put(Constants.FONTS_XSMALL, getContext().getResources().getDimension(R.dimen.medium_xsmall_text_size));
        medium.put(Constants.FONTS_SMALL, getContext().getResources().getDimension(R.dimen.medium_small_text_size));
        medium.put(Constants.FONTS_MEDIUM, getContext().getResources().getDimension(R.dimen.medium_medium_text_size));
        medium.put(Constants.FONTS_LARGE, getContext().getResources().getDimension(R.dimen.medium_large_text_size));
        medium.put(Constants.FONTS_XLARGE, getContext().getResources().getDimension(R.dimen.medium_xlarge_text_size));
        Map<String, Float> large = new HashMap<>();
        large.put(Constants.FONTS_XSMALL, getContext().getResources().getDimension(R.dimen.large_xsmall_text_size));
        large.put(Constants.FONTS_SMALL, getContext().getResources().getDimension(R.dimen.large_small_text_size));
        large.put(Constants.FONTS_MEDIUM, getContext().getResources().getDimension(R.dimen.large_medium_text_size));
        large.put(Constants.FONTS_LARGE, getContext().getResources().getDimension(R.dimen.large_large_text_size));
        large.put(Constants.FONTS_XLARGE, getContext().getResources().getDimension(R.dimen.large_xlarge_text_size));
        Map<String, Float> xlarge = new HashMap<>();
        xlarge.put(Constants.FONTS_XSMALL, getContext().getResources().getDimension(R.dimen.extra_xsmall_text_size));
        xlarge.put(Constants.FONTS_SMALL, getContext().getResources().getDimension(R.dimen.extra_small_text_size));
        xlarge.put(Constants.FONTS_MEDIUM, getContext().getResources().getDimension(R.dimen.extra_medium_text_size));
        xlarge.put(Constants.FONTS_LARGE, getContext().getResources().getDimension(R.dimen.extra_large_text_size));
        xlarge.put(Constants.FONTS_XLARGE, getContext().getResources().getDimension(R.dimen.extra_xlarge_text_size));
        fonts = new HashMap<>();
        fonts.put(Constants.FONTS_XSMALL, xsmall);
        fonts.put(Constants.FONTS_SMALL, small);
        fonts.put(Constants.FONTS_MEDIUM, medium);
        fonts.put(Constants.FONTS_LARGE, large);
        fonts.put(Constants.FONTS_XLARGE, xlarge);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TextCard, defStyle, 0);
            String fontName = a.getString(R.styleable.TextCard_tFontName);
            if (fontName != null){
                Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/"+fontName);
                setTypeface(font);
            }

            String dimension = a.getString(R.styleable.TextCard_tDimension);
            String scale = a.getString(R.styleable.TextCard_tScale);
            if (dimension == null) dimension = getContext().getString(R.string.settings_array_values_font_sizes_def);
            if (scale == null) scale = Session.getFontSize();
            if (!scale.equals(Constants.FONTS_SYSTEM)) setTextSize(TypedValue.COMPLEX_UNIT_SP, fonts.get(scale).get(dimension));

            this.mDimension = dimension;
            this.mScale = scale;
            this.mfontName = fontName;
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
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

