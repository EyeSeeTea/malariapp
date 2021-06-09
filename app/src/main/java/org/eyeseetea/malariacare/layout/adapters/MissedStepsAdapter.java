package org.eyeseetea.malariacare.layout.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.presentation.viewmodels.observations.CompositeScoreViewModel;
import org.eyeseetea.malariacare.presentation.viewmodels.observations.MissedStepViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MissedStepsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MissedStepViewModel> missedSteps = new ArrayList<>();
    private Map<String, MissedStepViewModel> missedStepsMap = new HashMap<>();

    public void setMissedSteps(List<MissedStepViewModel> missedSteps) {
        this.missedSteps.clear();
        this.missedSteps.addAll(missedSteps);

        missedStepsMap.clear();

        for (MissedStepViewModel step: missedSteps) {
            if (!missedStepsMap.containsKey(step.getKey())){
                missedStepsMap.put(step.getKey(), step);
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_missed_critical_step, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        MissedStepViewModel missedCriticalStep = missedSteps.get(position);

        ((ViewHolder) viewHolder).bindView(missedCriticalStep);

        if (missedCriticalStep.isCompositeScore()) {
            CompositeScoreViewModel compositeScore = (CompositeScoreViewModel) missedCriticalStep;

            viewHolder.itemView.setOnClickListener(view -> {
                expandOrCollapse(compositeScore);
            });
        }
    }

    private void expandOrCollapse(CompositeScoreViewModel compositeScore) {
        int startIndex = missedSteps.indexOf(compositeScore);

        compositeScore.setExpanded(!compositeScore.isExpanded());

        for (int i = startIndex + 1; i < missedSteps.size(); i++) {
            MissedStepViewModel currentMissedCriticalStep = missedSteps.get(i);

            CompositeScoreViewModel parentMissedCriticalStep = (CompositeScoreViewModel)
                    missedStepsMap.get(currentMissedCriticalStep.getParentKey());

            if (parentMissedCriticalStep != null && parentMissedCriticalStep.isVisible()
                    && parentMissedCriticalStep.isExpanded()){
                currentMissedCriticalStep.setVisible(true);
            } else {
                currentMissedCriticalStep.setVisible(false);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return missedSteps.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView missedCriticalStepTextView;
        ImageView missedCriticalStepImageView;
        LinearLayout missedCriticalStepContainer;

        public ViewHolder(View itemView) {
            super(itemView);

            missedCriticalStepTextView =
                    itemView.findViewById(R.id.missed_critical_step_text_view);
            missedCriticalStepImageView =
                    itemView.findViewById(R.id.missed_critical_step_image_view);
            missedCriticalStepContainer =
                    itemView.findViewById(R.id.missed_critical_step_container);
        }

        void bindView(MissedStepViewModel missedCriticalStep) {
            if (missedCriticalStep.isVisible()) {
                itemView.setVisibility(View.VISIBLE);
                itemView.setLayoutParams(
                        new RecyclerView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));


                missedCriticalStepTextView.setText(missedCriticalStep.getLabel());

                if (missedCriticalStep.isCompositeScore()) {
                    CompositeScoreViewModel compositeScore =
                            (CompositeScoreViewModel) missedCriticalStep;

                    missedCriticalStepImageView.setVisibility(View.VISIBLE);

                    if (compositeScore.isExpanded()) {
                        missedCriticalStepImageView.setRotation(180);
                    } else {
                        missedCriticalStepImageView.setRotation(0);
                    }

                    setCompositeScoreBackground(compositeScore);

                } else {
                    missedCriticalStepImageView.setVisibility(View.GONE);

                    missedCriticalStepContainer.setBackgroundResource(android.R.color.white);
                }
            } else {
                itemView.setVisibility(View.GONE);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            }
        }

        private void setCompositeScoreBackground(CompositeScoreViewModel compositeScore) {
            String code = compositeScore.getCode();

            //Count number of '.' in string
            int numDots = code.length() - code.replace(".", "").length();

            if (numDots == 0) {
                missedCriticalStepContainer.setBackgroundResource(R.color.feedbackDarkBlue);
            } else if (numDots == 1) {
                missedCriticalStepContainer.setBackgroundResource(R.color.feedbackLightBlue);
            } else {
                missedCriticalStepContainer.setBackgroundResource(R.color.scoreGrandson);
            }
        }
    }

}
