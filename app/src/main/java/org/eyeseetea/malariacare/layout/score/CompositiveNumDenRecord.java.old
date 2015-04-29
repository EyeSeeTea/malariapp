package org.eyeseetea.malariacare.layout.score;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.CompositiveScore;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositiveNumDenRecord extends ANumDenRecord{

    public void updateCompositivesScore(CompositiveScore compositiveScore, View gridView){

            //Iterate to get the children
            List<Float> numDenTotal = readNumDen(compositiveScore, new ArrayList<Float>(Arrays.asList(0F, 0F)));
            float score = ScoreUtils.calculateScoreFromNumDen(numDenTotal);

            List<View> compositiveScoreRow = LayoutUtils.getChildrenByTag((ViewGroup) gridView.findViewById(R.id.compositivescoreTable), null, "CompositiveScore_" + compositiveScore.getId().toString());
            ((TextView) ((ViewGroup) ((ViewGroup) compositiveScoreRow.get(0)).getChildAt(2)).getChildAt(0)).setText(Utils.round(score));

            //Iterate to get the parents
            while (compositiveScore.hasParent()){
                compositiveScore = compositiveScore.getCompositive_score();

                numDenTotal = readNumDen(compositiveScore, numDenTotal);
                score = ScoreUtils.calculateScoreFromNumDen(numDenTotal);

                compositiveScoreRow = LayoutUtils.getChildrenByTag((ViewGroup) gridView.findViewById(R.id.compositivescoreTable), null, "CompositiveScore_" + compositiveScore.getId().toString());
                ((TextView) ((ViewGroup) ((ViewGroup) compositiveScoreRow.get(0)).getChildAt(2)).getChildAt(0)).setText(Utils.round(score));
            }
    }
    public List<Float> readNumDen(CompositiveScore compositiveScore, List<Float> numDenTotal){
        numDenTotal = this.calculateNumDenTotal(numDenTotal);

        if (compositiveScore.hasChildren()){
            for (CompositiveScore compositiveScoreChild: compositiveScore.getCompositiveScoreChildren()){
                numDenTotal = readNumDen(compositiveScoreChild, numDenTotal);
            }
        }

        return numDenTotal;
    }
}
