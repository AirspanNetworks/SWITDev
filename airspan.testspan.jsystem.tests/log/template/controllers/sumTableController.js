/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function appendTestsToSumTable(tests, table) {

    var success = 0;
    var error = 0;
    var failure = 0;
    var warning = 0;
    var duration = 0;
    var running = 0;
    var name = null;
	var uid = null;
    var cores = "";
    var suites = new Array();
    $(tests).each(function() {
	

        if (name !== null /*&& name !== this.name*/) {
            //TODO : Handle same scenario name in different machines
            suites.push({"name": name, "duration": duration, "success": success, "error": error, "failure": failure, "warning": warning, "uid": uid,"cores": props,"running": running});
            success = error = failure = warning = duration = running = 0;
			
        }
		uid = this.uid;
        name = formatName(this.className) + " - " + this.name ;
        switch (this.status) {
            case "success":
                if (this.duration == 0){
                    running++ ;
                    break;
                }
                success++;
                break;
            case "error":
                error++;
                break;
            case "failure":
                failure++;
                break;
            case "warning":
                warning++;
                break;
        }
        
        props = this.properties;
        duration += this.duration * 1;


    });
    //The last scenario
    suites.push({"name": name, "duration": duration, "success": success, "error": error, "failure": failure, "warning": warning,"uid": uid,"cores": props,"running": running});
    var total = {"tests": 0, "duration": 0, "success": 0, "error": 0, "failure": 0, "warning": 0,"running":0};

    $(suites).each(function() {
        var durInSec = Math.round(this.duration/1000);
        var durationHour = Math.floor(((durInSec % 31536000) % 86400) / 3600);
        var durationMin = Math.floor((((durInSec % 31536000) % 86400) % 3600) / 60);
        var durationSec = (((durInSec % 31536000) % 86400) % 3600) % 60;
        var tr = $('<tr>');
        tr.append($('<td>').append($('<a>').text(this.name).attr("href","tests/test_"+this.uid+"/test.html")));
        var tests = this.success + this.error + this.failure + this.warning * 1;
        tr.append($('<td>').text(tests));
        total.tests+=tests;
        tr.append($('<td>').text(durationHour + "h" + durationMin + "m" + durationSec + "s"));
        total.duration += this.duration;

        tr.append($('<td>').text(this.running).addClass(this.running > 0 ? "s_running_back" : ""));
        total.running += this.running;

        tr.append($('<td>').text(this.success).addClass(this.success > 0 ? "s_success_back" : ""));
        total.success += this.success;
        tr.append($('<td>').text(this.warning).addClass(this.warning > 0 ? "s_warning_back" : ""));
        total.warning += this.warning;
        tr.append($('<td>').text(this.failure).addClass(this.failure > 0 ? "s_failure_back" : ""));
        total.failure += this.failure;
        
        
       // tr.append($('<td>').text(calculateSuccessRate(this)+"%"));
        var coreTD = $('<td>');
        createCoreTD(this.cores,coreTD)
        tr.append(coreTD);

        tr.append($('<td>').text(this.error).addClass(this.error > 0 ? "s_error_back" : ""));
        total.error += this.error;

        $(table).append(tr);
    });
    var tr = $('<tr>');
    tr.append($('<td>').text("Total"));
    tr.append($('<td>').text(total.tests));
    durInSec = Math.round(total.duration/1000);
    durationHour = Math.floor(((durInSec % 31536000) % 86400) / 3600);
    durationMin = Math.floor((((durInSec % 31536000) % 86400) % 3600) / 60);
    durationSec = (((durInSec % 31536000) % 86400) % 3600) % 60;
    tr.append($('<td>').text(durationHour + "h" + durationMin + "m" + durationSec + "s"));
    tr.append($('<td>').text(total.running).addClass(total.running > 0 ? "s_running_back" : ""));
    tr.append($('<td>').text(total.success).addClass(total.success > 0 ? "s_success_back" : ""));
    tr.append($('<td>').text(total.warning).addClass(total.warning > 0 ? "s_warning_back" : ""));
    tr.append($('<td>').text(total.failure).addClass(total.failure > 0 ? "s_failure_back" : ""));
    tr.append($('<td>').text(""));
    tr.append($('<td>').text(total.error).addClass(total.error > 0 ? "s_error_back" : ""));
   // tr.append($('<td>').text(calculateSuccessRate(total)+"%"));
    $(table).append(tr);
}

function calculateSuccessRate(element){
    var allTests = element.success + element.error + element.failure + element.warning;
    if (allTests === 0){
        return 0;
    }
    return Math.round(element.success / allTests * 100);
}

function sumTableController(element) {
    var tests = collectAllTests();
    appendTestsToSumTable(tests, element);

}

function createCoreTD(props,td){
    var count=1;
    for (var key in props) {
        if(key.includes("CoreFilePath")){
            
            coreName = count//key.split("_")[2]
            td.append($('<a>').text(coreName + " ").attr("href",props[key]));
           /* if(count%6 == 0)
            td.append($('<br>'))*/
        count++;
            
        }
    };
}

function formatName(className) {
	
  var NameArr = className.split(".");
  var finalName = "["
  for (i=1 ; i<NameArr.length ; i++ ){
	   finalName+=NameArr[i]
	  if(i!=NameArr.length-1 )
		  finalName+="->"
	  else
		   finalName+="]"
  }
  
  return finalName;
  
  

}
