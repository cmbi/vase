
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

function loadJob(jobid) {
	
	var job = { id:jobid, status:'new' };
	
	$.ajax({
		  type: "GET",
		  dataType: "text",
		  url: "rest/status/"+jobid,
		  data: '',
		  success: function(data, status, jqXHR) {
			  
			  job.status = status;
		  }
		});
	
	return job;
}

var jobidsCookieName = 'jobs';
var cookiesAccepted=false;

var jobs=[];

function submitJob(structure) {
	
	console.log("submitting:\n"+structure)
	
	$.ajax({
		  type: "POST",
		  url: "rest/custom",
		  data: structure,
		  dataType: 'text',
		  
		  success: function(data, status, jqXHR) {

			  var job = { id:data, status: 'new' };
			  
			  console.log(" job submitted: "+data);
				
			  jobs.push( job );
			  
			  saveJobs();
			  
			  updateJobListingRow(job);
		  },
	
		  error: function( jqXHR, status, errorThrown ) { }
	});
}

function pollJobs() {
	
	for(var i=0; i<jobs.length;i++) {
		
		$.ajax( {
			  type: "GET",
			  dataType: "text",
			  url: "rest/status/"+jobs[i].id,
			  data: '',
			  
			  beforeSend: function(jqXHR, settings) {
			        jqXHR.url = settings.url;
			    },
			  success: function(data, status, jqXHR) {
				  
				  console.log("url:"+jqXHR.url);
				  
				  jobs[i].status = data;
				  
				  updateJobListingRow(jobs[i]);
			  }
		} );
	}
}

function saveJobs() {
	
	var s="";
	for(var i=0; i<jobs.length; i++) {
		
		if(s.length>0) s+= ',' ;
		
		s += jobs[i].id ;
	}
	
	setCookie(jobidsCookieName, s, 365);
}

function initPage() {
	
	var cookie = getCookie(jobidsCookieName);
	
	console.log("cookie:"+cookie);
	
	if(cookie==null ) {
		
		cookiesAccepted = confirm('Do you allow this page to use cookies to remember your job submissions for you?');
		
		if(cookiesAccepted)	{
			
			setCookie(jobidsCookieName, '', 365);
		}
			
	} else {
		
		cookiesAccepted = true;
		
		if(cookie.trim().length>0) {
		
			var jobids = cookie.split(',');
			
			for(var i=0; i<jobids.length; i++) {
				
				jobs.push( loadJob( jobids[i] ) );
			}
		}
	}
	
	updateJobListing();
}

function addJobRow( table, job) {
	
	var row = table.tBodies[0].insertRow(-1);
	row.id=job.id;
	
	var idCell= row.insertCell(0);
	idCell.innerHTML = job.id;
	
	var statusCell= row.insertCell(1);
	statusCell.innerHTML = job.status ;
}

function updateJobListingRow(job) {
	
	var list = document.getElementById("joblisting");
	
	for(var i=0; i<list.rows.length; i++) {
		
		if(list.rows[i].id==job.id) {
			
			list.rows[i].cells[1].innerHTML=jobs[i].status;
			return;
		}
	}

	// if not found, add to rows
	addJobRow( list, job );
}

function updateJobListing() {
	
	var list = document.getElementById("joblisting");
	for(var i=0; i<jobs.length; i++) {
		
		addJobRow( lits, sobs[i]);
	}
}