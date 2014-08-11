
var restURL = baseURL + "/rest",
	alignURL = baseURL + "/align";

function endswith(string,ending) {
	
	return string.substring(string.length - ending.length) == ending;
}

function setCookie(cname, cvalue, exdays) {
	
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+d.toGMTString();
    document.cookie = cname + "=" + cvalue + "; " + expires;
}

function getCookie(cname) {
	
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i=0; i<ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1);
        if (c.indexOf(name) != -1) return c.substring(name.length,c.length);
    }
    return null;
}

function deleteCookie(cname) {
	
	document.cookie = cname+"=; expires=Thu, 01 Jan 1970 00:00:00 GMT";
}

function initJob( jobid ) {
	
	var job = { id:jobid, status:'new' };
	
	return job;
}

var jobidsCookieName = 'jobs';
var cookiesAccepted=false;

var jobs=[];

function saveJobIDs() {
	
	var s="";
	for(var i=0; i<jobs.length; i++) {
		
		if(s.length>0) s+= ',' ;
		
		s += jobs[i].id ;
	}
	setCookie(jobidsCookieName, s, 365);
}

function jobRemove( jobid ) {
	
	for( var i=0; i<jobs.length; i++ ) {
		
		if( jobs[i].id == jobid ) {
			
			jobs.splice( i, 1 );
		}
	}
	
	removeJobListingRow( jobid );
	
	if( cookiesAccepted ) {
		
		saveJobIDs();
	}
}

function jobAdd( job ) {
	
	var jobIndex = -1;
	for( var i=0; i<jobs.length; i++ ) {
		
		if( jobs[i].id == job.id ) {
			jobIndex = i;
		}
	}
	
	if(jobIndex<0) {
		
		jobs.push( job );
		jobIndex = jobs.length - 1;
	}
	
	if( cookiesAccepted ) {
		
		saveJobIDs();
	}
	
	updateJobListingRow(jobs[ jobIndex ]); // adds it if not yet in table
}

var pJobID = /^[0-9a-zA-Z\-]+$/ ;

function submitJob(structure) {
	
	$.ajax({
		  type: "POST",
		  url: restURL+"/custom",
		  data: { pdbfile: structure },
		  
		  success: function(data, status, jqXHR) {

			  var jobid = data;
			  if( pJobID.test( data ) ) {

				  var job = initJob( data );
				  
				  jobAdd( job );
			  }
			  else console.log("an invalid job id was returned by rest: '"+jobid+"'");
		  },
	
		  error: function( jqXHR, status, errorThrown ) {
			  
			  alert("An error occurred while submitting your job. " + errorThrown);
			  
			  console.log("error on job submit:" + errorThrown);
		  }
	});
}

function pollJobs() {
	
	for(var i=0; i<jobs.length;i++) {
		
		$.ajax( {
			  type: "GET",
			  url: restURL+"/status/"+jobs[i].id,
			  data: '',
			  
			  beforeSend: function(jqXHR, settings) {
				  
				  jqXHR.url = settings.url;
			  },
			  success: function(data, status, jqXHR) {
				  
				  for(var i=0; i<jobs.length;i++) {
					  
					  if( endswith( jqXHR.url, "/"+jobs[i].id ) ) {
						  
						  jobs[i].status = data;
						  
						  updateJobListingRow(jobs[i]);
					  }
				  }
			  }
		} );
	}
}

function initJobPage() {
	
	var cookie = getCookie( jobidsCookieName );
	
	if(cookie==null ) {
		
		cookiesAccepted = confirm('Do you allow this page to use cookies, to remember your job submissions for you?');
		
		if(cookiesAccepted)	{
			
			setCookie(jobidsCookieName, '', 365);
		}
			
	} else {
		
		cookiesAccepted = true;
		
		if( cookie.trim().length>0 ) {
		
			var jobids = cookie.split(',');
			
			for(var i=0; i<jobids.length; i++) {
				
				var job = initJob( jobids[i] );
				
				jobAdd( job );
			}
		}
		
		// Do an initial poll to get rid of the status 'new'
		pollJobs();
	}
}

function setTextareaToPDBFileOf( jobid ) {

	$.ajax( {
		  type: "GET",
		  url: restURL+"/structure/"+jobid,
		  data: '',
		  
		  success: function(data, status, jqXHR) {
			  
			  document.getElementById("pdb-input").value = data ;
		  }
	} );
	
}

function addJobRow( table, job) {
	
	var row = table.tBodies[0].insertRow(-1);
	row.id=job.id;
	
	var arrowLeftCell = row.insertCell(0);
	arrowLeftCell.setAttribute("title","Show Submitted Data on "+job.id);
	arrowLeftCell.setAttribute("class","show-job-input glyphicon glyphicon-arrow-left");
	arrowLeftCell.setAttribute("onclick","setTextareaToPDBFileOf( '"+job.id+"' );");
	
	var idCell= row.insertCell(1);
	idCell.innerHTML = job.id;
	
	var statusCell= row.insertCell(2);
	statusCell.innerHTML = job.status ;
	
	var deleteCell= row.insertCell(3);
	deleteCell.setAttribute("title","Remove from List");
	deleteCell.setAttribute("class","delete-job-id glyphicon glyphicon-remove");
	deleteCell.setAttribute("onclick","jobRemove('"+job.id+"');");
}

function removeJobListingRow( jobid ) {
	
	var list = document.getElementById("joblisting");
	var row = document.getElementById(jobid);
	
	if (list != null && row != null) {
		list.tBodies[0].removeChild(row);
	}
}

function updateJobListingRow(job) {
	
	var list = document.getElementById("joblisting");
	
	var present = false;
	for(var i=0; i<list.rows.length; i++) {
		
		if(list.rows[i].id
			&& list.rows[i].id==job.id) {
			
			if(job.status.toLowerCase()=="finished") {

				list.rows[i].cells[1].innerHTML="<a href='"+alignURL+"/"+job.id+"'>"+job.id+"</a>"
			}
			
			list.rows[i].cells[2].innerHTML=job.status;
			present=true;
		}
	}

	// if not found, add a new row
	if(!present) {
		
		addJobRow( list, job );
	}
}