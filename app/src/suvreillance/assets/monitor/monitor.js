/*
 * Copyright (c) 2016.
 *
 * This file is part of QIS Surveillance App.
 *
 *  QIS Surveillance App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Surveillance App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Surveillance App.  If not, see <http://www.gnu.org/licenses/>.
 */
function Monitor() {
    //Map of i18n messages
    this.messages = {};

    //Map of tables
    this.tables = {};

    //Current TimeUnit
    this.timeUnit = "months";
}

//Update timeUnit dinamically
Monitor.prototype.updateTimeUnit = function(sel){
    //Update current timeUnit
    this.timeUnit = sel.value;
    //Reload tables
    this.drawTables();
};

//Updates messages map
Monitor.prototype.updateMessages = function(msg) {
    this.messages = msg;

    //Replace dom element values with the right ones
    for (var key in this.messages) {
        if (this.messages.hasOwnProperty(key)) {
            var element = document.getElementById(key);
            if (element) {
                element.innerHTML = this.messages[key];
            }
        }
    }
};

//Add new table
Monitor.prototype.addTable = function(title) {
    //Table has already been added
    if (this.tables[title]) {
        return;
    }

    //Adds new item to map
    this.tables[title] = {
        title: title,
        rows: []
    };
};

//Add new table
Monitor.prototype.addRow = function(title,row) {
    var table=this.tables[title];
    //Table has already been added
    if (!table) {
        return;
    }

    table.rows.push(row);
};

//Reloads tables from data according to current timeUnit
Monitor.prototype.drawTables = function(){
    document.getElementById("statsContainer").innerHTML="";

    //Loop over tables and repaint
    for (var tableKey in this.tables) {
        if (this.tables.hasOwnProperty(tableKey)) {
            this.drawTable(tableKey);
        }
    }
};

//Draws table into page
Monitor.prototype.drawTable = function(title){
    var table=this.tables[title];
    //Table has already been added
    if (!table) {
        return;
    }

    //Add table container
    this.drawTableContainer(table);

    //Add each row to the table
    var tableHashCode = table.title.hashCode();
    var tbodyDOM=document.getElementById("tbody"+tableHashCode);
    for(var i=0;i<table.rows.length;i++){
        var rowHtml=this.buildRow(tableHashCode,table.rows[i]);
        tbodyDOM.insertAdjacentHTML("beforeend",rowHtml);
    }
};

//Adds the table template to the DOM
Monitor.prototype.drawTableContainer = function (table) {
    //tableHashCode will generate a unique suffix for dom ids
    var tableHashCode = table.title.hashCode();

    //Get table template
    var tableTemplate = document.getElementById('tableTemplate').innerHTML;
    //Turn into a custom table
    tableTemplate = tableTemplate.replace(/###/g, tableHashCode);
    //Add to the stats container
    document.getElementById("statsContainer").insertAdjacentHTML("beforeend",tableTemplate);

    //Update table title
    document.getElementById("title"+tableHashCode).innerHTML = table.title;
};

//Builds tr markup for each table row
Monitor.prototype.buildRow = function (tableHashCode, row){
    var rowHtml="<tr>"
    for(var i=0;i<row.columnClasses.length;i++){
        rowHtml=rowHtml+this.buildColumn(row,i);
    }
    rowHtml=rowHtml+"</tr>";
    return rowHtml;
};

//Builds td markup for each table row
Monitor.prototype.buildColumn = function (row, i){
    var columnHtml = "<td class='"+row.columnClasses[i]+"'>"
    columnHtml = columnHtml + row.columnData[this.timeUnit][i];
    columnHtml = columnHtml+"</td>";
    return columnHtml;
};

//Adds a hashCode to String so you can generate a table Id from its title
String.prototype.hashCode = function() {
  var hash = 0, i, chr, len;
  if (this.length === 0) return hash;
  for (i = 0, len = this.length; i < len; i++) {
    chr   = this.charCodeAt(i);
    hash  = ((hash << 5) - hash) + chr;
    hash |= 0; // Convert to 32bit integer
  }
  return hash;
};

var monitor = new Monitor();
