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

package org.eyeseetea.malariacare.views.filters.test;

import org.eyeseetea.malariacare.views.filters.MinMaxInputFilter;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by arrizabalaga on 2/06/15.
 */
public class MinMaxInputFilterTest {

    @Test
    public void no_bounds_null_accepted(){
        //GIVEN
        MinMaxInputFilter minMaxInputFilter=new MinMaxInputFilter(null);

        //WHEN
        boolean result=minMaxInputFilter.inRange(null);

        //THEN
        assertTrue(result);
        assertFalse(result);
    }

    @Test
    public void no_min_null_rejected(){
        //GIVEN
        MinMaxInputFilter minMaxInputFilter=new MinMaxInputFilter(null,10);

        //WHEN
        boolean result=minMaxInputFilter.inRange(null);

        //THEN
        assertFalse(result);
    }

    @Test
    public void no_max_null_rejected(){
        //GIVEN
        MinMaxInputFilter minMaxInputFilter=new MinMaxInputFilter(5,null);

        //WHEN
        boolean result=minMaxInputFilter.inRange(null);

        //THEN
        assertFalse(result);
    }

    @Test
    public void no_max_value_accepted(){
        //GIVEN
        MinMaxInputFilter minMaxInputFilter=new MinMaxInputFilter(5,null);

        //WHEN
        boolean result=minMaxInputFilter.inRange(5);

        //THEN
        assertTrue(result);
    }

    @Test
    public void bounds_set_value_inside_accepted(){
        //GIVEN
        MinMaxInputFilter minMaxInputFilter=new MinMaxInputFilter(0,10);

        //WHEN
        boolean result=minMaxInputFilter.inRange(5);

        //THEN
        assertTrue(result);
    }

    @Test
    public void bounds_set_value_outside_rejected(){
        //GIVEN
        MinMaxInputFilter minMaxInputFilter=new MinMaxInputFilter(0,10);

        //WHEN
        boolean result=minMaxInputFilter.inRange(-5);

        //THEN
        assertFalse(result);
    }
}
