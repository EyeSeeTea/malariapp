/**
Copyright (c) 2013-2015 Nick Downie

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
**/

/*
    Usage:
   
    pieXTabGroupChart({
        title:'Sample tabgroup',
        tip:'Quality of care(based on last assessment)',
        idTabGroup: 1,
        valueA:10,
        valueB:20,
        valueC:0
    })

*/

var green;
var yellow;
var red;

function setGreen(color){
    green=color["color"];
    //console.log(green);
}

function setYellow(color){
    yellow=color["color"];
    //console.log(yellow);
}

function setRed(color){
    red=color["color"];
    //console.log(red);
}
function pieXTabGroupChart(data){

    var canvasDOMId="tabgroupCanvas"+data.idTabGroup;
    var legendDOMId="tabgroupLegend"+data.idTabGroup;
    var titleDOMId="tabgroupTitle"+data.idTabGroup;
    var titleTableDOMId="tabgroupTip"+data.idTabGroup;
    //console.log(green);
    //console.log(yellow);
    //console.log(red);

    //Chart
    var ctx = document.getElementById(canvasDOMId).getContext("2d");
    var  myChart  = new Chart(ctx).Doughnut(
                               [{
                                   value: data.valueA,
                                   color: green,
                                   label: "A (>80)"
                               }, {
                                   value: data.valueB,
                                   color: yellow,
                                   label: "B (50-80)"
                               }, {
                                   value: data.valueC,
                                   color: red,
                                   label: "C (<50)"
                               }],
                               {
                                   segmentShowStroke: false,
                                   animateRotate: false,
                                   animateScale: false,
                                   percentageInnerCutout: 50,
                                   tooltipTemplate: "<%= value %>",
                                   onAnimationComplete: function(){
                                       this.showTooltip(this.segments, true);
                                   },
                                   tooltipEvents: [],
                                   showTooltips: true
                               }
                           );
    //Legend
    document.getElementById(legendDOMId).insertAdjacentHTML("beforeend",myChart.generateLegend());

    //Update title && tip
    updateChartTitle(titleTableDOMId,data.tip);
}

/*
    Use:

      buildPieCharts([
        {
            title:'Sample tabgroup',
            tip:'Quality of care(based on last assessment)',
            idTabGroup: 1,
            valueA:14,
            valueB:8,
            valueC:10
        },
        {
            title:'Sample tabgroup',
            tip:'Quality of care(based on last assessment)',
            idTabGroup: 2,
            valueA:24,
            valueB:12,
            valueC:10
        }        
        ]);    
*/
var selectedOrgUnit;
var inputOrgUnit;
//Show orgUnit, it is called from changedProgram.
function showPie(){
	changedOrgunit();
}
//Save the facility data in inputOrgUnit
function setFacilityData(data){
    inputOrgUnit=data;
}
//Create the select options for select diferentes org unit
function createSelectOrgUnit(){
	rebuildTableFacilities();
}

//event on click select/or to change the selected orgunit and reload.
function changedOrgunit(){
	selectedOrgUnit="";
	for(var i=0;i<Object.keys(inputOrgUnit).length;i++){
		if(inputOrgUnit[i].uidprogram==selectedProgram){
			selectedOrgUnit=inputOrgUnit[i].uidorgunit;
		}
	}
    if(selectedProgram==="AllAssessment"){
        createSelectOrgUnit();
    }else{
        renderPieCharts();
    }
}
//Save the data of the pies
function buildPieCharts(dataPies){
    //For each pie
	setFacilityData(dataPies);
}

//Render the pie and create the select options
function renderPieCharts(){
    //If nothing to show, let's clean up the canvas
    if(selectedOrgUnit==""){
        removeDataPie();
        createSelectOrgUnit();
    }
    else{
        for(var i=0;i<inputOrgUnit.length;i++){
            if (inputOrgUnit[i].uidorgunit==selectedOrgUnit){
                showDataPie(inputOrgUnit[i]);
                createSelectOrgUnit();
            }
        }
        createSelectProgram();
    }
}

//Insert the pie in the html
function showDataPie(dataPie){
    var defaultTemplate= document.getElementById('pieTemplate').innerHTML;
	document.getElementById("pieChartContent").innerHTML=defaultTemplate;
    //Create template with right ids
    var customTemplate=defaultTemplate.replace(/###/g, dataPie.idTabGroup);
    //Add DOM element
    document.getElementById("pieChartContent").innerHTML=customTemplate;
    //Draw chart on it
    pieXTabGroupChart(dataPie);

}
//Remove the pie from html
function removeDataPie(){
	document.getElementById("pieChartContent").innerHTML="";
}

