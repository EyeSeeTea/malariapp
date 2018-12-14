/*
 * Copyright (c) 2017.
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

package org.eyeseetea.malariacare.domain.entity;


import static org.eyeseetea.malariacare.domain.common.RequiredChecker.required;

public class ScoreType {

    public enum Classification {HIGH, MEDIUM, LOW, NO_SCORE}

    public String HighKey ="A";
    public String MediumKey ="B";
    public String LowKey ="C";
    public final static float HIGH_SCORE_HIGHER_THAN = 89f;
    public final static float MEDIUM_LOWER_THAN = 90f;
    public final static float LOW_LOWER_THAN = 80f;
    public final static float NO_SCORE = 0f;


    private float score;

    public ScoreType(Float score) {
        this.score = required(score, "Score is required");
    }

    public Classification getClassification() {
        if(score> HIGH_SCORE_HIGHER_THAN){
            return Classification.HIGH;
        }else if (score >= LOW_LOWER_THAN && score < MEDIUM_LOWER_THAN){
            return Classification.MEDIUM;
        } else if (score < LOW_LOWER_THAN && score > NO_SCORE) {
            return Classification.LOW;
        } else {
            return Classification.NO_SCORE;
        }
    }

    public static int getMonitoringMinimalHigh() {
        return (int)HIGH_SCORE_HIGHER_THAN;
    }

    public static int getMonitoringMaximumMedium() {
        return (int)MEDIUM_LOWER_THAN;
    }

    public static int getMonitoringMaximumLow() {
        return (int)LOW_LOWER_THAN;
    }

    public static String getMonitoringMediumPieFormat() {
        return (int)LOW_LOWER_THAN+"-"+(int)HIGH_SCORE_HIGHER_THAN;
    }

    /**
     * Returns this survey is type A (green)
     */
    public boolean isTypeA() {
        return getClassification() ==  Classification.HIGH;
    }

    /**
     * Returns this survey is type B (amber)
     */
    public boolean isTypeB() {
        return getClassification() ==  Classification.MEDIUM;
    }

    /**
     * Returns this survey is type C (red)
     */
    public boolean isTypeC() {
        return getClassification() == Classification.LOW
                || getClassification() == Classification.NO_SCORE;
    }


    /**
     * Returns the type as string(used in control dataElement)
     */
    public String getType() {
        String type = "";
        if (isTypeA()) {
            type = HighKey;
        } else if (isTypeB()) {
            type = MediumKey;
        } else if (isTypeC()) type = LowKey;
        return type;
    }
}
