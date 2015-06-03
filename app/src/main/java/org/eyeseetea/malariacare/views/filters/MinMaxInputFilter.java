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

package org.eyeseetea.malariacare.views.filters;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Filter that checks if the value from an input (an integer) is between a range.
 * The filter checks:
 *  min<= value <=max
 * Created by arrizabalaga on 2/06/15.
 */
public class MinMaxInputFilter implements InputFilter {
    /**
     * Minimum allowed value for the input.
     * Null means there is no minimum limit.
     */
    private Integer minAllowed;

    /**
     * Maximum allowed value for the input.
     * Null means there is no maximum limit.
     */
    private Integer maxAllowed;



    public MinMaxInputFilter(Integer min){
        this.minAllowed=min;
    }

    public MinMaxInputFilter(Integer min, Integer max){
        this(min);
        this.maxAllowed=max;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            // Remove the string out of destination that is to be replaced
            String newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend, dest.toString().length());
            // Add the new string in
            newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart, newVal.length());
            int input = Integer.parseInt(newVal);
            if (inRange(input)) {
                return null;
            }
        }catch (NumberFormatException nfe) {
        }
        return "";
    }

    /**
     * Checks if the value is between the specified range.
     *
     * @param value
     * @return
     */
    public boolean inRange(Integer value){
        boolean isMinOk=true;
        boolean isMaxOk=true;
        //No bounds -> ok
        if(minAllowed==null && maxAllowed==null){
            return true;
        }
        //Check minimum
        if(minAllowed!=null){
            if(value==null){
                isMinOk=false;
            }else{
                isMinOk=minAllowed<=value;
            }
        }
        //Check maximum
        if(maxAllowed!=null){
            if(value==null){
                isMaxOk=false;
            }else{
                isMaxOk=value<=maxAllowed;
            }
        }
        return isMinOk && isMaxOk;
    }


}
