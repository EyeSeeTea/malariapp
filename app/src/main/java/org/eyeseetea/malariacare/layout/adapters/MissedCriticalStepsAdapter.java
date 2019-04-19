package org.eyeseetea.malariacare.layout.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.presentation.viewmodels.Observations.CompositeScoreViewModel;
import org.eyeseetea.malariacare.presentation.viewmodels.Observations.MissedCriticalStepViewModel;

import java.util.ArrayList;
import java.util.List;

public class MissedCriticalStepsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MissedCriticalStepViewModel> missedCriticalSteps = new ArrayList<>();

    public void setMissedCriticalSteps(List<MissedCriticalStepViewModel> missedCriticalSteps) {
        this.missedCriticalSteps.clear();
        this.missedCriticalSteps.addAll(missedCriticalSteps);

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
        MissedCriticalStepViewModel missedCriticalStep = missedCriticalSteps.get(position);

        ((ViewHolder) viewHolder).bindView(missedCriticalStep);
    }

    @Override
    public int getItemCount() {
        return missedCriticalSteps.size();
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

        void bindView(MissedCriticalStepViewModel missedCriticalStep) {
            missedCriticalStepTextView.setText(missedCriticalStep.getLabel());

            if (missedCriticalStep.isCompositeScore()) {
                missedCriticalStepImageView.setVisibility(View.VISIBLE);

                String code = ((CompositeScoreViewModel) missedCriticalStep).getCode();

                //Count number of '.' in string
                int numDots = code.length() - code.replace(".", "").length();

                if (numDots == 0) {
                    missedCriticalStepContainer.setBackgroundResource(R.color.feedbackDarkBlue);
                } else if (numDots == 1) {
                    missedCriticalStepContainer.setBackgroundResource(R.color.feedbackLightBlue);
                } else {
                    missedCriticalStepContainer.setBackgroundResource(R.color.scoreGrandson);
                }

            } else {
                missedCriticalStepImageView.setVisibility(View.GONE);

                missedCriticalStepContainer.setBackgroundResource(android.R.color.white);
            }
        }
    }

}
