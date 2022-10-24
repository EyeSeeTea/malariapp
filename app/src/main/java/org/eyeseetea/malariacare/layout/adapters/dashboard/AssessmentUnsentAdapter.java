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

package org.eyeseetea.malariacare.layout.adapters.dashboard;

import static org.eyeseetea.malariacare.DashboardActivity.dashboardActivity;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.repositories.SurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.domain.usecase.GetSurveyAnsweredRatioUseCase;
import org.eyeseetea.malariacare.domain.usecase.ISurveyAnsweredRatioCallback;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.presentation.viewmodels.SurveyViewModel;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.DoublePieChart;

import java.util.ArrayList;
import java.util.List;

public class AssessmentUnsentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<SurveyViewModel> surveys = new ArrayList<>();

    public void setSurveys(List<SurveyViewModel> surveys){
        this.surveys = surveys;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.assessment_unsent_record, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ((ViewHolder) viewHolder).bindView(position);
    }

    @Override
    public int getItemCount() {
        return surveys.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        public static final String COMPLETED_SURVEY_MARK = "* ";

        private SurveyViewModel survey;
        private ImageView menuDots;
        private CustomTextView facilityName;
        private CustomTextView surveyType;
        final CustomTextView overall;
        final CustomTextView mandatory;

        public ViewHolder(View itemView) {
            super(itemView);

            menuDots = itemView.findViewById(R.id.menu_dots);
            facilityName = itemView.findViewById(R.id.facility);
            surveyType = itemView.findViewById(R.id.survey_type);


            menuDots.setOnClickListener(view -> {
                //TODO: Avoid to use model db here
                SurveyDB surveyDB =  SurveyDB.getSurveyByUId(survey.getSurveyUid());
                dashboardActivity.onAssessSelected(surveyDB);
            });

            overall = itemView.findViewById(R.id.label_overall);

            mandatory = itemView.findViewById(R.id.label_mandatory_completed);
        }

        void bindView(int position) {
            survey = surveys.get(position);

            adjustRowPadding();

            renderAnsweredRatio(survey);
            renderFacility(facilityName, surveyType, survey);
            renderSurveyType(surveyType, survey);

            decorateBackground(position);
        }

        private void renderAnsweredRatio(final SurveyViewModel survey) {
            ISurveyAnsweredRatioRepository surveyAnsweredRatioRepository =
                    new SurveyAnsweredRatioRepository();
            IAsyncExecutor asyncExecutor = new AsyncExecutor();
            IMainExecutor mainExecutor = new UIThreadExecutor();
            GetSurveyAnsweredRatioUseCase getSurveyAnsweredRatioUseCase =
                    new GetSurveyAnsweredRatioUseCase(surveyAnsweredRatioRepository, mainExecutor,
                            asyncExecutor);

            getSurveyAnsweredRatioUseCase.execute(survey.getSurveyUid(),
                    new ISurveyAnsweredRatioCallback() {
                        @Override
                        public void nextProgressMessage() {
                            Log.d(getClass().getName(), "nextProgressMessage");
                        }

                        @Override
                        public void onComplete(SurveyAnsweredRatio surveyAnsweredRatio) {
                            Log.d(getClass().getName(), "onComplete");

                            if (surveyAnsweredRatio != null) {
                                int mandatoryStatus = surveyAnsweredRatio.getMandatoryStatus();
                                int totalStatus = surveyAnsweredRatio.getTotalStatus();

                                setPercentage(mandatory, mandatoryStatus,
                                        DoublePieChart.getMandatoryColorByPercentage(mandatoryStatus,
                                                mandatory.getContext()));
                                setPercentage(overall, totalStatus,
                                        DoublePieChart.getOverAllColorByPercentage(totalStatus,
                                                overall.getContext()));
                            }
                        }
                    });
        }

        private void renderFacility(CustomTextView facilityName, CustomTextView surveyType,
                SurveyViewModel survey) {
            facilityName.setText(survey.getOrgUnit());
            facilityName.setLayoutParams(
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 0.5f));
            surveyType.setLayoutParams(
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 0.5f));
        }

        private CustomTextView renderSurveyType(CustomTextView surveyType, SurveyViewModel survey) {
            String surveyDescription;

            surveyDescription = survey.getProgram();

            surveyType.setText(surveyDescription);
            return surveyType;
        }

        private void decorateBackground(int position) {
            if (position == 0 || position % 2 == 0) {
                itemView.setBackgroundColor(
                        itemView.getContext().getResources().getColor(R.color.white));
            } else {
                itemView.setBackgroundColor(
                        itemView.getContext().getResources().getColor(R.color.white_grey));
            }
        }

        private void setPercentage(CustomTextView textView, int percentage, int color) {
            Context context = textView.getContext();
            textView.setText(context.getString(R.string.template_percentage_number, percentage));
            textView.setTextColor(color);
        }

       protected void adjustRowPadding() {
            float density = itemView.getContext().getResources().getDisplayMetrics().density;
            int paddingDp = (int) (5 * density);

            itemView.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
        }
    }
}