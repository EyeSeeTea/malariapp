
function updateChartTitle(id,text){
    document.getElementById(id).innerHTML=text;
}

function hideMultipleEventLegend(){
    document.getElementById('multipleEventLegend').style.display = 'none';
}
function showMultipleEventLegend(){
    document.getElementById('multipleEventLegend').style.display = 'block';
}

function hideElement(idElement){
    var element = document.getElementById(idElement);
    if(!element){
        return;
    }
    element.classList.remove("show");
    element.classList.add("hide");
}

function showElement(idElement){
    var element = document.getElementById(idElement);
    if(!element){
        return;
    }
    element.classList.remove("hide");
    element.classList.add("show");
}