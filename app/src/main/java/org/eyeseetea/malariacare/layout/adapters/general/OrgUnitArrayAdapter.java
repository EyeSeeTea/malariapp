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

package org.eyeseetea.malariacare.layout.adapters.general;

import android.content.Context;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.List;

/**
 * Created by adrian on 30/04/15.
 */
public class OrgUnitArrayAdapter extends AddlArrayAdapter<OrgUnitDB> {

    public OrgUnitArrayAdapter(Context context, List<OrgUnitDB> orgUnits) {
        super(context, orgUnits);
    }

    public OrgUnitArrayAdapter(Context context, Integer layout, List<OrgUnitDB> orgUnits) {
        super(context, layout, orgUnits);
    }

    public OrgUnitArrayAdapter(Context context, Integer layout, Integer dropdownLayout, List<OrgUnitDB> orgUnits) {
        super(context, layout, dropdownLayout, orgUnits);
    }

    @Override
    public void drawText(CustomTextView customTextView, OrgUnitDB orgUnit) {
        customTextView.setPadding(customTextView.getPaddingLeft(),getContext().getResources().getDimensionPixelSize(R.dimen.filters_top_bottom_padding),customTextView.getPaddingRight(),getContext().getResources().getDimensionPixelSize(R.dimen.filters_top_bottom_padding));
        customTextView.setText(orgUnit.getName());
    }
}
