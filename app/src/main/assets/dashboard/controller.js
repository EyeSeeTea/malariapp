var inputPrograms = [];
var inputOrgUnits = [];
var allDataByProgram = [];
var allDataByOrgUnit = [];
var programs = [];
var orgunits = [];
var selectedProgram=allAssessmentKey;
var selectedOrgUnit=allOrgUnitKey;
var chart=null;
var selectedPie;
var piesDataByProgram;
var piesDataByOrgUnit;
var allAssessmentTitle="All Assessment";
var allOrgUnitTitle="All Org Unit";
var allAssessmentKey="AllAssessment";
var allOrgUnitKey="AllOrgUnits";
	
//Save all the program stats data
function setProgramData(data){
	var temp=data.slice();
	setAllAssessment(temp)
	countPrograms(data[3]);
	inputPrograms.push(data);
}

//Save all the org unit stats data
function setOrgUnitData(data){
	var temp=data.slice();
	setAllOrgUnit(temp)
	countOrgUnits(data[3]);
	inputOrgUnits.push(data);
}

function countOrgUnits(data){
 	var exist=false;
 	for(var i=0;i<orgunits.length;i++){
 		if(orgunits[i]==data){
 			exist=true;
 		}
 	}
 	if(!exist)
 	{
 		orgunits.push(data);
 	}
}
function countPrograms(data){
 	var exist=false;
 	for(var i=0;i<programs.length;i++){
 		if(programs[i]==data){
 			exist=true;
 		}
 	}
 	if(!exist)
 	{
		programs.push(data);
	}
}
//Save and merge a All assassement object with the stats of all the programs merged
function setAllAssessment(data){
	var exist=false;
	for(var i=0;i<Object.keys(allDataByProgram).length;i++){
		if(allDataByProgram[i][4]==data[4]){
			exist=true;
			allDataByProgram[i][0]+=data[0];
			allDataByProgram[i][2]=allAssessmentTitle;
			allDataByProgram[i][3]=allAssessmentKey;
		}
	}
	if(exist==false){
	    allDataByProgram.push(data);
	}
}
//Save and merge a All OrgUnit object with the stats of all the programs merged
function setAllOrgUnit(data){
	var exist=false;
	for(var i=0;i<Object.keys(allDataByOrgUnit).length;i++){
		if(allDataByOrgUnit[i][4]==data[4]){
			exist=true;
			allDataByOrgUnit[i][0]+=data[0];
			allDataByOrgUnit[i][2]=allOrgUnitTitle;
			allDataByOrgUnit[i][3]=allOrgUnitKey;
		}
	}
	if(exist==false){
	    allDataByOrgUnit.push(data);
	}
}

//change program and refresh table and graphics (or refresh principal table with all the stats)
function changeProgram(){
	hideMultipleEventLegend();
	if(selectedProgram===allAssessmentKey){
        showElement("tableCanvas");
        hideElement("graphicCanvas");
        hideElement("noSurveysText");
        renderPieChartsByProgram();
	}else{
		showProgram();
		hideElement("tableCanvas");
		showElement("graphicCanvas");
		showElement("noSurveysText")
	}
}

function resetOrgUnitSpinner(){ 
		selectedOrgUnit=allOrgUnitKey;
}
function resetProgramSpinner(){ 
		selectedProgram=allAssessmentKey;
}
//change orgUnit and refresh table and graphics (or refresh principal table with all the stats)
function changeOrgUnit(){
    hideMultipleEventLegend();
	if(selectedOrgUnit===allOrgUnitKey){
        renderPieChartsByOrgUnit();
        showElement("tableCanvas");
        hideElement("graphicCanvas");
	}else{
		showOrgUnit();
		showElement("graphicCanvas");
		hideElement("tableCanvas");
	}
} 

//event on click select/or in program "spinner" to change the selected program and reload.
function changePieAndTablesByProgram(){
	selectedPie="";
	findProgram=false;
	for(var i=0;i<Object.keys(piesDataByProgram).length;i++){
		if(piesDataByProgram[i].uidprogram==selectedProgram){
			selectedPie=piesDataByProgram[i].uidprogram;
			findProgram=true;
			break;
		}
	}

	noSurveysId="noSurveysText";
    	 if(!findProgram){
                updateChartTitle(noSurveysId,messages["noSurveys"]);
            }else{
                updateChartTitle(noSurveysId,"");
            }


    if(selectedProgram===allAssessmentKey){
        rebuildTableFacilities(selectedProgram, inputDataTablesPerProgram);
    }else{
        renderPieChartsByProgram();
    }
}

//event on click select/or in program "spinner" to change the selected program and reload.
function changePieAndTablesByOrgUnit(){
	selectedPie="";
	for(var i=0;i<Object.keys(piesDataByOrgUnit).length;i++){
		if(piesDataByOrgUnit[i].uidorgunit==selectedOrgUnit){
			selectedPie=piesDataByOrgUnit[i].uidorgunit;
			break;
		}
	}
    if(selectedOrgUnit===allOrgUnitKey){
        rebuildTableFacilities(selectedOrgUnit, inputDataTablesPerOrgUnit)
    }
	else{
        renderPieChartsByOrgUnit();
	}
}
//Show tables/Pies by program.
function showProgram(){
    removeDataPie();
	changePieAndTablesByProgram();
}

//Show tables/Pies by org unit.
function showOrgUnit(){
    removeDataPie();
	changePieAndTablesByOrgUnit();
}

//Save the pie data by uid(program or org unit)
function setProgramPieData(data){
    piesDataByProgram=data;
}
//Save the pie data by uid(program or org unit)
function setOrgUnitPieData(data){
    piesDataByOrgUnit=data;
}

//Render the pie and table by program filter and reload the spinners
function renderPieChartsByProgram(){
	var programOrgUnit="";
    if(selectedProgram!=""){
        for(var i=0;i<piesDataByProgram.length;i++){
            if (piesDataByProgram[i].uidprogram==selectedProgram){
				programOrgUnit=piesDataByProgram[i].uidprogram;
                showDataPie(piesDataByProgram[i]);
				break;
            }
        }
        rebuildTableFacilities(programOrgUnit, inputDataTablesPerOrgUnit);
    } 
} 

//Render the pie and table by orgUnit filter and reload the spinners
function renderPieChartsByOrgUnit(){
	var orgUnitPrograms;
    if(selectedOrgUnit!=""){
        for(var i=0;i<piesDataByOrgUnit.length;i++){
            if (piesDataByOrgUnit[i].uidorgunit==selectedOrgUnit){
				orgUnitPrograms=piesDataByOrgUnit[i].uidorgunit;
                showDataPie(piesDataByOrgUnit[i]);
				break;
            }
        }
        rebuildTableFacilities(orgUnitPrograms, inputDataTablesPerProgram);
    }   
}

function updateOrgUnitFilter(uid){
    if(uid=="null"){
        uid=allOrgUnitKey;
    }
    selectedOrgUnit = uid;
	changeOrgUnit();
}

function updateProgramFilter(uid){
    if(uid=="null"){
        uid=allAssessmentKey;
    }
    selectedProgram=uid;
	changeProgram();
}