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

package org.eyeseetea.malariacare.layout.score;

import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreRegister {

    private static final Map<CompositeScore, CompositeNumDenRecord> compositeScoreRegister = new HashMap<CompositeScore, CompositeNumDenRecord>();
    private static final Map<Tab, GeneralNumDenRecord> generalScoreRegister = new HashMap<Tab, GeneralNumDenRecord>();

    public static void addRecord(Question question, Float num, Float den){
        if (question.getCompositeScore() != null) {
            compositeScoreRegister.get(question.getCompositeScore()).addRecord(question, num, den);
        }
        generalScoreRegister.get(question.getHeader().getTab()).addRecord(question, num, den);
    }

    public static void deleteRecord(Question question){
        if (question.getCompositeScore() != null)
            compositeScoreRegister.get(question.getCompositeScore()).deleteRecord(question);
        generalScoreRegister.get(question.getHeader().getTab()).deleteRecord(question);
    }

    private static List<Float> getRecursiveScore(CompositeScore cScore, List<Float> result) {

        if (!cScore.hasChildren())
            return compositeScoreRegister.get(cScore).calculateNumDenTotal(result);
        else {
            for (CompositeScore cScoreChildren : cScore.getCompositeScoreChildren())
                result = getRecursiveScore(cScoreChildren, result);
            return result;
        }
    }

    public static List<Float> getNumDenum(Question question) {
        return generalScoreRegister.get(question.getHeader().getTab()).getNumDenRecord().get(question);
    }

    public static Float getCompositeScore(CompositeScore cScore) {

        List<Float>result = compositeScoreRegister.get(cScore).calculateNumDenTotal(new ArrayList<Float>(Arrays.asList(0F, 0F)));

        result = getRecursiveScore(cScore, result);

        return ScoreUtils.calculateScoreFromNumDen(result);
    }


    public static List<Float> calculateGeneralScore(Tab tab) {
        return generalScoreRegister.get(tab).calculateTotal();
    }

    public static void registerScore(CompositeScore compositeScore){
        compositeScoreRegister.put(compositeScore, new CompositeNumDenRecord());
    }

    public static void registerScore(Tab tab){
        generalScoreRegister.put(tab, new GeneralNumDenRecord());
    }
}
