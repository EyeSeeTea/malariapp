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

package org.eyeseetea.malariacare.layout.adapters.dashboard;

import android.content.Context;
import android.view.LayoutInflater;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;

import java.util.List;

public class AssessmentSentAdapter extends AAssessmentAdapter implements IAssessmentAdapter {

    public AssessmentSentAdapter(List<Survey> items, Context context) {
        this.items = items;
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
        this.headerLayout = R.layout.assessment_sent_header;
        this.recordLayout = R.layout.assessment_sent_record;
        this.footerLayout = R.layout.assessment_sent_footer;
        if(PreferencesState.getInstance().isVerticalDashboard())
            this.title = context.getString(R.string.assessment_sent_title_header);
    }

    @Override
    public IDashboardAdapter newInstance(List items, Context context) {
        return new AssessmentSentAdapter((List<Survey>) items, context);
    }
}