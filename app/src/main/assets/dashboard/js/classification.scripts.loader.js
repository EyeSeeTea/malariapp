var scriptsByClassificationLoaded = false;

function init (){
    // Request to Android function server classification to current connected server
    const serverClassification = Android.getServerClassification();
    console.log('serverClassification:' + serverClassification);

    // Loaded script files to chart and table according to server classification
    if (serverClassification === 1){
        console.log('loading competencies scripts');
        includeJsFile('js/pie.competencies.tabgroup.js');
        includeJsFile('js/table.competencies.facilities.js');
    } else {
        console.log('loading scoring scripts');
        includeJsFile('js/pie.scoring.tabgroup.js');
        includeJsFile('js/table.scoring.facilities.js');
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