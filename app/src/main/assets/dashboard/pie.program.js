/**
Copyright (c) 2013-2015 Nick Downie

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
**/

/*
    Usage:
   
    pieXProgramChart({
        title:'Quality of care: Sample program',
        idProgram: 1,
        valueA:10,
        valueB:20,
        valueC:0
    })

*/

function pieXProgramChart(data){

    var canvasDOMId="programCanvas"+data.idProgram;
    var legendDOMId="programLegend"+data.idProgram;
    var titleDOMId="programTitle"+data.idProgram;

    //Chart
    var ctx = document.getElementById(canvasDOMId).getContext("2d");
    var myChart = new Chart(ctx).Doughnut(
        [{
            value: data.valueA,
            color: "#84b467",
            label: "A (>80)"
        }, {
            value: data.valueB,
            color: "#f1c232",
            label: "B (50-80)"
        }, {
            value: data.valueC,
            color: "#ff060d",
            label: "C (<50)"
        }],
        {
            segmentShowStroke: false,
            animateRotate: true,
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
    document.getElementById(legendDOMId).innerHTML = myChart.generateLegend();    

    //Update title
    updateChartTitle(titleDOMId,data.title);
}


/*
    Use:

      buildPieCharts([
        {
            title:'Quality of care: First Program',
            idProgram: 1,
            valueA:14,
            valueB:8,
            valueC:10
        },
        {
            title:'Quality of care: Second Program',
            idProgram: 2,
            valueA:24,
            valueB:12,
            valueC:10
        }        
        ]);    
*/

function buildPieCharts(dataPies){
    var defaultTemplate= '<div> <span class="line-title" id="programTitle###"></span> <div> <canvas id="programCanvas###" style="width: 100%; height: auto;"></canvas> </div><div id="programLegend###" class="chart-legend"></div></div>';

    //For each pie

    for(var i=0;i<dataPies.length;i++){
        var dataPie = dataPies[i];
        //Create template with right ids
        var customTemplate=defaultTemplate.replace(/###/g, dataPie.idProgram);
        //Add DOM element
        document.getElementById("hrSent").insertAdjacentHTML("afterend",customTemplate);
        //Draw chart on it
        pieXProgramChart(dataPie);
    }

}




