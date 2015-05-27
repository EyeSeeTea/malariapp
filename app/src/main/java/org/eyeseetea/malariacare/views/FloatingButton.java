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
    private String contentString = getContext().getString(R.string.empty_string); // TODO: use a default from R.string...
    private int contentColor = getContext().getResources().getColor(R.color.white); // TODO: use a default from R.color...
    private float dimension = 0; // TODO: use a default from R.dimen...
    private Drawable contentDrawable;

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
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.FloatingButton, defStyle, 0);

        contentString = a.getString(
                R.styleable.FloatingButton_contentString);
        contentColor = a.getColor(
                R.styleable.FloatingButton_contentColor,
                contentColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        dimension = a.getDimension(
                R.styleable.FloatingButton_dimension,
                dimension);

        if (a.hasValue(R.styleable.FloatingButton_drawable)) {
            contentDrawable = a.getDrawable(
                    R.styleable.FloatingButton_drawable);
            contentDrawable.setCallback(this);
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
        mTextPaint.setTextSize(dimension);
        mTextPaint.setColor(contentColor);
        mTextWidth = mTextPaint.measureText(contentString);

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
        canvas.drawText(contentString,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight) / 2,
                mTextPaint);

        // Draw the example contentDrawable on top of the text.
        if (contentDrawable != null) {
            contentDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            contentDrawable.draw(canvas);
        }
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getContentString() {
        return contentString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param contentString The example string attribute value to use.
     */
    public void setContentString(String contentString) {
        this.contentString = contentString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getContentColor() {
        return contentColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param contentColor The example color attribute value to use.
     */
    public void setExampleColor(int contentColor) {
        this.contentColor = contentColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float dimension() {
        return dimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param dimension The example dimension attribute value to use.
     */
    public void setDimension(float dimension) {
        this.dimension = dimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example contentDrawable attribute value.
     *
     * @return The example contentDrawable attribute value.
     */
    public Drawable getContentDrawable() {
        return contentDrawable;
    }

    /**
     * Sets the view's example contentDrawable attribute value. In the example view, this contentDrawable is
     * drawn above the text.
     *
     * @param contentDrawable The example contentDrawable attribute value to use.
     */
    public void setContentDrawable(Drawable contentDrawable) {
        this.contentDrawable = contentDrawable;
    }
}
