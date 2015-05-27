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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static com.google.android.apps.common.testing.deps.guava.base.Preconditions.checkNotNull;

/**
 * Created by arrizabalaga on 25/05/15.
 */
public class ErrorTextMatcher extends TypeSafeMatcher<View> {
    private final String expectedError;

    private ErrorTextMatcher(String expectedError) {
        this.expectedError = checkNotNull(expectedError);
    }

    @Override
    public boolean matchesSafely(View view) {
        if (!(view instanceof EditText)) {
            return false;
        }
        EditText editText = (EditText) view;
        return expectedError.equals(editText.getError());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("with error: " + expectedError);
    }

    public static Matcher<? super View> hasErrorText(String expectedError) {
        return new ErrorTextMatcher(expectedError);
    }
}