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
};


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
    for(var dataPie of dataPies){
        //Create template with right ids
        var customTemplate=defaultTemplate.replace(/###/g, dataPie.idProgram);
        //Add DOM element
        document.body.insertAdjacentHTML("beforeend",customTemplate);
        //Draw chart on it
        pieXProgramChart(dataPie);
    }

}




