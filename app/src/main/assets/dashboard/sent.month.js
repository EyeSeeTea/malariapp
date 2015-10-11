/* Updates the text of the given html element*/
function updateChartTitle(id,text){
    document.getElementById(id).innerHTML=text;
}

var surveyXMonthChart= (function SentXMonthChart(){
    /* Prepares 'sent surveys x month' chart*/
    var ctx = document.getElementById("surveyXMonthCanvas").getContext("2d");
    var chart = new Chart(ctx).Line({
        labels: [],
        datasets: [
            {
                label: "Sent surveys",
                fillColor: "rgba(241,194,50,0)",
                strokeColor: "rgba(241,194,50,1)",
                pointColor: "rgba(241,194,50,1)",
                pointStrokeColor: "#fff",
                pointHighlightFill: "#fff",
                pointHighlightStroke: "rgba(241,194,50,1)",
                data: []
            },
            {
                label: "Expected surveys",
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
    });

    return chart;
})();

/* Use: 

	Updates title of a chart 
		javascript:updateChartTitle('titleLineSpan','%s')
	Adds data to line chart
		javascript:surveyXMonthChart.addData([%d, %d], '%s')
*/
