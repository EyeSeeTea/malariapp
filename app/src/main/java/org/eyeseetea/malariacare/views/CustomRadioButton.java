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
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RadioGroup;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;

public class CustomRadioButton extends androidx.appcompat.widget.AppCompatRadioButton implements IEyeSeeView {
    private Context context = getContext();
    private String mfontName = context.getString(R.string.normal_font);
    private String mScale = context.getString(R.string.settings_array_values_font_sizes_def);
    private String mDimension = context.getString(R.string.settings_array_values_font_sizes_def);
    private AssetManager assetManager = context.getAssets();
    private TypedArray a;
    private Typeface font;

    private OptionDB option = null;

    public CustomRadioButton(Context context) {
        super(context);
        init(null, 0);
    }

    public CustomRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CustomRadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public CustomRadioButton(Context context, OptionDB option, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setTag(option);
        this.setText(option.getName());
        init(attrs, defStyle);
    }

    public CustomRadioButton(Context context, OptionDB option) {
        super(context);
        this.setTag(option);
        this.setText(option.getName());
        init(null, 0);
    }

    public CustomRadioButton(Context context, OptionDB option, AttributeSet attrs) {
        super(context, attrs);
        this.setTag(option);
        this.setText(option.getName());
        init(attrs, 0);
    }

    /**
     * Initializing method. Sets font name and font size depending on the styled attributes selected
     * @param attrs
     * @param defStyle
     */
    public void init(AttributeSet attrs, int defStyle) {

        if(!PreferencesState.getInstance().isVerticalDashboard() && isInEditMode()){
            return;
        }

        // Load attributes
        if (attrs != null) {
            a = context.obtainStyledAttributes(attrs, R.styleable.CustomRadioButton, defStyle, 0);
            try {
                mfontName = a.getString(R.styleable.CustomRadioButton_rFontName);
                if (mfontName != null) {
                    setTypeface(TypefaceCache.getInstance().getTypeface(mfontName));
                }

                mDimension = a.getString(R.styleable.CustomRadioButton_rDimension);
                mScale = a.getString(R.styleable.CustomRadioButton_rScale);
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
     * Update styleable properties
     */

    /**
     * Call sequentially all the updateXXX methods for each styleable properties
     * @param scale global size selected by the user
     * @param dimension specific dimension of this component related to the rest of the components
     * @param fontName font name, within listed in fonts folder inside the resources of the app
     */
    public void updateProperties(String scale, String dimension, String fontName){
        updateFontName(fontName);
        updateFontSize(scale, dimension);
    }

    /**
     * Set the Object font name. This must be a valid font placed in fonts subfolder, inside the resources of the app
     * @param fontName
     */
    public void updateFontName(String fontName){
        if (fontName != null){
            setTypeface(TypefaceCache.getInstance().getTypeface(fontName));
            mfontName = fontName;
        }
    }

    /**
     * Set the Object font size. This will be determined by the scale and dimension parameters, both one of [xsmall|small|medium|large|xlarge ] choices
     * @param dimension size related to rest of screen objects
     * @param scale global app font size established in preferences
     */
    public void updateFontSize(String scale, String dimension){
        if (dimension != null && scale != null){
            this.mDimension = dimension;
            this.mScale = scale;
            if (!scale.equals(context.getString(R.string.font_size_system))) {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, PreferencesState.getInstance().getFontSize(scale,dimension));
            }
        }
    }

    @Override
    /**
     * Sets the view's mFontName attribute value. This is intended to be a String that represents the font name we need to use here.
     *
     * @param mFontName The dimension for this component.
     */
    public void setmFontName(String mFontName) {
        this.mfontName = mFontName;
    }

    @Override
    /**
     * Gets the view's mFontName attribute value. This is intended to be a String that represents the font name we need to use here.
     *
     * @return The dimension for this component.
     */
    public String getmFontName() {
        return this.mfontName;
    }

    @Override
    /**
     * Sets the view's mDimension attribute value. This is intended to be a String that represents component dimension size of this font [xsmall|small|medium|large|xlarge]].
     *
     * @param mDimension The dimension for this component.
     */
    public void setmDimension(String mDimension) {
        this.mDimension = mDimension;
        if (getmScale() != getContext().getString(R.string.font_size_system))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, PreferencesState.getInstance().getFontSize(mScale, mDimension));
    }

    @Override
    /**
     * Gets the view's mDimension attribute value. This is intended to be a String that represents component dimension size of this font [xsmall|small|medium|large|xlarge]].
     *
     * @return The dimension for this component.
     */
    public String getmDimension() {
        return this.mDimension;
    }

    @Override
    /**
     * Sets the view's mScale attribute value. This is intended to be a String that represents the global font scale on this app [xsmall|small|medium|large|xlarge]].
     *
     * @param mScale The scale for this component.
     */
    public void setmScale(String mScale) {
        if (!mScale.equals(this.mScale))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, PreferencesState.getInstance().getFontSize(mScale, mDimension));
        this.mScale = mScale;
    }

    @Override
    /**
     * Gets the view's mScale attribute value. This is intended to be a String that represents the global font scale on this app [xsmall|small|medium|large|xlarge]].
     *
     * @return The scale for this component.
     */
    public String getmScale() {
        return this.mScale;
    }

    public OptionDB getOption() {
        return option;
    }

    public void setOption(OptionDB option) {
        this.option = option;
        this.setTag(option);
        this.setText(option.getName());
    }

    @Override
    /**
     * toggle the selection in the Radiobutton. Here we provide the capability of deselection when already pressed answer in pressed.
     */
    public void toggle() {
        if(isChecked()) {
            if(getParent() instanceof RadioGroup) {
                ((RadioGroup)getParent()).clearCheck();
            }
        } else {
            setChecked(true);
        }
    }
}
