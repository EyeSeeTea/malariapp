/**
Copyright (c) 2013-2015 Nick Downie

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
**/

/* Updates the text of the given html element*/

    var input = [];
	var inputall = [];
	var programs = [];
	var allAssessment="AllAssessment";
    var selectedProgram=allAssessment;
	var chart=null;

function updateChartTitle(id,text){
    document.getElementById(id).innerHTML=text;
}

//Save the data of the stats
function setData(data){
	var temp=data.slice();
	setAllAssassement(temp)
	countPrograms(data[3]);
	input.push(data);
}

 function countPrograms(data){
 	console.log(data);
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
function setAllAssassement(data){
	var exist=false;
	for(var i=0;i<Object.keys(inputall).length;i++){
		if(inputall[i][4]==data[4]){
			exist=true;
			inputall[i][0]+=data[0]; 
			inputall[i][2]="All assetments";
			inputall[i][3]=allAssessment;
		}
	}
	if(exist==false)
		inputall.push(data);
}
//show the data in the table.
function showData(){
	for(var i=0;i<inputall.length;i++){
	//Show the table filter by the select program(all assessment) or without filter if only have one program
        if(inputall[i].indexOf(selectedProgram) > -1 || programs.length==1){
			surveyXMonthChart.addData([inputall[i][0], inputall[i][1]], inputall[i][4]);
		}
	}
}
//Create the select options for select the program
function createSelectProgram(){
	var selectHtml='<select onchange="changeProgram()" id="changeProgram" ">';
	var selected="";
	if(selectedProgram==="AllAssessment")
		selected="selected";
	selectHtml+="<option "+selected+" value="+allAssessment+">"+"ALL ASSESSMENTS"+"</option>";
	selected="selected";
	for(var i=0;i<input.length;i++){
		if(!(selectHtml.indexOf(input[i][3]) > -1) && !(input[i][3]=== undefined)){
		if(input[i][3]==selectedProgram){
			selected="selected";
		}
		else
			selected="";
		selectHtml+="<option "+selected+" value="+input[i][3]+">"+input[i][2].toUpperCase()+"</option>";
		if(selected==="selected"){
			selected="";
		}
		}
	}
	selectHtml+="</select>";
	document.getElementById('selectProgram').innerHTML = selectHtml;
}
//change program, change table, and change pie to load the pie from the new progra
function changeProgram(){
  var myselect = document.getElementById("changeProgram");
  selectedProgram=(myselect.options[myselect.selectedIndex].value);
	var hidden=false;
	if(selectedProgram==="AllAssessment"){
		hidden=true;
	}
	if(hidden==true){
		//Uncoment it for make the pie and chart program dependent.  
		document.getElementById("tableCanvas").classList.remove("hide");
		document.getElementById("tableCanvas").classList.add("show");
		document.getElementById("graphicCanvas").classList.remove("show");
		document.getElementById("graphicCanvas").classList.add("hide");
	}
	else{
		showPie();
		changedOrgunit();
		document.getElementById("tableCanvas").classList.remove("show");
		document.getElementById("tableCanvas").classList.add("hide");
		document.getElementById("graphicCanvas").classList.remove("hide");
		document.getElementById("graphicCanvas").classList.add("show");
	}
}

var surveyXMonthChart= (function SentXMonthChart(){
    /* Prepares 'sent surveys x month' chart*/
    var ctx = document.getElementById("surveyXMonthCanvas").getContext("2d");
    chart = new Chart(ctx).Line({
        labels: [],
		datasets: [
            {
                label: "Assessment undertaken",
                fillColor: "rgba(132,180,103,0)",
                strokeColor: "#81980d",
                pointColor: "#81980d",
                pointStrokeColor: "#fff",
                pointHighlightFill: "#fff",
                pointHighlightStroke: "rgba(241,194,50,1)",
                data: []
            },
            {
                label: "Target",
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
})();

/* Use:

	Updates title of a chart
		javascript:updateChartTitle('titleLineSpan','%s')
	Adds data to line chart
		javascript:surveyXMonthChart.addData([%d, %d], '%s')
*/