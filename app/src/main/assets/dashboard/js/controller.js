var selectedProgram="";
var selectedOrgUnit="";
var piesDataByProgram;
var piesDataByOrgUnit;
var classificationContext;
var serverClassification;

// ----- functions invoked from Android --------

function setServerClassification(classification){
    serverClassification = classification;
}

function setClassificationContext(context){
    classificationContext = context;
    console.log({classificationContext});
}

function updateOrgUnitFilter(uid){
    selectedOrgUnit = uid;
	changeOrgUnit();
}

function updateProgramFilter(uid){
    selectedProgram=uid;
	changeProgram();
}

//Save the pie data by uid(program or org unit)
function setProgramPieData(data){
    piesDataByProgram=data;
}
//Save the pie data by uid(program or org unit)
function setOrgUnitPieData(data){
    piesDataByOrgUnit=data;
}

//--------------------------------------------

function changeProgram(){
	hideMultipleEventLegend();
    showProgram();
}

function changeOrgUnit(){
    hideMultipleEventLegend();
    showOrgUnit();
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

//event on click select/or in program "spinner" to change the selected program and reload.
function changePieAndTablesByProgram(){
	findProgram=false;
	for(var i=0;i<Object.keys(piesDataByProgram).length;i++){
		if(piesDataByProgram[i].uidprogram==selectedProgram){
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

    renderPieChartsByProgram();
}

//event on click select/or in program "spinner" to change the selected program and reload.
function changePieAndTablesByOrgUnit(){
	findOrgUnit=false;
	for(var i=0;i<Object.keys(piesDataByOrgUnit).length;i++){
		if(piesDataByOrgUnit[i].uidorgunit==selectedOrgUnit){
			findOrgUnit=true;
			break;
		}
	}

	noSurveysId="noSurveysText";

    if(!findOrgUnit){
        updateChartTitle(noSurveysId,messages["noSurveys"]);
    }else{
        updateChartTitle(noSurveysId,"");
    }

    renderPieChartsByOrgUnit();
}

//Render the pie and table by program filter and reload the spinners
function renderPieChartsByProgram(){
	var programOrgUnit="";
    if(selectedProgram!=""){
        for(var i=0;i<piesDataByProgram.length;i++){
            if (piesDataByProgram[i].uidprogram==selectedProgram){
				programOrgUnit=piesDataByProgram[i].uidprogram;
                buildPieChart(piesDataByProgram[i]);
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
                buildPieChart(piesDataByOrgUnit[i]);
				break;
            }
        }
        rebuildTableFacilities(orgUnitPrograms, inputDataTablesPerProgram);
    }   
}

//Insert the pie in the html
function buildPieChart(dataPie){
    var defaultTemplate= document.getElementById('pieTemplate').innerHTML;
	document.getElementById("pieChartContent").innerHTML=defaultTemplate;

    if (serverClassification === 1){
        const pieChartBuilder = new CompetenciesPieChartBuilder();
        pieChartBuilder.build(dataPie)
    } else {
        const pieChartBuilder = new ScoringPieChartBuilder();
        pieChartBuilder.build(dataPie)
    }
}

//Remove the pie from html
function removeDataPie(){
	document.getElementById("pieChartContent").innerHTML="";
}

// ----- functions invoked to Android --------
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
//--------------------------------------------