package org.eyeseetea.malariacare.utils;

import android.content.Context;

import org.eyeseetea.malariacare.R;

import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;

public class CompetencyUtils {
    public static String convertCompetencyToText(CompetencyScoreClassification classification, Context context) {
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
}
