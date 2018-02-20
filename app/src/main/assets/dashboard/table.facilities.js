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
							[{"uid":31,"score":0.0},{"uid":30,"score":100.0}],
							null,
							[{"uid":1,"score":0.0},{"uid":12,"score":100.0},{"uid":21,"score":100.0}],
							null,
							null,
							null,
							null,
							null,
							null,
							[{"uid":113,"score":10.0}]
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
//Save the table data
function buildTablesPerProgram(tabGroupId,dataFacilities){
	inputDataTablesPerProgram.push(dataFacilities);
}
//Save the table data
function buildTablesPerOrgUnit(tabGroupId,dataFacilities){
	inputDataTablesPerOrgUnit.push(dataFacilities);
} 


//show the data in the table.
function showMainTableByProgram(){
	if(inputPrograms.length>1){
		surveyXMonthChart = SentXMonthChart();
		//Show main table by program
		for(var i=0;i<allDataByProgram.length;i++){
			surveyXMonthChart.addData([allDataByProgram[i][0], allDataByProgram[i][1]], allDataByProgram[i][4]); 
		}
	}
	else{
	    console.log("Not have surveys");
	}
}
	
//show the data in the table.
function showMainTableByOrgUnit(){
	if(inputOrgUnits.length>1){
		surveyXMonthChart = SentXMonthChart();
		//Show main table by orgunit
		for(var i=0;i<allDataByOrgUnit.length;i++){ 
			surveyXMonthChart.addData([allDataByOrgUnit[i][0], allDataByOrgUnit[i][1]], allDataByOrgUnit[i][4]); 
		}
	}
	else{
	    console.log("Not have surveys");
	}
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
	var row="<tr class='tr_header'>";
	//name
	row=row+"<td class='tr_title' colspan='"+facility.values.length+"  ' >"+facility.name+"</td></tr><tr class='tr_row'>";
	//value x month
	for(var i=0;i<facility.values.length;i++){
		var facilityMonth=facility.values[i];
		var average=0;
		if(facilityMonth==null){
			var average=null;
		}else{
			for(var d=0;d<facilityMonth.length;d++){
				average+= facilityMonth[d].score;
			}
			average=average/facilityMonth.length;
			average=Math.round(average);
            if(facilityMonth.length>1){
                showMultipleEventLegend();
            }
		}

        row=row+""+buildColorXScore(average,facilityMonth)+""+buildCellXScore(average)+"</span></div></td>";
	}
	//end row
	row=row+"</tr>";
	return row;
}

function buildColorXScore(value, listOfSurveys){
	if(value==null){
		return "<td class='novisible' ><div class='circleContainer' ><img src='img/scoreCircleGrey.svg'/><span class='centerspan'>";
	}
	if(value<50){
	    if(listOfSurveys.length>1){
		    return "<td class='redcircle'   onclick=\"androidPassUids(\'" +getListOfUids(listOfSurveys)+ "\')\"><div class='circleContainer'><img src='img/scoreCircleMultipleGreen.svg'/><span class='centerspanmultiple'>";
		}else{
		    return "<td class='redcircle'   onclick=\"androidMoveToFeedback(\'" +listOfSurveys[0].id+ "\')\"><div class='circleContainer'><img src='img/scoreCircleGreen.svg'/><span class='centerspan'>";
		}
	}

	if(value<80){
	    if(listOfSurveys.length>1){
		    return "<td class='ambercircle'  onclick=\"androidPassUids(\'" +getListOfUids(listOfSurveys)+ "\')\"><div class='circleContainer'><img src='img/scoreCircleMultipleYellow.svg'/><span class='centerspanmultiple'>";
		}else{
		    return "<td class='ambercircle'  onclick=\"androidMoveToFeedback(\'" +listOfSurveys[0].id+ "\')\"><div class='circleContainer'><img src='img/scoreCircleYellow.svg'/><span class='centerspan'>";
		}
	}

	if(listOfSurveys.length>1){
	    return "<td class='greencircle'  onclick=\"androidPassUids(\'" +getListOfUids(listOfSurveys)+ "\')\" ><div class='circleContainer'><img src='img/scoreCircleMultipleRed.svg'/><span class='centerspanmultiple'>";
	}else{
		return "<td class='greencircle'  onclick=\"androidMoveToFeedback(\'" +listOfSurveys[0].id+ "\')\" ><div class='circleContainer'><img src='img/scoreCircleRed.svg'/><span class='centerspan'>";
	}
}

function buildCellXScore(value){
	if(value==null){
		return '';
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