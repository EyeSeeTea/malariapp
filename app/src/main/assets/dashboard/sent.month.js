/**
Copyright (c) 2013-2015 Nick Downie
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
**/

/* Updates the text of the given html element*/

    var inputPrograms = [];
    var inputOrgUnits = [];
	var allDataByProgram = [];
	var allDataByOrgUnit = [];
	var programs = [];
	var orgunits = [];
	var allAssessmentTitle="All Assessment";
	var allOrgUnitTitle="All Org Unit";
	//allAssessment\allOrgUnits are keys to identify the all program. It doesn't work with spaces ( "All asssemsnet")
	var allAssessmentKey="AllAssessment";
	var allOrgUnitKey="AllOrgUnits";
    var selectedProgram=allAssessmentKey;
    var selectedOrgUnit=allOrgUnitKey;
	var chart=null;

function updateChartTitle(id,text){
    document.getElementById(id).innerHTML=text;
}

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
//show the data in the table.
function showMainTable(){
	if(inputOrgUnits.length<1){
	//orgunit filter not active
		var d = document.getElementById("spinnerOrgUnit");
		d.className = "hidden";

		var d = document.getElementById("spinnerProgram");
		d.className = "horizontal oneFilter";
	}
	else if(inputPrograms.length<1){
	//program filter not active
		var d = document.getElementById("spinnerProgram");
		d.className = "hidden";

		var d = document.getElementById("spinnerOrgUnit");
		d.className = "horizontal oneFilter";
	}
	if(inputPrograms.length>1){
	//Show main table by program
		for(var i=0;i<allDataByProgram.length;i++){
		//Show the table filter by the select program(all assessment) or without filter if only have one program
			if(allDataByProgram[i].indexOf(selectedProgram) > -1 || programs.length==1){
				surveyXMonthChart.addData([allDataByProgram[i][0], allDataByProgram[i][1]], allDataByProgram[i][4]);
			}
		}
	}
	else if(inputOrgUnits.length>1){
	//Show main table by orgunit
		for(var i=0;i<allDataByOrgUnit.length;i++){
		//Show the table filter by the select program(all assessment) or without filter if only have one program
			if(allDataByOrgUnit[i].indexOf(selectedOrgUnit) > -1 || orgunits.length==1){
				surveyXMonthChart.addData([allDataByOrgUnit[i][0], allDataByOrgUnit[i][1]], allDataByOrgUnit[i][4]);
			}
		}
	}
	else{
	    console.log("Not have surveys");
	}
}

//change program and refresh table and graphics (or refresh principal table with all the stats)
function changeProgram(){
    var myselect = document.getElementById("changeProgram");
    selectedProgram=(myselect.options[myselect.selectedIndex].value);
	if(selectedProgram===allAssessmentKey){
		resetOrgUnitSpinner();
        showElement("tableCanvas");
        hideElement("graphicCanvas");
        hideElement("noSurveysText");
	}else{
	    resetOrgUnitSpinner();
		showProgram();
		showElement("graphicCanvas");
		hideElement("tableCanvas");
		showElement("noSurveysText")
	}
}

function resetOrgUnitSpinner(){
		document.getElementById("changeOrgUnit").selectedIndex = allOrgUnitKey;
		selectedOrgUnit=allOrgUnitKey;
}
function resetProgramSpinner(){
		document.getElementById("changeProgram").selectedIndex = allAssessmentKey;
		selectedProgram=allAssessmentKey;
}
//change orgUnit and refresh table and graphics (or refresh principal table with all the stats)
function changeOrgUnit(){
    var myselect = document.getElementById("changeOrgUnit");
    selectedOrgUnit=(myselect.options[myselect.selectedIndex].value);
	if(selectedOrgUnit===allOrgUnitKey){
	    resetProgramSpinner();
        showElement("tableCanvas");
        hideElement("graphicCanvas");
	}else{
	    resetProgramSpinner();
		showOrgUnit();
		showElement("graphicCanvas");
		hideElement("tableCanvas");
	}
}
//Create the select options for select the program
function createSpinnerProgram(){
	var selectHtml='<select onchange="changeProgram()" id="changeProgram">';
	var selected="";
	if(selectedProgram===allAssessmentKey){
	    selected="selected";
	}
	selectHtml+="<option "+selected+" value="+allAssessmentKey+">"+allAssessmentTitle+"</option>";
	selected="selected";
	for(var i=0;i<inputPrograms.length;i++){
		if(!(selectHtml.indexOf(inputPrograms[i][3]) > -1) && !(inputPrograms[i][3]=== undefined)){
            selected = inputPrograms[i][3]==selectedProgram?"selected":"";
            selectHtml+="<option "+selected+" value="+inputPrograms[i][3]+">"+inputPrograms[i][2].toUpperCase()+"</option>";
		}
	}
	selectHtml+="</select>";
	document.getElementById('spinnerProgram').innerHTML = selectHtml;
}

//Create the select options for select the program
function createSpinnerOrgUnit(){
	var selectHtml='<select onchange="changeOrgUnit()" id="changeOrgUnit">';
	var selected="";
	if(selectedProgram===allOrgUnitKey){
	    selected="selected";
	}
	selectHtml+="<option "+selected+" value="+allOrgUnitKey+">"+allOrgUnitTitle+"</option>";
	selected="selected";
	for(var i=0;i<inputOrgUnits.length;i++){
		if(!(selectHtml.indexOf(inputOrgUnits[i][3]) > -1) && !(inputOrgUnits[i][3]=== undefined)){
            selected = inputOrgUnits[i][3]==selectedOrgUnit?"selected":"";
            selectHtml+="<option "+selected+" value="+inputOrgUnits[i][3]+">"+inputOrgUnits[i][2].toUpperCase()+"</option>";
		}
	}
	selectHtml+="</select>";
	document.getElementById('spinnerOrgUnit').innerHTML = selectHtml;
}

function hideElement(idElement){
    var element = document.getElementById(idElement);
    if(!element){
        return;
    }
    element.classList.remove("show");
    element.classList.add("hide");
}

function showElement(idElement){
    var element = document.getElementById(idElement);
    if(!element){
        return;
    }
    element.classList.remove("hide");
    element.classList.add("show");
}

function SentXMonthChart(){
    /* Prepares 'sent surveys x month' chart*/
    var ctx = document.getElementById("surveyXMonthCanvas").getContext("2d");
    chart = new Chart(ctx).Line({
        labels: [],
		datasets: [
            {
                label: messages["assesmentUnderTaken"],
                fillColor: "rgba(132,180,103,0)",
                strokeColor: "#81980d",
                pointColor: "#81980d",
                pointStrokeColor: "#fff",
                pointHighlightFill: "#fff",
                pointHighlightStroke: "rgba(241,194,50,1)",
                data: []
            },
            {
                label: messages["target"],
                fillColor: "rgba(132,180,103,0)",
                strokeColor: "#00b4e3",
                pointColor: "#00b4e3",
                pointStrokeColor: "#fff",
                pointHighlightFill: "#fff",
                pointHighlightStroke: "rgba(151,187,205,1)",
                data: []
            }
        ]
    },{
        scaleBeginAtZero : true,
        scaleFontSize: 18,
        scaleFontFamily: "'Roboto-Condensed'",
        scaleFontStyle: "bold",
        scaleFontColor: "#000",
    });

    //Adds legend to chart
    document.getElementById('sentLegend').innerHTML = chart.generateLegend();
    return chart;
};

/* Use:
	Updates title of a chart
		javascript:updateChartTitle('titleLineSpan','%s')
	Adds data to line chart
		javascript:surveyXMonthChart.addData([%d, %d], '%s')
*/