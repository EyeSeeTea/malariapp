/*
	Usage:
		buildTableFacilities(
			{
				title:"Quality of care - Last 12 months"
				months:['jan.','feb.','mar.','apr.','may.','jun.','jul.','aug.','sep','oct.','nov.','dec.']
				facilities:[
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
var inputDataFacilities=[];
//Save the table data
function buildTableFacilities(tabGroupId,dataFacilities){
	inputDataFacilities.push(dataFacilities);
}
//Build the correct table
function rebuildTableFacilities(selectedUid){
	for(var i=0;i<inputDataFacilities.length;i++){
		if(inputDataFacilities[i].tableuid==selectedUid){
		    var id=inputDataFacilities[i].id;
			var facilitiesHeadId="facilitiesHead";
			var facilitiesBodyId="facilitiesBody";
			var titleFacilitiesId="titleFacilities";
			//Clear table
			document.getElementById(facilitiesHeadId).innerHTML='';
			document.getElementById(facilitiesBodyId).innerHTML='';

			//Title to table
			updateChartTitle(titleFacilitiesId,messages["qualityOfCare"]+inputDataFacilities[i].months.length+messages["months"]);

			//Add header
			buildTableHeader(id,inputDataFacilities[i].months);

			//Add body
			buildTableBody(id,inputDataFacilities[i].facilities);

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

function buildTableBody(tabGroupId,facilities){
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
	row=row+"<td  colspan="+facility.values.length+" style='background:#3e3e3f; color:white;' >"+facility.name+"</td></tr><tr>";
	//value x month
	for(var i=0;i<facility.values.length;i++){
		var facilityMonth=facility.values[i];
		var average=0;
		var asterisk = "";
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
                asterisk = "*";
            }
		}

        row=row+""+buildColorXScore(average,facilityMonth)+""+buildCellXScore(average)+"</span></div>"+asterisk+"</td>";
	}
	//end row
	row=row+"</tr>";
	return row;
}

function buildColorXScore(value, listOfSurveys){
	if(value==null){
		return "<td class='novisible' ><div class='circlerow' ><span class='centerspan'>";
	}
	if(value<50){
	    if(listOfSurveys.length>1){
		    return "<td class='redcircle'   onclick=\"androidPassUids(\'" +getListOfUids(listOfSurveys)+ "\')\"><div class='circlerow' style='background-color:"+red+"'><span class='centerspan'>";
		}else{
		    return "<td class='redcircle'   onclick=\"androidMoveToFeedback(\'" +listOfSurveys[0].id+ "\')\"><div class='circlerow' style='background-color:"+red+"'><span class='centerspan'>";
		}
	}

	if(value<80){
	    if(listOfSurveys.length>1){
		    return "<td class='ambercircle'  onclick=\"androidPassUids(\'" +getListOfUids(listOfSurveys)+ "\')\"><div class='circlerow' style='background-color:"+yellow+"'><span class='centerspan'>";
		}else{
		    return "<td class='ambercircle'  onclick=\"androidMoveToFeedback(\'" +listOfSurveys[0].id+ "\')\"><div class='circlerow' style='background-color:"+yellow+"'><span class='centerspan'>";
		}
	}

	if(listOfSurveys.length>1){
	    return "<td class='greencircle'  onclick=\"androidPassUids(\'" +getListOfUids(listOfSurveys)+ "\')\" ><div class='circlerow' style='background-color:"+green+"'><span class='centerspan'>";
	}else{
		return "<td class='greencircle'  onclick=\"androidMoveToFeedback(\'" +listOfSurveys[0].id+ "\')\" ><div class='circlerow' style='background-color:"+green+"'><span class='centerspan'>";
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