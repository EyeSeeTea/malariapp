/**
Copyright (c) 2013-2015 Nick Downie

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
**/

/* Updates the text of the given html element*/

    var input = [];
    var selectedProgram="";
function updateChartTitle(id,text){
    document.getElementById(id).innerHTML=text;
}
function setData(data){
    input.push(data);
}
function showData(){
  removeData( );
	for(i=0;i<input.length;i++){
		if(selectedProgram=="")
			selectedProgram=input[i][3];
        if(input[i].indexOf(selectedProgram) > -1){
            surveyXMonthChart.addData([input[i][0], input[i][1]], input[i][4]);
        }
	}
	createSelectProgram();
}
function createSelectProgram(){
	var selectHtml='<select onchange="changeProgram()" id="changeProgram">';
	var selected="selected";
	for(i=0;i<input.length;i++){
		if(!(selectHtml.indexOf(input[i][3]) > -1) && !(input[i][3]=== undefined)){
		if(input[i][3]==selectedProgram){
			selected="selected";
		}

		selectHtml+="<option "+selected+" value="+input[i][3]+">"+input[i][2]+"</option>";
		if(selected==="selected"){
			selected="";
		}
		}
	}
	selectHtml+="</select>";
	document.getElementById('selectProgram').innerHTML = selectHtml;

}

function changeProgram(){
  var myselect = document.getElementById("changeProgram");
  selectedProgram=(myselect.options[myselect.selectedIndex].value);

  showData();
}
var chart=null;
function removeData()
{chart.update();

  var oldChart = document.getElementById("surveyXMonthCanvas");
  var newChart = document.createElement("canvas");
  newChart.setAttribute("id","surveyXMonthCanvas");
  newChart.setAttribute("style","width: 100%; height: auto;");
  oldChart.parentNode.replaceChild(newChart, oldChart);

surveyXMonthChart= (function SentXMonthChart(){
    /* Prepares 'sent surveys x month' chart*/
    var ctx = document.getElementById("surveyXMonthCanvas").getContext("2d");
    chart = new Chart(ctx).Line({
        labels: [],
        datasets: [
            {
                label: "Assessment undertaken",
                fillColor: "rgba(241,194,50,0)",
                strokeColor: "rgba(241,194,50,1)",
                pointColor: "rgba(241,194,50,1)",
                pointStrokeColor: "#fff",
                pointHighlightFill: "#fff",
                pointHighlightStroke: "rgba(241,194,50,1)",
                data: []
            },
            {
                label: "Target",
                fillColor: "rgba(132,180,103,0)",
                strokeColor: "rgba(132,180,103,1)",
                pointColor: "rgba(132,180,103,1)",
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

}

var surveyXMonthChart= (function SentXMonthChart(){
    /* Prepares 'sent surveys x month' chart*/
    var ctx = document.getElementById("surveyXMonthCanvas").getContext("2d");
    chart = new Chart(ctx).Line({
        labels: [],
        datasets: [
            {
                label: "Assessment undertaken",
                fillColor: "rgba(241,194,50,0)",
                strokeColor: "rgba(241,194,50,1)",
                pointColor: "rgba(241,194,50,1)",
                pointStrokeColor: "#fff",
                pointHighlightFill: "#fff",
                pointHighlightStroke: "rgba(241,194,50,1)",
                data: []
            },
            {
                label: "Target",
                fillColor: "rgba(132,180,103,0)",
                strokeColor: "rgba(132,180,103,1)",
                pointColor: "rgba(132,180,103,1)",
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
