package org.eyeseetea.malariacare.layout.adapters.monitor;

import android.content.res.Resources;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.ScoreType;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.utils.CompetencyUtils;
import org.eyeseetea.malariacare.utils.DateParser;

import java.util.ArrayList;
import java.util.List;

public class SurveysMonitorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ServerClassification serverClassification;

    public SurveysMonitorAdapter(ServerClassification serverClassification) {
        this.serverClassification = serverClassification;
    }

    public interface OnSurveyClickListener {
        void onSurveyClick(SurveyDB survey);
    }

    private List<SurveyDB> surveys = new ArrayList<>();
    private OnSurveyClickListener onSurveyClickListener;

    public void setOnSurveyClickListener(OnSurveyClickListener listener){
        onSurveyClickListener = listener;
    }

    public void setSurveys(List<SurveyDB> newSurveys) {
        surveys.clear();
        this.surveys.addAll(newSurveys);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_survey_monitor, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        SurveyDB survey = surveys.get(position);

        ((ViewHolder) viewHolder).bindView(survey, serverClassification);

        viewHolder.itemView.setOnClickListener(view -> {
            if (onSurveyClickListener != null){
                onSurveyClickListener.onSurveyClick(survey);
            }
        });
    }

    @Override
    public int getItemCount() {
        return surveys.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView completionDateView;
        TextView competencyView;
        TextView scoreView;

        public ViewHolder(View itemView) {
            super(itemView);

            completionDateView = itemView.findViewById(R.id.survey_completion_date_item_view);
            competencyView = itemView.findViewById(R.id.survey_competency_item_view);
            scoreView = itemView.findViewById(R.id.survey_score_item_view);
        }

        void bindView(SurveyDB survey,
                ServerClassification serverClassification) {

            DateParser dateParser = new DateParser();
            completionDateView.setText(
                    dateParser.getEuropeanFormattedDate(survey.getCompletionDate()));
            scoreView.setText(Math.round(survey.getMainScoreValue()) + "");
            Resources resources = itemView.getContext().getResources();

            ScoreType scoreType = new ScoreType(survey.getMainScoreValue());
            if (scoreType.isTypeA()) {
                scoreView.setBackgroundColor(resources.getColor(R.color.high_score_color));
            } else if (scoreType.isTypeB()) {
                scoreView.setBackgroundColor(resources.getColor(R.color.medium_score_color));
            } else if (scoreType.isTypeC()) {
                scoreView.setBackgroundColor(resources.getColor(R.color.low_score_color));
            }

            if (serverClassification == ServerClassification.COMPETENCIES){
                competencyView.setVisibility(View.VISIBLE);
                CompetencyScoreClassification classification =
                        CompetencyScoreClassification.get(
                                survey.getCompetencyScoreClassification());

                CompetencyUtils.setBackgroundByCompetency(competencyView, classification);
                CompetencyUtils.setTextByCompetencyAbbreviation(competencyView, classification);
                CompetencyUtils.setTextColorByCompetency(competencyView, classification);
            } else {
                competencyView.setVisibility(View.GONE);
            }
        }
    }

}
