/*
 * Copyright (c) 2016.
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

package org.eyeseetea.malariacare.layout.utils;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.views.CustomButton;
import org.eyeseetea.malariacare.views.CustomEditText;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO that holds visual components a ease its manipulation
 * Created by arrizabalaga on 19/04/16.
 */
public class AutoTabViewHolder {
    //Label
    public CustomTextView statement;

    // Main component in the row: Spinner, EditText or RadioGroup
    public View component;

    public View parentImage;

    public View parentImageShown;

    public View childrenImage;

    public View notChildParentSpace;

    private List<View> columnComponents;

    public CustomTextView num;
    public CustomTextView denum;

    public AutoTabViewHolder() {
        columnComponents = new ArrayList<>();
    }

    public AutoTabViewHolder(View component) {
        this();
        this.component = component;
        this.parentImage = ((ViewGroup)component.getParent()).findViewById(R.id.parent_img);
        this.childrenImage = ((ViewGroup)component.getParent()).findViewById(R.id.child_img);
    }

    public void addColumnComponent(View component) {
        this.columnComponents.add(component);
    }

    public View getColumnComponent(int position) {
        if (position > (this.columnComponents.size() - 1)) {
            return null;
        }

        return this.columnComponents.get(position);
    }

    /**
     * Fixes a bug in older apis where a RadioGroup cannot find its children by id
     *
     * @param id
     * @return
     */
    public CustomRadioButton findRadioButtonById(int id) {
        //No component -> done
        if (component == null || !(component instanceof RadioGroup)) {
            return null;
        }

        //Modern api -> delegate in its method
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            return (CustomRadioButton) component.findViewById(id);
        }

        //Find button manually
        for (int i = 0; i < ((RadioGroup) component).getChildCount(); i++) {
            View button = ((RadioGroup) component).getChildAt(i);
            if (button.getId() == id) {
                return (CustomRadioButton) button;
            }
        }
        return null;
    }


    public void setText(String text) {
        if (component == null) {
            return;
        }

        if (component instanceof CustomEditText)
            ((CustomEditText) component).setText(text);
        if (component instanceof CustomButton)
            ((CustomButton) component).setText(text);
    }

    public void setNumText(String text) {
        if (num == null) {
            return;
        }

        num.setText(text);
    }

    public void setDenumText(String text) {
        if (denum == null) {
            return;
        }
        denum.setText(text);
    }

    public void setSpinnerSelection(int position) {
        if (component == null || !(component instanceof Spinner)) {
            return;
        }

        ((Spinner) component).setSelection(position);
    }

    public void setRadioChecked(OptionDB option) {
        if (component == null || !(component instanceof RadioGroup) || component.findViewWithTag(option)==null) {
            return;
        }
        ((CustomRadioButton) component.findViewWithTag(option)).setChecked(true);
    }

    public void setSwitchOption(OptionDB option) {
        if (component == null || !(component instanceof Switch)) {
            return;
        }

        boolean isChecked = false;
        String switchText = "";
        if (option != null) {
            isChecked = Boolean.valueOf(option.getCode());
            switchText = option.getName();
        }

        Switch switchButton = (Switch) component;
        switchButton.setChecked(isChecked);
        switchButton.setText(switchText);
    }
}
