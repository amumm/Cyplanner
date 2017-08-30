var stompClient = null;

function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
//       setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/client/manage-requirements', function (managing) {
            if(managing.body!="fail"){
              $("#requirements_tables").html(managing.body);
              $(".requirement_header").click(function(){
                $("."+$(this).attr('id')+"_row").toggle();
              });
              $(".requirement_row").dblclick(function(){
            	  
            	localStorage.setItem("courseToAdd",$(this).attr("id"));
            	$("#semester_to_add_to").show();
              });
            }
            else
            	$("#requirements_tables").html("");
        });
        stompClient.subscribe('/client/manage-Schedule', function (message) {
        	result = message.body;
        	localStorage.setItem("schedules", result);
        	alert("Schedule Saved!");
        	System.out.print("complete");
          });
        stompClient.subscribe('/client/manage-autocomplete', function (message) {
          var ar=message.body.split(", ");
        	for(i=0;i<ar.length-1;i++){
        		console.log(ar[i]);
        	}
        	$("#major_add").autocomplete({source:ar});
        });
        
        stompClient.subscribe('/client/manage-autocomplete-course', function (message) {
            var ar=message.body.split(", ");
          	for(i=0;i<ar.length-1;i++){
          		console.log(ar[i]);
          	}
          	$("#course_add").autocomplete({source:ar});
          });

        stompClient.subscribe('/client/manage-semesters', function (message) {
          $("#semester_tables").html(message.body);
        });
        
        stompClient.subscribe('/client/manage-course-info', function (message) {
        	var input=document.getElementById("semester_to_add_to_for_course").value;
        	document.getElementById("semester_to_add_to_for_course").value="";
        	
        	//get localstorage-stored courses and credits arrays
        	var temp_arr= JSON.parse(localStorage.getItem("courses"));
        	var temp_creds=JSON.parse(localStorage.getItem("credits"));
        	
        	var is_in_schedule=false;
        	for(var i=0; i<temp_arr.length;i++){
        		if(temp_arr[i].indexOf(message.body.split(" ")[0])>-1){
        			is_in_schedule=true;
        			break;
        		}
        	}
        	if(input!="" && message.body.split(" ")[1]!=null && !is_in_schedule){
            	
            	
            	//add the new course data
            	temp_creds[input].push(parseInt(message.body.split(" ")[1]));
        	  	temp_arr[input].push(message.body.split(" ")[0]);
        	  	
        	  	//put the arrays back into the localStorage
        	  	localStorage.setItem("courses",JSON.stringify(temp_arr));
        	  	localStorage.setItem("credits",JSON.stringify(temp_creds));
        	  	//add a row to the necessary semester table
        	  	$("#semester_"+input).html($("#semester_"+input).html().split("</tbody></table>")[0]+"<tr id='"+message.body.split(" ")[0]+"_semester' class='semester_row_"+input+"' style='cursor:pointer;'><td>"+message.body.split(" ")[0]+"</td><td>"+message.body.split(" ")[1]+"</td></tr></tbody></table>");
        	  	
        	  	//add the jquery handler for deletion of this row to this row
        	  	$(".semester_row_"+input).dblclick(function deletion(){
        	  		//get localstorage-stored courses and credits arrays
	        		var temp_arr= JSON.parse(localStorage.getItem("courses"));
	        		var temp_creds=JSON.parse(localStorage.getItem("credits"));
	        		var j=parseInt($(this).attr('class').split("_")[2]);
//	        		alert(j);
	        		if(temp_arr[j].indexOf($(this).attr('id').split("_semester")[0])>=0){
	        			temp_arr[j].splice(temp_arr[j].indexOf($(this).attr('id').split("_semester")[0]),1);
	        			temp_creds[j].splice(temp_arr[j].indexOf($(this).attr('id').split("_semester")[0]),1);
	        		}
	        		  
	        		//put the arrays back into the localStorage
	        		localStorage.setItem("courses",JSON.stringify(temp_arr));
	        		localStorage.setItem("credits",JSON.stringify(temp_creds));
	        		
	        		//rerender the semester
	        		var result="";
	        		result +="<table style='width: 100%;'>";
					result +="<tr style='width: 100%;'>";
					result +="<th colspan='2'>Semester "+(j)+"</th>";
					result +="</tr>";
					result +="<tr style='width: 100%;'>";
					result +="<th>Course ID</th><th>credits</th>";
					result +="</tr>";
					for(var k=0;k<temp_arr[j].length;k++){
						result +="<tr id='"+temp_arr[j][k]+"_semester' class='semester_row_"+(j)+"' style='cursor:pointer;'>";
						result +="<td>";
						result +=temp_arr[j][k];
						result +="</td>";
						result +="<td>";
						if(temp_creds[j][k]!=null)
							result +=temp_creds[j][k];
						else 
							result +="R";
						result +="</td>";
						result +="</tr>";
					}
					
					result +="</table>";
	        		
	        		$("#semester_"+(j)).html(result);
	        		$(".semester_row_"+(j)).dblclick(deletion);
	        		//re-validate the schedule
	            	var obj = {courses:JSON.parse(localStorage.getItem("courses")), credits:JSON.parse(localStorage.getItem("credits"))};
	            	send_validation(JSON.stringify(obj)+" ;;; "+document.getElementById('major_add').value);
	        	  });

        	  	
        	  	
        	  	//re-validate the schedule
            	var obj = {courses:JSON.parse(localStorage.getItem("courses")), credits:JSON.parse(localStorage.getItem("credits"))};
            	send_validation(JSON.stringify(obj)+" ;;; "+document.getElementById('major_add').value);
            }
            //clear and hide the input box
            document.getElementById("semester_to_add_to_for_course").value="";
            $(this).hide();
        });
        
        stompClient.subscribe('/client/manage-validation', function (message) {
        	
        	var arr=JSON.parse(message.body);
        	var result="<table style='width:100%;margin-left: 10%;'><tbody>";
        	result+="<tr><th colspan='2' id='validate_header' style='cursor:pointer;'>Requirements and Courses that aren't met</th></tr>";
        	result+="<tr class='validate_row' style='display:none;'><th>Requirement/Course Name</th><th>Reason for error</th></tr>";
        	for(var i =0;i<arr[0].length;i++){
        		result+="<tr class='validate_row' style='display:none;'>";
        		result+="<td>";
        		result+=arr[0][i];
        		result+="</td>";
        		result+="<td>";
        		result+=arr[1][i];
        		result+="</td>";
        		result+="</tr>";
        	}
        	result+="</tbody></table>";
        	
        	$("#validation_div").html(result);
        	$("#validate_header").click(function(){
        		$(".validate_row").toggle();
        	});
        });

    });

}
function sendSchedule(message)
{
	stompClient.send("/app/manageSchedule",{},message);
}
function send_requirements(message)
{
  stompClient.send("/app/manage-requirements",{},message);
}

