const competencyScoreClassification = {
    NOT_AVAILABLE: 0,
    COMPETENT: 1,
    COMPETENT_NEEDS_IMPROVEMENT: 2,
    NOT_COMPETENT: 3
}

class CompetenciesBubblesRowBuilder {
    buildRowFacility(facility) {
        //start row
        var row = "<tr>";
        //name
        if (facility != undefined) {
            row = row + "<td  colspan=" + facility.values.length + " style='background:#3e3e3f; color:white;"
                + " padding:8px 16px 8px 16px;' >" + facility.name + "</td></tr><tr>";
            //value x month
            for (var i = 0; i < facility.values.length; i++) {
                var facilityMonth = facility.values[i];
                var competency = 0;
                var asterisk = "";
                if (facilityMonth == null) {
                    competency = null;
                } else {
                    competency = facilityMonth[0].competency;

                    if (facilityMonth.length > 1) {
                        showMultipleEventLegend();
                        asterisk = "*";
                    }
                }

                row = row + "" + this.buildColorXScore(competency, facilityMonth) + "" + this.buildCellXScore(competency) + "</span></div>" + asterisk + "</td>";
            }
        }
        //end row
        row = row + "</tr>";
        return row;
    }

    buildColorXScore(value, listOfSurveys) {
        if (value == null) {
            return "<td class='novisible' ><div class='circlerow' ><span class='centerspan'>";
        }
        if (value == competencyScoreClassification.COMPETENT) {
            if (listOfSurveys.length > 1) {
                return "<td class='competent-circle' onclick=\"androidPassUids(\'" + getListOfUids(listOfSurveys) + "\')\" ><div class='circlerow' style='background-color:" + classificationContext.colors.competentColor + "'><span class='centerspan'>";
            } else {
                return "<td class='competent-circle' onclick=\"androidMoveToFeedback(\'" + listOfSurveys[0].id + "\')\" ><div class='circlerow' style='background-color:" + classificationContext.colors.competentColor + "'><span class='centerspan'>";
            }
        } else if (value == competencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT) {
            if (listOfSurveys.length > 1) {
                return "<td class='competent_improvement-circle' onclick=\"androidPassUids(\'" + getListOfUids(listOfSurveys) + "\')\"><div class='circlerow' style='background-color:" + classificationContext.colors.competentImprovementColor + "'><span class='centerspan'>";
            } else {
                return "<td class='competent_improvement-circle' onclick=\"androidMoveToFeedback(\'" + listOfSurveys[0].id + "\')\"><div class='circlerow' style='background-color:" + classificationContext.colors.competentImprovementColor + "'><span class='centerspan'>";
            }
        } else if (value == competencyScoreClassification.NOT_COMPETENT) {
            if (listOfSurveys.length > 1) {
                return "<td class='not-competent-circle' onclick=\"androidPassUids(\'" + getListOfUids(listOfSurveys) + "\')\"><div class='circlerow' style='background-color:" + classificationContext.colors.notCompetentColor + "'><span class='centerspan'>";
            } else {
                return "<td class='not-competent-circle' onclick=\"androidMoveToFeedback(\'" + listOfSurveys[0].id + "\')\"><div class='circlerow' style='background-color:" + classificationContext.colors.notCompetentColor + ";border: 1px solid " + classificationContext.colors.notCompetentColor + ";'><span class='centerspan'>";
            }
        } else {
            //NOT_AVAILABLE
            if (listOfSurveys.length > 1) {
                return "<td class='not_available-circle' onclick=\"androidPassUids(\'" + getListOfUids(listOfSurveys) + "\')\"><div class='circlerow' style='background-color:" + classificationContext.colors.notAvailableColor + "'><span class='centerspan'>";
            } else {
                return "<td class='not_available-circle' onclick=\"androidMoveToFeedback(\'" + listOfSurveys[0].id + "\')\"><div class='circlerow' style='background-color:" + classificationContext.colors.notAvailableColor + "'><span class='centerspan'>";
            }
        }
    }

    buildCellXScore(competency) {
        if (competency == null) {
            return '';
        } else if (competency == competencyScoreClassification.COMPETENT) {
            return classificationContext.texts.competentAbbreviationText;
        } else if (competency == competencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT) {
            return classificationContext.texts.competentImprovementAbbreviationText;
        } else if (competency == competencyScoreClassification.NOT_COMPETENT) {
            return classificationContext.texts.notCompetentAbbreviationText;
        } else {
            return classificationContext.texts.notAvailableAbbreviationText;
        }

        return value;
    }
}