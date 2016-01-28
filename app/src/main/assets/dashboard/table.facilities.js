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
							67,
							null,
							83,
							96,
							91,
							20,
							null,
							null,
							15,
							25
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
	console.log(tabGroupId);
	inputDataFacilities.push(dataFacilities);
}
//Build the correct table
function rebuildTableFacilities(){
	for(i=0;i<inputDataFacilities.length;i++){
		if(inputDataFacilities[i].tableuid==selectedOrgUnit){
		var id=inputDataFacilities[i].id;
			var facilitiesHeadId="facilitiesHead";
			var facilitiesBodyId="facilitiesBody";
			var titleFacilitiesId="titleFacilities";
			//Clear table
			document.getElementById(facilitiesHeadId).innerHTML='';
			document.getElementById(facilitiesBodyId).innerHTML='';

			//Title to table
			updateChartTitle(titleFacilitiesId,"Quality of care: Last "+inputDataFacilities[i].months.length+" months");

			//Add header
			buildTableHeader(id,inputDataFacilities[i].months);

			//Add body
			buildTableBody(id,inputDataFacilities[i].facilities);

		}
	//}
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
		var iValue=facility.values[i];
row=row+"<td "+buildColorXScore(iValue)+"><div class='circlerow'><span class='centerspan'>"+buildCellXScore(iValue)+"</span></div></td>";
	
	}
	//end row
	row=row+"</tr>";
	return row;
}

function buildColorXScore(value){
	if(value==null){
		return "";
	}

	if(value<50){
		return "class='redcircle'";
	}

	if(value<80){
		return "class='ambercircle'";
	}

	return "class='greencircle'";
}

function buildCellXScore(value){
	if(value==null){
		return '';
	}
	return value;
}