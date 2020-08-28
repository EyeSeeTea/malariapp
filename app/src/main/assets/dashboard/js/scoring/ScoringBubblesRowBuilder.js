class ScoringBubblesRowBuilder {
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
                var average = 0;
                var asterisk = "";
                if (facilityMonth == null) {
                    var average = null;
                } else {
                    for (var d = 0; d < facilityMonth.length; d++) {
                        average += facilityMonth[d].score;
                    }
                    average = average / facilityMonth.length;
                    average = Math.round(average);
                    if (facilityMonth.length > 1) {
                        showMultipleEventLegend();
                        asterisk = "*";
                    }
                }

                row = row + "" + this.buildColorXScore(average, facilityMonth) + "" + this.buildCellXScore(average) + "</span></div>" + asterisk + "</td>";
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
        if (value < classificationContext.scores.low) {
            if (listOfSurveys.length > 1) {
                return "<td class='redcircle'   onclick=\"androidPassUids(\'" + getListOfUids(listOfSurveys) + "\')\"><div class='circlerow' style='background-color:" + classificationContext.colors.c + "'><span class='centerspan'>";
            } else {
                return "<td class='redcircle'   onclick=\"androidMoveToFeedback(\'" + listOfSurveys[0].id + "\')\"><div class='circlerow' style='background-color:" + classificationContext.colors.c + "'><span class='centerspan'>";
            }
        } else if (value < classificationContext.scores.medium) {
            if (listOfSurveys.length > 1) {
                return "<td class='ambercircle'  onclick=\"androidPassUids(\'" + getListOfUids(listOfSurveys) + "\')\"><div class='circlerow' style='background-color:" + classificationContext.colors.b + "'><span class='centerspan'>";
            } else {
                return "<td class='ambercircle'  onclick=\"androidMoveToFeedback(\'" + listOfSurveys[0].id + "\')\"><div class='circlerow' style='background-color:" + classificationContext.colors.b + "'><span class='centerspan'>";
            }
        } else {
            if (listOfSurveys.length > 1) {
                return "<td class='greencircle'  onclick=\"androidPassUids(\'" + getListOfUids(listOfSurveys) + "\')\" ><div class='circlerow' style='background-color:" + classificationContext.colors.a + "'><span class='centerspan'>";
            } else {
                return "<td class='greencircle'  onclick=\"androidMoveToFeedback(\'" + listOfSurveys[0].id + "\')\" ><div class='circlerow' style='background-color:" + classificationContext.colors.a + "'><span class='centerspan'>";
            }
        }
    }

    buildCellXScore(value) {
        if (value == null) {
            return '';
        }
        return value;
    }
}
