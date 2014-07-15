package nl.ru.cmbi.vase.web.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import nl.ru.cmbi.vase.data.VASEDataObject;
import nl.ru.cmbi.vase.job.HsspJob;
import nl.ru.cmbi.vase.job.HsspQueue;
import nl.ru.cmbi.vase.parse.VASEXMLParser;
import nl.ru.cmbi.vase.tools.util.Config;
import nl.ru.cmbi.vase.web.WicketApplication;
import nl.ru.cmbi.vase.web.page.AlignmentPage;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.rest.annotations.MethodMapping;
import org.wicketstuff.rest.annotations.parameters.RequestBody;
import org.wicketstuff.rest.annotations.parameters.RequestParam;
import org.wicketstuff.rest.contenthandling.RestMimeTypes;
import org.wicketstuff.rest.resource.gson.GsonRestResource;
import org.wicketstuff.rest.utils.http.HttpMethod;

public class JobRestResource extends GsonRestResource {
	
	private static final Logger log = LoggerFactory.getLogger(JobRestResource.class);
	
	private HsspQueue queue;

	public JobRestResource(WicketApplication application) {
		
		queue = application.getHsspQueue();
	}

	@MethodMapping(value="/custom", httpMethod=HttpMethod.POST, produces = RestMimeTypes.TEXT_PLAIN)
	public String custom() {
		
		// getPostParameters doesn't work for some reason
		IRequestParameters p = RequestCycle.get().getRequest().getRequestParameters();

	    StringValue pdbfile = p.getParameterValue("pdbfile");
	    if(pdbfile.toString()==null) {
	    	
			log.error("pdbfile parameter not set");

			throw new AbortWithHttpErrorCodeException(404);
	    }
		
		return queue.submit(pdbfile.toString());
	}
	
	@MethodMapping(value = "/status/{jobid}", httpMethod=HttpMethod.GET, produces = RestMimeTypes.TEXT_PLAIN)
	public String status(String jobid) {
		
		return queue.getStatus(jobid).toString();
	}
	
	@MethodMapping(value = "/structure/{id}", httpMethod=HttpMethod.GET, produces = RestMimeTypes.TEXT_PLAIN)
	public String structure(String id) {
		
		File	xmlFile = new File(Config.getCacheDir(),id+".xml.gz"),
				pdbFile = new File(Config.getHSSPCacheDir(),id+".pdb.gz");
		
		try {
			
			if(xmlFile.isFile()) {
			
				VASEDataObject data = VASEXMLParser.parse( new GZIPInputStream( new FileInputStream(xmlFile) ) );
			
				return data.getPdbContents();
			}
			else if(pdbFile.isFile()) {
				
				StringWriter pdbWriter = new StringWriter();
				IOUtils.copy(new GZIPInputStream( new FileInputStream(pdbFile) ), pdbWriter, "UTF-8");
				pdbWriter.close();
				return pdbWriter.toString();
			}
			else {
				
				log.error("no structure file for "+id);

				throw new AbortWithHttpErrorCodeException(404);
			}
			
		} catch (Exception e) {
			
			log.error("structure "+id+": "+e.getMessage());
			
			throw new AbortWithHttpErrorCodeException(404);
		}
	}
}