function send_autocomplete(message)
{
  stompClient.send("/app/manage-autocomplete",{},message);
}

function send_autocomplete_course(message)
{
  stompClient.send("/app/manage-autocomplete-course",{},message);
}

function send_semesters(message)
{
  stompClient.send("/app/manage-semesters",{},message);
}

function send_validation(message)
{
	stompClient.send("/app/manage-validation",{},message);
}

function send_for_course_info(message)
{
	stompClient.send("/app/manage-course-info",{},message);
}

$(document).ready(function () {
	connect();
	//set up localstorage and connect to server
	var temp_courses=new Array();
	var temp_credits=new Array();
	for(var i =0;i<9;i++){
		temp_courses.push(new Array());
		temp_credits.push(new Array());
	}
	if(localStorage.getItem("fromAuto") == 'false' || localStorage.getItem("fromAuto") == null){
		localStorage.setItem("courses",JSON.stringify(temp_courses));
		localStorage.setItem("credits",JSON.stringify(temp_credits));
	}
	else{
		setTimeout(function(){
			$("#major_add").val(localStorage.getItem("program"));
			send_requirements(document.getElementById('major_add').value);
			var obj = {courses:JSON.parse(localStorage.getItem("courses")), credits:JSON.parse(localStorage.getItem("credits"))};
        	send_validation(JSON.stringify(obj)+" ;;; "+document.getElementById('major_add').value);
		}, 1000);
		var credits = 0;
		var temp_arr= JSON.parse(localStorage.getItem("courses"));
		var temp_creds=JSON.parse(localStorage.getItem("credits"));
		var result = "";
		if(localStorage.getItem("compCourses")!=null)
			var comp_courses=localStorage.getItem("compCourses").split(" ");
		else
			comp_courses=null;
		result +="<div class='semester_table'id='semester_0'>";
		result +="<table style='width: 100%;'>";
		result +="<tr style='width: 100%;'>";
		result +="<th colspan='2'>Semester 0 (Classes taken before attending ISU)</th>";
		result +="</tr>";	
		result +="<tr style='width: 100%;'>";
		result +="<th>Course ID</th><th>credits</th>";
		result +="</tr>";
		if(comp_courses!=null){
			for(var k=0;k<comp_courses.length-1;k++){
//				if(temp_creds[i][k] == null){
					credits += parseInt(0);
					result += "<tr id='"+comp_courses[k]+"_semester' class='semester_row_0' style='cursor:pointer;width: 100%;'>";
					result += "<td>" + comp_courses[k] + "</td>";
					result += "<td>3</td>";
					result +="</tr>";
//				}
//				else{
//					credits += parseInt(temp_creds[i][k]);
//					result += "<tr id='"+temp_arr[i][k]+"_semester' class='semester_row_"+(i+1)+"' style='cursor:pointer;width: 100%;'>";
//					result += "<td>" + temp_arr[i][k] + "</td>";
//					result += "<td>" + temp_creds[i][k] + "</td>";
//					result +="</tr>";
//				}
			}
		}
		result +="</table>";
		result +="</div>";
		var numSems = parseInt(localStorage.getItem("num_semesters"));
		for(var i = 0; i < numSems; i++){
			credits = parseInt(0);
			result +="<div class='semester_table'id='semester_"+(i+1)+"'>";
			result +="<table style='width: 100%;'>";
			result +="<tr style='width: 100%;'>";
			result +="<th colspan='2'>Semester "+(i+1)+"</th>";
			result +="</tr>";	
			result +="<tr style='width: 100%;'>";
			result +="<th>Course ID</th><th>credits</th>";
			result +="</tr>";
			for(var k = 0; k < temp_arr[i].length; k++){
				if(temp_creds[i][k] == null){
					credits += parseInt(0);
					result += "<tr id='"+temp_arr[i][k]+"_semester' class='semester_row_"+(i+1)+"' style='cursor:pointer;width: 100%;'>";
					result += "<td>" + temp_arr[i][k] + "</td>";
					result += "<td>R</td>";
					result +="</tr>";
				}
				else{
					credits += parseInt(temp_creds[i][k]);
					result += "<tr id='"+temp_arr[i][k]+"_semester' class='semester_row_"+(i+1)+"' style='cursor:pointer;width: 100%;'>";
					result += "<td>" + temp_arr[i][k] + "</td>";
					result += "<td>" + temp_creds[i][k] + "</td>";
					result +="</tr>";
				}
				
			}
//			result += "<tr style='width: 100%;'>";
//			result += "<td>Total Credits</td>";
//			result += "<td>" + credits + "</td>";
//			result +="</tr>";
			result +="</table>";
			result +="</div>";
			
			
		}
		temp_arr.unshift(new Array());
		temp_creds.unshift(new Array());
		
		if(comp_courses!=null){
			for(var k=0;k<comp_courses.length -1;k++){
				temp_arr[0][k]=comp_courses[k];
				temp_creds[0][k]=3;
			}
		}
		
		localStorage.setItem("courses",JSON.stringify(temp_arr));
		localStorage.setItem("credits",JSON.stringify(temp_creds));
		$("#semester_tables").html(result);
		//add the jquery handler for deletion of this row to this row
		for(var k=-1;k<numSems;k++){
		  	$(".semester_row_"+(k+1)).dblclick(function delete_handler(){
		  		//get localstorage-stored courses and credits arrays
	    		var temp_arr= JSON.parse(localStorage.getItem("courses"));
	    		var temp_creds=JSON.parse(localStorage.getItem("credits"));
	    		var j=parseInt($(this).attr('class').split("_")[2]);
	//    		alert(j);
	    		if(temp_arr[j].indexOf($(this).attr('id').split("_semester")[0])>=0){
	    			temp_arr[j].splice(temp_arr[j].indexOf($(this).attr('id').split("_semester")[0]),1);
	    			temp_creds[j].splice(temp_arr[j].indexOf($(this).attr('id').split("_semester")[0]),1);
	    		}
	    		  
	    		//put the arrays back into the localStorage
	    		localStorage.setItem("courses",JSON.stringify(temp_arr));
	    		localStorage.setItem("credits",JSON.stringify(temp_creds));
	    		
	    		//rerender the semester
	    		var result="";
	    		result +="<table style='width: 100%;'>";
				result +="<tr style='width: 100%;'>";
				result +="<th colspan='2'>Semester "+(j)+"</th>";
				result +="</tr>";
				result +="<tr style='width: 100%;'>";
				result +="<th>Course ID</th><th>credits</th>";
				result +="</tr>";
				for(var l=0;l<temp_arr[j].length;l++){
					result +="<tr id='"+temp_arr[j][l]+"_semester' class='semester_row_"+(j)+"' style='cursor:pointer;'>";
					result +="<td>";
					result +=temp_arr[j][l];
					result +="</td>";
					result +="<td>";
					if(temp_creds[j][l]!=null)
						result +=temp_creds[j][l];
					else 
						result +="R";
					result +="</td>";
					result +="</tr>";
				}
				
				result +="</table>";
	    		
	    		$("#semester_"+(j)).html(result);
	//    		alert(delete_handler);
	    		$(".semester_row_"+(j)).dblclick(delete_handler);
	    		//re-validate the schedule
            	var obj = {courses:JSON.parse(localStorage.getItem("courses")), credits:JSON.parse(localStorage.getItem("credits"))};
            	send_validation(JSON.stringify(obj)+" ;;; "+document.getElementById('major_add').value);
	    	  });
			}
	  	localStorage.setItem("fromAuto", false);
	}
  
  //add button/click handlers
  $("#major_add").on('input',function(){
    send_autocomplete(document.getElementById('major_add').value);
  });
  
  $("#course_add").on('input',function(){
	 send_autocomplete_course(document.getElementById('course_add').value);
  });
  
  
  $("#semester_to_add_to").change(function(){
	var input=document.getElementById("semester_to_add_to").value;
    if(input!=""){
    	//get localstorage-stored courses and credits arrays
    	var temp="#"+localStorage.getItem("courseToAdd");
    	var temp_arr= JSON.parse(localStorage.getItem("courses"));
    	var temp_creds=JSON.parse(localStorage.getItem("credits"));
    	
    	var is_in_schedule=false;
    	for(var i=0; i<temp_arr.length;i++){
    		if(temp_arr[i].indexOf($(temp).attr("id"))>-1){
    			is_in_schedule=true;
    			break;
    		}
    	}
    	
    	if(!is_in_schedule){
	    	//add the new course data
	    	temp_creds[input].push(parseInt($(temp).html().split("<td>")[3].replace(/<\/td>/g ,"")));
		  	temp_arr[input].push($(temp).attr("id"));
		  	
		  	//put the arrays back into the localStorage
		  	localStorage.setItem("courses",JSON.stringify(temp_arr));
		  	localStorage.setItem("credits",JSON.stringify(temp_creds));
	    	
		  	//add a row to the necessary semester table
		  	$("#semester_"+input).html($("#semester_"+input).html().split("</tbody></table>")[0]+"<tr id='"+$(temp).attr("id")+"_semester' class='semester_row_"+input+"' style='cursor:pointer;'><td>"+$(temp).attr("id")+"</td><td>"+$(temp).html().split("<td>")[3]+"</td></tr></tbody></table>");
		  	
		  //add the jquery handler for deletion of this row to this row
		  	$(".semester_row_"+input).dblclick(function delete_handler(){
		  		//get localstorage-stored courses and credits arrays
	    		var temp_arr= JSON.parse(localStorage.getItem("courses"));
	    		var temp_creds=JSON.parse(localStorage.getItem("credits"));
	    		var j=parseInt($(this).attr('class').split("_")[2]);
	//    		alert(j);
	    		if(temp_arr[j].indexOf($(this).attr('id').split("_semester")[0])>=0){
	    			temp_arr[j].splice(temp_arr[j].indexOf($(this).attr('id').split("_semester")[0]),1);
	    			temp_creds[j].splice(temp_arr[j].indexOf($(this).attr('id').split("_semester")[0]),1);
	    		}
	    		  
	    		//put the arrays back into the localStorage
	    		localStorage.setItem("courses",JSON.stringify(temp_arr));
	    		localStorage.setItem("credits",JSON.stringify(temp_creds));
	    		
	    		//rerender the semester
	    		var result="";
	    		result +="<table style='width: 100%;'>";
				result +="<tr style='width: 100%;'>";
				result +="<th colspan='2'>Semester "+(j)+"</th>";
				result +="</tr>";
				result +="<tr style='width: 100%;'>";
				result +="<th>Course ID</th><th>credits</th>";
				result +="</tr>";
				for(var k=0;k<temp_arr[j].length;k++){
					result +="<tr id='"+temp_arr[j][k]+"_semester' class='semester_row_"+(j)+"' style='cursor:pointer;'>";
					result +="<td>";
					result +=temp_arr[j][k];
					result +="</td>";
					result +="<td>";
					if(temp_creds[j][k]!=null)
						result +=temp_creds[j][k];
					else 
						result +="R";
					result +="</td>";
					result +="</tr>";
				}
				
				result +="</table>";
	    		
	    		$("#semester_"+(j)).html(result);
	//    		alert(delete_handler);
	    		$(".semester_row_"+(j)).dblclick(delete_handler);
	    		//re-validate the schedule
	        	var obj = {courses:JSON.parse(localStorage.getItem("courses")), credits:JSON.parse(localStorage.getItem("credits"))};
	        	send_validation(JSON.stringify(obj)+" ;;; "+document.getElementById('major_add').value);
	    	  });
		  	
		  	//re-validate the schedule
	    	var obj = {courses:JSON.parse(localStorage.getItem("courses")), credits:JSON.parse(localStorage.getItem("credits"))};
	    	send_validation(JSON.stringify(obj)+" ;;; "+document.getElementById('major_add').value);
	    }
    }
    //clear and hide the input box
    document.getElementById("semester_to_add_to").value="";
    $(this).hide();
  });
  
  $("#semester_to_add_to_for_course").change(function(){
	  send_for_course_info(document.getElementById('course_add').value);
	//hide the input box
//    document.getElementById("semester_to_add_to_for_course").value="";
    $(this).hide();
  });
  
  $( "#major_add" ).change(function() {
    send_requirements(document.getElementById('major_add').value);
    send_semesters("create");
    localStorage.setItem("num_semesters",8);
  });
  
  
  $("#add_semester_button").click(function(){
	  //add a new array to the local storage arrays
	  var temp_arr= JSON.parse(localStorage.getItem("courses"));
	  var temp_creds=JSON.parse(localStorage.getItem("credits"));
	  temp_arr.push(new Array());
	  temp_creds.push(new Array());
	  localStorage.setItem("courses",JSON.stringify(temp_arr));
	  localStorage.setItem("credits",JSON.stringify(temp_creds));
	  
	  //generate the new semester table and update the number of semesters
	  var result="";
	  result +="<div class='semester_table'id='semester_"+(parseInt(localStorage.getItem("num_semesters"))+1)+"'>";
	  localStorage.setItem("num_semesters",(parseInt(localStorage.getItem("num_semesters"))+1));
	  result +="<table style='width: 100%;'>";
	  result +="<tr style='width: 100%;'>";
	  result +="<th colspan='2'>Semester "+(parseInt(localStorage.getItem("num_semesters")))+"</th>";
	  result +="</tr>";
	  result +="<tr style='width: 100%;'>";
	  result +="<th>Course ID</th><th>credits</th>";
	  result +="</tr>";
	  result +="</table>";
	  result +="</div>";
	  
	  //add the table to the div
	  $("#semester_tables").html($("#semester_tables").html()+result);
  });
  
  $("#course_add").change(function(){
	 $("#semester_to_add_to_for_course").show(); 
  });
  $("#saved").click(function(){
		var message = localStorage.getItem("user") + " ";
		message+=document.getElementById("scheduleName").value + " ";
		
		var courses = JSON.parse(localStorage.getItem("courses")).slice(1,JSON.parse(localStorage.getItem("courses")).length);
		message += JSON.stringify(courses) + " ";
		
		var credits = JSON.parse(localStorage.getItem("credits")).slice(1,JSON.parse(localStorage.getItem("credits")).length);
		message += JSON.stringify(credits) + " ";
		
		message+=localStorage.getItem("program");
		document.getElementById("scheduleName").value=" ";
		sendSchedule(message);
						
		
		});
		
  
  
  //  $("#validate_button").click(function(){
//	  var obj = {courses:JSON.parse(localStorage.getItem("courses")), credits:JSON.parse(localStorage.getItem("credits"))};
//	  send_validation(JSON.stringify(obj)+" ;;; "+document.getElementById('major_add').value);
//  });
  

});
