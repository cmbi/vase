
var restURL = "../rest";

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
	
	var present = false;
	for( var i=0; i<jobs.length; i++ ) {
		
		if( jobs[i].id == job.id ) {
			present = true;
		}
	}
	
	if(!present) {
		
		jobs.push( job );
	}
	
	if( cookiesAccepted ) {
		
		saveJobIDs();
	}
	
	updateJobListingRow(job); // adds it if not yet in table
}

var pJobID = /^[0-9a-zA-Z\-]+$/ ;

function submitJob(structure) {
	
	console.log("submitting: "+structure);
	
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

function addJobRow( table, job) {
	
	var row = table.tBodies[0].insertRow(-1);
	row.id=job.id;
	
	var idCell= row.insertCell(0);
	idCell.innerHTML = job.id;
	
	var statusCell= row.insertCell(1);
	statusCell.innerHTML = job.status ;
	
	var deleteCell= row.insertCell(2);
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
	
	for(var i=0; i<list.rows.length; i++) {
		
		if(list.rows[i].id
			&& list.rows[i].id==job.id) {
			
			list.rows[i].cells[1].innerHTML=job.status;
			return;
		}
	}

	// if not found, add to rows
	addJobRow( list, job );
}