package org.eyeseetea.malariacare.layout.score;

import android.view.View;

import org.eyeseetea.malariacare.database.model.CompositiveScore;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreRegister {

    private static final Map<CompositiveScore, CompositiveNumDenRecord> compositiveScoreRegister = new HashMap<CompositiveScore, CompositiveNumDenRecord>();
    private static final Map<Tab, GeneralNumDenRecord> generalScoreRegister = new HashMap<Tab, GeneralNumDenRecord>();

    public static void addRecord(Question question, Float num, Float den){
        if (question.getCompositiveScore() != null) {
            compositiveScoreRegister.get(question.getCompositiveScore()).addRecord(question, num, den);
        }
        generalScoreRegister.get(question.getHeader().getTab()).addRecord(question, num, den);
    }

    public static void deleteRecord(Question question){
        if (question.getCompositiveScore() != null) {
            compositiveScoreRegister.get(question.getCompositiveScore()).deleteRecord(question);
        }
        generalScoreRegister.get(question.getHeader().getTab()).deleteRecord(question);
    }

    public static List<Float> calculateGeneralScore(Tab tab) {
        return generalScoreRegister.get(tab).calculateTotal();
    }

    public static void updateCompositivesScore(CompositiveScore compositiveScore, View gridView){
        if (compositiveScore != null)compositiveScoreRegister.get(compositiveScore).updateCompositivesScore(compositiveScore, gridView);
    }

    public static void registerScore(CompositiveScore compositiveScore){
        compositiveScoreRegister.put(compositiveScore, new CompositiveNumDenRecord());
    }

    public static void registerScore(Tab tab){
        generalScoreRegister.put(tab, new GeneralNumDenRecord());
    }
}
