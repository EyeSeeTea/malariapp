package org.eyeseetea.malariacare.layout.adapters.dashboard;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.views.CustomTextView;

public class AMonitorAdapter extends  ABaseAdapter{

    public AMonitorAdapter(Context context) {
        super(context);
        recordLayout = R.layout.monitor_survey_row;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Get current survey
        SurveyDB survey = (SurveyDB) getItem(position);

        View rowView = this.lInflater.inflate(getRecordLayout(), parent, false);
        org.eyeseetea.sdk.presentation.views.CustomTextView
                id = (org.eyeseetea.sdk.presentation.views.CustomTextView) rowView.findViewById(R.id.idHeader);
        org.eyeseetea.sdk.presentation.views.CustomTextView
                score = (org.eyeseetea.sdk.presentation.views.CustomTextView) rowView.findViewById(R.id.scoreHeader);
        if(survey.getEventUid()!=null) {
            id.setText("Uid: " + survey.getEventUid());
        }else{
            id.setText("Local Id: " + survey.getId_survey());
        }
        score.setText("Score: "+survey.getMainScore());
        return rowView;
    }
}
