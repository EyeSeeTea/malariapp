package org.eyeseetea.malariacare.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import org.eyeseetea.malariacare.R;

import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;

public class CompetencyUtils {
    public static String getTextByCompetency(CompetencyScoreClassification classification, Context context) {
        String competencyText = "";

        if (classification == CompetencyScoreClassification.NOT_AVAILABLE) {
            competencyText = context.getString(
                    R.string.competency_classification_not_available);
        } else if (classification == CompetencyScoreClassification.COMPETENT) {
            competencyText = context.getString(R.string.competency_classification_competent);
        } else if (classification == CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT) {
            competencyText = context.getString(
                    R.string.competency_classification_competent_improvement);
        } else if (classification == CompetencyScoreClassification.NOT_COMPETENT) {
            competencyText = context.getString(
                    R.string.competency_classification_not_competent);
        }
        return competencyText;
    }

    public static int getBackgroundByCompetency(CompetencyScoreClassification classification, Context context) {
        if (classification == CompetencyScoreClassification.COMPETENT) {
            return ContextCompat.getColor(context,R.color.competency_competent_background_color);
        } else if (classification == CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT) {
            return ContextCompat.getColor(context,R.color.competency_competent_improvement_background_color);
        } else if (classification == CompetencyScoreClassification.NOT_COMPETENT) {
            return ContextCompat.getColor(context,R.color.competency_not_competent_background_color);
        } else  {
            return ContextCompat.getColor(context,R.color.competency_not_available_background_color);
        }
    }

    public static int getTextColorByCompetency(CompetencyScoreClassification classification, Context context) {
        if (classification == CompetencyScoreClassification.COMPETENT) {
            return ContextCompat.getColor(context,R.color.competency_competent_text_color);
        } else if (classification == CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT) {
            return ContextCompat.getColor(context,R.color.competency_competent_improvement_text_color);
        } else if (classification == CompetencyScoreClassification.NOT_COMPETENT) {
            return ContextCompat.getColor(context,R.color.competency_not_competent_text_color);
        } else  {
            return ContextCompat.getColor(context,R.color.competency_not_available_text_color);
        }
    }
}
