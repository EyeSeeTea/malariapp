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

function pieXTabGroupChart(data){

    var canvasDOMId="tabgroupCanvas"+data.idTabGroup;
    var legendDOMId="tabgroupLegend"+data.idTabGroup;
    var titleDOMId="tabgroupTitle"+data.idTabGroup;
    var titleTableDOMId="tabgroupTip"+data.idTabGroup;

    //Chart
    var ctx = document.getElementById(canvasDOMId).getContext("2d");
    var myChart = new Chart(ctx).Doughnut(
        [{
            value: data.valueA,
            color: "#81980d",
            label: "A (>80)"
        }, {
            value: data.valueB,
            color: "#00b4e3",
            label: "B (50-80)"
        }, {
            value: data.valueC,
            color: "#fa8900",
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
    updateChartTitle(titleDOMId,data.title);
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
	var selectHtml='<select onchange="changedOrgunit()" id="changeOrgUnit">';
	var selected="selected";
	for(i=0;i<Object.keys(inputOrgUnit).length;i++){
		if(inputOrgUnit[i].uidorgunit==selectedOrgUnit){
			selected="selected";
		}
		selectHtml+="<option "+selected+" value="+inputOrgUnit[i].uidorgunit+">"+inputOrgUnit[i].title+"</option>";
		if(selected==="selected"){			
			selected="";
		}
	}
	selectHtml+="</select>";
	document.getElementById('selectFacility').innerHTML = selectHtml;
	rebuildTableFacilities();
}

//event on click select/or to change the selected orgunit and reload.
function changedOrgunit(){
  var myselect = document.getElementById("changeOrgUnit");
  selectedOrgUnit=(myselect.options[myselect.selectedIndex].value);
  console.log("new"+selectedOrgUnit);
  renderPieCharts();
  rebuildTableFacilities();
}
//Save the data of the pies
function buildPieCharts(dataPies){
    //For each pie
	setFacilityData(dataPies);
	}

//Render the pie and create the select options
function renderPieCharts(){
    for(var i=0;i<inputOrgUnit.length;i++){
		if(selectedOrgUnit===undefined){
			console.log("undefined");
			showDataPie(inputOrgUnit[0]);
			selectedOrgUnit=inputOrgUnit[0].uidorgunit;
		}
		  if (inputOrgUnit[i].uidorgunit==selectedOrgUnit)
		{
			showDataPie(inputOrgUnit[i]);
		}
	}
	createSelectOrgUnit();
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


