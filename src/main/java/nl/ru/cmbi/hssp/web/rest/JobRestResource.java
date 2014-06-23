package nl.ru.cmbi.hssp.web.rest;

import java.lang.annotation.Annotation;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.ru.cmbi.hssp.job.HsspJob;
import nl.ru.cmbi.hssp.job.HsspQueue;
import nl.ru.cmbi.hssp.web.WicketApplication;
import nl.ru.cmbi.hssp.web.page.AlignmentPage;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.annotations.parameters.RequestBody;
import org.wicketstuff.rest.annotations.parameters.RequestParam;
import org.wicketstuff.rest.resource.gson.GsonRestResource;
import org.wicketstuff.rest.utils.http.HttpMethod;

public class JobRestResource extends GsonRestResource {
	
	private static final Logger log = LoggerFactory.getLogger(JobRestResource.class);
	
	private HsspQueue queue;

	public JobRestResource(WicketApplication application) {
		
		queue = application.getHsspQueue();
	}

	@MethodMapping(value="/custom", httpMethod=HttpMethod.POST)
	public String custom() {
		
		// getPostParameters doesn't work for some reason
		IRequestParameters p = RequestCycle.get().getRequest().getRequestParameters();

	    StringValue pdbfile = p.getParameterValue("pdbfile");
	    if(pdbfile.toString()==null) {
	    	return "";
	    }
		
		return queue.submit(pdbfile.toString());
	}
	
	@MethodMapping(value = "/status/{jobid}", httpMethod=HttpMethod.GET)
	public String status(String jobid) {
		
		return queue.getStatus(jobid).toString();
	}
}
