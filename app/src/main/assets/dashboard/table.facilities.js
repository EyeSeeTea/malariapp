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

function buildTableFacilities(tabGroupId,dataFacilities){
	var facilitiesHeadId="facilitiesHead"+tabGroupId;
	var facilitiesBodyId="facilitiesBody"+tabGroupId;
	var titleFacilitiesId="titleFacilities"+tabGroupId;
	//Clear table
	document.getElementById(facilitiesHeadId).innerHTML='';
	document.getElementById(facilitiesBodyId).innerHTML='';

	//Title to table
	updateChartTitle(titleFacilitiesId,dataFacilities.title);

	//Add header
	buildTableHeader(tabGroupId,dataFacilities.months);

	//Add body
	buildTableBody(tabGroupId,dataFacilities.facilities);
}

function buildTableHeader(tabGroupId,months){
	var facilitiesHeadId="facilitiesHead"+tabGroupId;
	var rowsHeader="<tr><th></th>";
	for(var i=0;i<months.length;i++){
		rowsHeader=rowsHeader+"<th>"+months[i]+"</th>";
	}
	rowsHeader=rowsHeader+"</tr>";

	//Add tr to thead
	document.getElementById(facilitiesHeadId).insertAdjacentHTML("beforeend",rowsHeader);
}

function buildTableBody(tabGroupId,facilities){
	var facilitiesBodyId="facilitiesBody"+tabGroupId;
	for(var i=0;i<facilities.length;i++){
		var rowFacility=buildRowFacility(facilities[i]);
		document.getElementById(facilitiesBodyId).insertAdjacentHTML("beforeend",rowFacility);
	}
}

function buildRowFacility(facility){
	//start row
	var row="<tr>";
	//name
	row=row+"<td>"+facility.name+"</td>";
	//value x month
	for(var i=0;i<facility.values.length;i++){
		var iValue=facility.values[i];
		row=row+"<td "+buildColorXScore(iValue)+">"+buildCellXScore(iValue)+"</td>";
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
		return "class='red'";
	}

	if(value<80){
		return "class='amber'";
	}

	return "class='green'";
}

function buildCellXScore(value){
	if(value==null){
		return '';
	}
	return value;
}