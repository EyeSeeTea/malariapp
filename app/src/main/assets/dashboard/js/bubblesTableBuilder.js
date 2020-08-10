/*
	Usage:
		setDataTablesPerProgram(
			{
				title:"Quality of care - Last 12 months"
				months:['jan.','feb.','mar.','apr.','may.','jun.','jul.','aug.','sep','oct.','nov.','dec.']
				tables:[
					{
						name:' Sample facility 1',
						values:[
							null,
							null,
							[{"uid":31,"competency|score":3},{"uid":30,"competency|score":3}],
							null,
							[{"uid":1,"competency|score":2},{"uid":12,"competency|score":100.0},{"uid":21,"competency|score":0}],
							null,
							null,
							null,
							null,
							null,
							null,
							[{"uid":113,"competency|score":1}]
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
function setDataTablesPerProgram(tabGroupId,dataFacilities){
	inputDataTablesPerProgram.push(dataFacilities);
}
//Save the table data
function setDataTablesPerOrgUnit(tabGroupId,dataFacilities){
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
	const facilitiesBodyId="facilitiesBody";
	const rowFacilityBuilder = createRowFacilityBuilder();

	for(var i=0;i<facilities.length;i++){
		var rowFacility=rowFacilityBuilder.buildRowFacility(facilities[i]);
		document.getElementById(facilitiesBodyId).insertAdjacentHTML("beforeend",rowFacility);
	}
}

function createRowFacilityBuilder(){
    if (serverClassification === 1){
        return new CompetenciesBubblesRowBuilder()
    } else {
        return new ScoringBubblesRowBuilder()
    }
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