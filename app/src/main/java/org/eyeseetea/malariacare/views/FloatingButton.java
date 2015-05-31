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
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.ImageButton;

import org.eyeseetea.malariacare.R;

/**
 * TODO: document your custom view class.
 */
public class FloatingButton extends ImageButton {
    private String mString = getContext().getString(R.string.empty_string);
    private int mColor = getContext().getResources().getColor(R.color.white);
    private float mDimension = getContext().getResources().getDimension(R.dimen.float_button_def);
    private Drawable mDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    public FloatingButton(Context context) {
        super(context);
        init(null, 0);
    }

    public FloatingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public FloatingButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingButton, defStyle, 0);

        mString = a.getString(R.styleable.FloatingButton_fString);
        mColor = a.getColor(R.styleable.FloatingButton_fColor, mColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mDimension = a.getDimension(R.styleable.FloatingButton_fDimension, mDimension);

        if (a.hasValue(R.styleable.FloatingButton_fDrawable)) {
            mDrawable = a.getDrawable(R.styleable.FloatingButton_fDrawable);
            mDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(this.mDimension);
        mTextPaint.setColor(this.mColor);
        mTextWidth = mTextPaint.measureText(this.mString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the text.
        canvas.drawText(mString,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight) / 2,
                mTextPaint);

        // Draw the example mDrawable on top of the text.
        if (mDrawable != null) {
            mDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mDrawable.draw(canvas);
        }
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getmString() {
        return mString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param mString The example string attribute value to use.
     */
    public void setmString(String mString) {
        this.mString = mString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getmColor() {
        return mColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param mColor The example color attribute value to use.
     */
    public void setmColor(int mColor) {
        this.mColor = mColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example mDimension attribute value.
     *
     * @return The example mDimension attribute value.
     */
    public float getmDimension() {
        return mDimension;
    }

    /**
     * Sets the view's example mDimension attribute value. In the example view, this mDimension
     * is the font size.
     *
     * @param mDimension The example mDimension attribute value to use.
     */
    public void setmDimension(float mDimension) {
        this.mDimension = mDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example mDrawable attribute value.
     *
     * @return The example mDrawable attribute value.
     */
    public Drawable getmDrawable() {
        return mDrawable;
    }

    /**
     * Sets the view's example mDrawable attribute value. In the example view, this mDrawable is
     * drawn above the text.
     *
     * @param mDrawable The example mDrawable attribute value to use.
     */
    public void setmDrawable(Drawable mDrawable) {
        this.mDrawable = mDrawable;
    }
}
