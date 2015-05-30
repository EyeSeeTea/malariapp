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

package org.eyeseetea.malariacare.layout.adapters.survey.autoTabUtils;

import android.content.Context;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.eyeseetea.malariacare.database.model.Option;

/**
 * Created by adrian on 30/05/15.
 */
public class UncheckeableRadioButton extends RadioButton{


    public UncheckeableRadioButton(Context context) {
        super(context);
    }

    public UncheckeableRadioButton(Context context, Option option) {
        super(context);
        this.setTag(option);
        this.setText(option.getName());
    }

    @Override
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
