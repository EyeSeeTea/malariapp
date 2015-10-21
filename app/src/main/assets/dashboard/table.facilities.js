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

function buildTableFacilities(dataFacilities){
	//Clear table
	document.getElementById("facilitiesHead").innerHTML='';
	document.getElementById("facilitiesBody").innerHTML='';

	//Title to table
	updateChartTitle("titleFacilities",dataFacilities.title);

	//Add header
	buildTableHeader(dataFacilities.months);

	//Add body
	buildTableBody(dataFacilities.facilities);
}

function buildTableHeader(months){
	var rowsHeader="<tr><th></th>";
	for(var i=0;i<months.length;i++){
		rowsHeader=rowsHeader+"<th>"+months[i]+"</th>";
	}
	rowsHeader=rowsHeader+"</tr>";

	//Add tr to thead
	document.getElementById("facilitiesHead").insertAdjacentHTML("beforeend",rowsHeader);
}

function buildTableBody(facilities){
	for(var i=0;i<facilities.length;i++){
		var rowFacility=buildRowFacility(facilities[i]);
		document.getElementById("facilitiesBody").insertAdjacentHTML("beforeend",rowFacility);
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