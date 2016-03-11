/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.test.utils;

import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by arrizabalaga on 25/05/15.
 */
public class SizeListMatcher extends TypeSafeMatcher<View> {
    private final int expectedSize;

    private SizeListMatcher(int size) {
        this.expectedSize = checkNotNull(size);
    }

    @Override
    public boolean matchesSafely(View view) {
        //+2 required for header + footer
        return ((ListView) view).getCount () == (expectedSize+2);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(String.format("ListView should have %d children ",expectedSize));
    }

    public static Matcher<? super View> withListSize(final int size) {
        return new SizeListMatcher(size);
    }
}