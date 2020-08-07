var scriptsByClassificationLoaded = false;

function init (){
    // Request to Android function server classification to current connected server
    const serverClassification = Android.getServerClassification();

    // Loaded script files to chart and table according to server classification
    if (serverClassification === 1){
        includeJsFile('js/competencies.pie.chart.js');
        //includeJsFile('js/competencies.bubbles.table.js');
    } else {
        includeJsFile('js/scoring.pie.chart.js');
        //includeJsFile('js/scoring.bubbles.table.js');
    }
}

function includeJsFile(filename)
{
   var head = document.getElementsByTagName('head')[0];

   var script = document.createElement('script');
   script.src = filename;
   script.type = 'text/javascript';

   head.appendChild(script)
}

init();
