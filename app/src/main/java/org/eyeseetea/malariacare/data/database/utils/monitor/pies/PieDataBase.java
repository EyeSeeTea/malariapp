/*
 * Copyright (c) 2016.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.data.database.utils.monitor.pies;

import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.ScoreType;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.utils.Constants;

public class PieDataBase {

    static final String JSONFORMAT =
            "{title:'%s',tip:'%s',idTabGroup: '%s',valueA:%d,valueB:%d,valueC:%d,valueNA:%d,"
                    + "uidprogram:'%s',"
                    + "uidorgunit:'%s'}";

    int numA;

    int numB;

    int numC;

    int numNA;


    public void incCounterByCompetency(int competencyScoreClassification) {
        CompetencyScoreClassification classification =
                CompetencyScoreClassification.get(competencyScoreClassification);

        if (classification == CompetencyScoreClassification.COMPETENT) {
            numA++;
        } else if (classification == CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT) {
            numB++;
        } else if (classification == CompetencyScoreClassification.NOT_COMPETENT) {
            numC++;
        } else {
            numNA++;
        }
    }

    public void incCounterByScoring(float score){
        ScoreType scoreType = new ScoreType(score);
        if(scoreType.getClassification() == ScoreType.Classification.LOW){
            numC++;
            return;
        }

        else if(scoreType.getClassification() == ScoreType.Classification.MEDIUM){
            numB++;
            return;
        }

        numA++;
        return;
    }
}