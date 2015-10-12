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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class CustomListView extends ListView implements View.OnTouchListener, AbsListView.OnScrollListener {

    private int listViewTouchAction;
    private static final int MAXIMUM_LIST_ITEMS_VIEWABLE = 99;

    public CustomListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        listViewTouchAction = -1;
        setOnScrollListener(this);
        setOnTouchListener(this);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount)
    {
        if (getAdapter() != null && getAdapter().getCount() > MAXIMUM_LIST_ITEMS_VIEWABLE)
        {
            if (listViewTouchAction == MotionEvent.ACTION_MOVE)
            {
                scrollBy(0, -1);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int newHeight = 0;
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY)
        {
            ListAdapter listAdapter = getAdapter();
            if (listAdapter != null && !listAdapter.isEmpty())
            {
                int listPosition = 0;
                for (listPosition = 0; listPosition < listAdapter.getCount()
                        && listPosition < MAXIMUM_LIST_ITEMS_VIEWABLE; listPosition++)
                {
                    View listItem = listAdapter.getView(listPosition, null, this);
                    //now it will not throw a NPE if listItem is a ViewGroup instance
                    if (listItem instanceof ViewGroup)
                    {
                        listItem.setLayoutParams(new LayoutParams(
                                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    }
                    listItem.measure(widthMeasureSpec, heightMeasureSpec);
                    newHeight += listItem.getMeasuredHeight();
                }
                newHeight += getDividerHeight() * listPosition;
            }
            if ((heightMode == MeasureSpec.AT_MOST) && (newHeight > heightSize))
            {
                if (newHeight > heightSize)
                {
                    newHeight = heightSize;
                }
            }
        }
        else
        {
            newHeight = getMeasuredHeight();
        }
        setMeasuredDimension(getMeasuredWidth(), newHeight);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (getAdapter() != null && getAdapter().getCount() > MAXIMUM_LIST_ITEMS_VIEWABLE)
        {
            if (listViewTouchAction == MotionEvent.ACTION_MOVE)
            {
                scrollBy(0, 1);
            }
        }
        return false;
    }
}