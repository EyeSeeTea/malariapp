/*
	Usage:
		buildTablesPerProgram(
			{
				title:"Quality of care - Last 12 months"
				months:['jan.','feb.','mar.','apr.','may.','jun.','jul.','aug.','sep','oct.','nov.','dec.']
				tables:[
					{
						name:' Sample facility 1',
						values:[
							null,
							null,
							[{"uid":31,"competency":3},{"uid":30,"competency":3}],
							null,
							[{"uid":1,"competency":2},{"uid":12,"competency":100.0},{"uid":21,"competency":0}],
							null,
							null,
							null,
							null,
							null,
							null,
							[{"uid":113,"competency":1}]
						]
					},
					...
					{name:},
				]
			}
		);
*/
var inputDataTablesPerProgram=[];
var inputDataTablesPerOrgUnit=[];

const competencyScoreClassification = {
    NOT_AVAILABLE: 0,
    COMPETENT: 1,
    COMPETENT_NEEDS_IMPROVEMENT: 2,
    NOT_COMPETENT: 3
}

//Save the table data
function buildTablesPerProgram(tabGroupId,dataFacilities){
	inputDataTablesPerProgram.push(dataFacilities);
}
//Save the table data
function buildTablesPerOrgUnit(tabGroupId,dataFacilities){
	inputDataTablesPerOrgUnit.push(dataFacilities);
}

//Build the correct table
function rebuildTableFacilities(selectedUid, group){
	if(group==undefined){
		return;
	}
	for(var i=0;i<group.length;i++){
		if(group[i].tableuid==selectedUid){
		    var id=group[i].id;
			var facilitiesHeadId="facilitiesHead";
			var facilitiesBodyId="facilitiesBody";
			var titleFacilitiesId="titleFacilities";
			//Clear table
			document.getElementById(facilitiesHeadId).innerHTML='';
			document.getElementById(facilitiesBodyId).innerHTML='';

			//Title to table
			updateChartTitle(titleFacilitiesId,messages["qualityOfCare"]+group[i].months.length+messages["months"]);

			//Add header
			buildTableHeader(id,group[i].months);

			//Add body
			buildTableBody(id, group[i].tables);

		}
	}
}


function buildTableHeader(tabGroupId,months){
	var facilitiesHeadId="facilitiesHead";
	var rowsHeader="<tr>";
	for(var i=0;i<months.length;i++){
		rowsHeader=rowsHeader+"<th>"+months[i]+"</th>";
	}
	rowsHeader=rowsHeader+"</tr>";
	//Add tr to thead
	document.getElementById(facilitiesHeadId).insertAdjacentHTML("beforeend",rowsHeader);
}

function buildTableBody(tabGroupId, facilities){
	var facilitiesBodyId="facilitiesBody";
	for(var i=0;i<facilities.length;i++){
		var rowFacility=buildRowFacility(facilities[i]);
		document.getElementById(facilitiesBodyId).insertAdjacentHTML("beforeend",rowFacility);
	}
}

function buildRowFacility(facility){
	//start row
	var row="<tr>";
	//name
	if(facility != undefined){
	    row=row+"<td  colspan="+facility.values.length+" style='background:#3e3e3f; color:white;"
	     +" padding:8px 16px 8px 16px;' >"+facility.name+"</td></tr><tr>";
	    //value x month
	    for(var i=0;i<facility.values.length;i++){
	    	var facilityMonth=facility.values[i];
	    	var competency=0;
	    	var asterisk = "";
	    	if(facilityMonth==null){
	    		competency=null;
	    	}else{
	    	    competency = facilityMonth[0].competency;

                if(facilityMonth.length>1){
                    showMultipleEventLegend();
                    asterisk = "*";
                }
	    	}

            row=row+""+buildColorXScore(competency,facilityMonth)+""+buildCellXScore(competency)+"</span></div>"+asterisk+"</td>";
	    }
	}
	//end row
	row=row+"</tr>";
	return row;
}

function buildColorXScore(value, listOfSurveys){
    if(value==null){
        return "<td class='novisible' ><div class='circlerow' ><span class='centerspan'>";
    }
    if(value == competencyScoreClassification.COMPETENT){
        if(listOfSurveys.length>1){
            return "<td class='competent-circle' onclick=\"androidPassUids(\'" +getListOfUids(listOfSurveys)+ "\')\" ><div class='circlerow' style='background-color:"+competentColor+"'><span class='centerspan'>";
        }else{
            return "<td class='competent-circle' onclick=\"androidMoveToFeedback(\'" +listOfSurveys[0].id+ "\')\" ><div class='circlerow' style='background-color:"+competentColor+"'><span class='centerspan'>";
        }
    } else if(value == competencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT){
        if(listOfSurveys.length>1){
            return "<td class='competent_improvement-circle' onclick=\"androidPassUids(\'" +getListOfUids(listOfSurveys)+ "\')\"><div class='circlerow' style='background-color:"+competentImprovementColor+"'><span class='centerspan'>";
        }else{
            return "<td class='competent_improvement-circle' onclick=\"androidMoveToFeedback(\'" +listOfSurveys[0].id+ "\')\"><div class='circlerow' style='background-color:"+competentImprovementColor+"'><span class='centerspan'>";
        }
    } else if(value == competencyScoreClassification.NOT_COMPETENT){
        if(listOfSurveys.length>1){
            return "<td class='not-competent-circle' onclick=\"androidPassUids(\'" +getListOfUids(listOfSurveys)+ "\')\"><div class='circlerow' style='background-color:"+notCompetentColor+"'><span class='centerspan'>";
        }else{
            return "<td class='not-competent-circle' onclick=\"androidMoveToFeedback(\'" +listOfSurveys[0].id+ "\')\"><div class='circlerow' style='background-color:"+notCompetentColor+";border: 1px solid "+notCompetentColor+";'><span class='centerspan'>";
        }
    } else {
        //NOT_AVAILABLE
        if(listOfSurveys.length>1){
            return "<td class='not_available-circle' onclick=\"androidPassUids(\'" +getListOfUids(listOfSurveys)+ "\')\"><div class='circlerow' style='background-color:"+notAvailableColor+"'><span class='centerspan'>";
        }else{
            return "<td class='not_available-circle' onclick=\"androidMoveToFeedback(\'" +listOfSurveys[0].id+ "\')\"><div class='circlerow' style='background-color:"+notAvailableColor+"'><span class='centerspan'>";
        }
    }
}

function buildCellXScore(competency){
	if(competency==null){
		return '';
	} else if(competency == competencyScoreClassification.COMPETENT){
        return competentAbbreviationText;
    } else if(competency == competencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT){
        return competentImprovementAbbreviationText;
    } else if(competency == competencyScoreClassification.NOT_COMPETENT){
        return notCompetentAbbreviationText;
    } else {
        return notAvailableAbbreviationText;
    }

	return value;
}

function getListOfUids(listOfSurveys){
	var uidList = '';
	if(listOfSurveys!=null){
		for(var i=0;i<listOfSurveys.length;i++){
			uidList += listOfSurveys[i].id+";";
		}
		uidList = uidList.substring(0,uidList.lastIndexOf(";"));
	}
	return uidList;
}

function androidPassUids(value){
    showLog(value);
    Android.passUidList(value);
}

function androidMoveToFeedback(value){
    showLog(value);
    Android.moveToFeedback(value);
}

function showLog(value){
    console.log(value);
    Android.clickLog();
}