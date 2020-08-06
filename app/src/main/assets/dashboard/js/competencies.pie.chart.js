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
var competentColor;
var competentImprovementColor;
var notCompetentColor;
var notAvailableColor;
var competentText;
var competentImprovementText;
var notCompetentText;
var notAvailableText;
var competentAbbreviationText;
var competentImprovementAbbreviationText;
var notCompetentAbbreviationText;
var notAvailableAbbreviationText;

// ----- functions invoked from Android --------

function setClassification(classification){
    competentText=classification["competentText"];
    competentImprovementText=classification["competentImprovementText"];
    notCompetentText=classification["notCompetentText"];
    notAvailableText=classification["notAvailableText"];
    competentAbbreviationText=classification["competentAbbreviationText"];
    competentImprovementAbbreviationText=classification["competentImprovementAbbreviationText"];
    notCompetentAbbreviationText=classification["notCompetentAbbreviationText"];
    notAvailableAbbreviationText=classification["notAvailableAbbreviationText"];
}
function setCompetentColor(color){
    competentColor=color["color"];
}

function setCompetentImprovementColor(color){
    competentImprovementColor=color["color"];
}

function setNotCompetentColor(color){
    notCompetentColor=color["color"];
}

function setNotAvailableColor(color){
    notAvailableColor=color["color"];
}

// ----------------------------------------------------------

//Insert the pie in the html
function showDataPie(dataPie){
    var defaultTemplate= document.getElementById('pieTemplate').innerHTML;
	document.getElementById("pieChartContent").innerHTML=defaultTemplate;
    pieXTabGroupChart(dataPie);

}
//Remove the pie from html
function removeDataPie(){
	document.getElementById("pieChartContent").innerHTML="";
}

function pieXTabGroupChart(data){

    var canvasDOMId="tabgroupCanvas";
    var legendDOMId="tabgroupLegend";
    var titleTableDOMId="tabgroupTip";

    //Chart
    var ctx = document.getElementById(canvasDOMId).getContext("2d");
    var  myChart  = new Chart(ctx).Pie(
                               [{
                                   value: data.valueA,
                                   color: competentColor,
                                   label: "A ("+competentText+")"
                               }, {
                                   value: data.valueB,
                                   color: competentImprovementColor,
                                   label: "B ("+competentImprovementText +")"
                               }, {
                                   value: data.valueC,
                                   color: notCompetentColor,
                                   label: "C ("+notCompetentText+")"
                               }, {
                                   value: data.valueNA,
                                   color: notAvailableColor,
                                   label: ""+notAvailableText+""
                               }],
                               {
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