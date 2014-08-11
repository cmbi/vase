package nl.ru.cmbi.vase.web.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;

import nl.ru.cmbi.vase.data.VASEDataObject;
import nl.ru.cmbi.vase.job.HsspJob;
import nl.ru.cmbi.vase.job.HsspQueue;
import nl.ru.cmbi.vase.parse.StockholmParser;
import nl.ru.cmbi.vase.parse.VASEXMLParser;
import nl.ru.cmbi.vase.tools.util.Config;
import nl.ru.cmbi.vase.tools.util.Utils;
import nl.ru.cmbi.vase.web.WicketApplication;
import nl.ru.cmbi.vase.web.page.AlignmentPage;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
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
	
	//private HsspQueue queue;
	
	private String hsspRestURL = "http://cmbi23.cmbi.ru.nl:8013/api";

	public JobRestResource(WicketApplication application) {
		
		/*if(Config.isXmlOnly()) {
			
			queue = null ;
		}
		else {
			queue = application.getHsspQueue();
		}*/
	}

	@MethodMapping(value="/custom", httpMethod=HttpMethod.POST, produces = RestMimeTypes.TEXT_PLAIN)
	public String custom() {
		
		if(Config.isXmlOnly()) {
			
			// hssp job submission is not allowed if hssp is turned off
			throw new AbortWithHttpErrorCodeException(404);
		}
		
		// getPostParameters doesn't work for some reason
		IRequestParameters p = RequestCycle.get().getRequest().getRequestParameters();

	    StringValue pdbfile = p.getParameterValue("pdbfile");
	    if(pdbfile.toString()==null) {
	    	
			log.error("pdbfile parameter not set");

			throw new AbortWithHttpErrorCodeException(404);
	    }
		
		ClientResource resource = new ClientResource(hsspRestURL+"/create/hssp/from_pdb/");
		resource.setMethod(Method.POST);
        resource.getReference().addQueryParameter("format", "json");

		try {
	        JSONObject obj = new JSONObject();
			obj.put("pdb_content",pdbfile.toString());
			
			StringRepresentation stringRep = new StringRepresentation(obj.toString());
	        stringRep.setMediaType(MediaType.APPLICATION_JSON);

	        resource.post(stringRep); 
			
		} catch (JSONException e) {
			log.error("json exception");
		}
		
		if (resource.getStatus().isSuccess()) {
			
			try {
				return resource.get().getText();
				
			} catch (IOException e) {
				
				log.error("IOException");
				throw new AbortWithHttpErrorCodeException(404);
			}
			
	    } else {
	    	
			log.error("create/hssp/from_pdb: unsuccesful call");

			throw new AbortWithHttpErrorCodeException(404);
	    }
	}
	
	@MethodMapping(value = "/status/{jobid}", httpMethod=HttpMethod.GET, produces = RestMimeTypes.TEXT_PLAIN)
	public String status(String jobid) {

		ClientResource resource = new ClientResource(hsspRestURL+"/job/hssp_from_pdb/"+jobid+"/status/");
		resource.setMethod(Method.GET);
		
		if(Config.isXmlOnly()) {
			
			// hssp job submission is not allowed if hssp is turned off
			throw new AbortWithHttpErrorCodeException(404);
		}
		
		try {
			
			JsonRepresentation represent = new JsonRepresentation(resource.get());
			
			JSONObject object = represent.getJsonObject();
            if (object != null) {
            	return object.getString("status");
            }
            else throw new AbortWithHttpErrorCodeException(404);

		} catch (Exception e) {
			
			log.error(e.getMessage(),e);
			throw new AbortWithHttpErrorCodeException(404);
		}
	}
	
	@MethodMapping(value = "/hssp/{id}", httpMethod=HttpMethod.GET, produces = RestMimeTypes.TEXT_PLAIN)
	public String hssp(String jobid) {
		
		File hsspFile = new File(Config.getHSSPCacheDir(),jobid+".hssp.bz2");
		
		ClientResource resource = new ClientResource(hsspRestURL+"/job/hssp_from_pdb/"+jobid+"/result/");
		resource.setMethod(Method.GET);
		
		try{
			
			if(!hsspFile.isFile()) {
			
				OutputStream hsspOut = new BZip2CompressorOutputStream(new FileOutputStream(hsspFile));
				resource.get().write(hsspOut);
				hsspOut.close();
			}
			
			StringWriter hsspWriter = new StringWriter();
			IOUtils.copy(new GZIPInputStream( new FileInputStream(hsspFile) ), hsspWriter, "UTF-8");
			hsspWriter.close();
			
			return hsspWriter.toString();
			
		} catch (IOException e) {
			
			log.error("IOException");
			throw new AbortWithHttpErrorCodeException(404);
		}
	}
		
	
	@MethodMapping(value = "/structure/{id}", httpMethod=HttpMethod.GET, produces = RestMimeTypes.TEXT_PLAIN)
	public String structure(String id) {
		
		Matcher mpdb = StockholmParser.pPDBAC.matcher(id);
		
		File	xmlFile = new File(Config.getCacheDir(),id+".xml.gz"),
				pdbFile = new File(Config.getHSSPCacheDir(),id+".pdb.gz");
		
		try {

			if(mpdb.matches()) {
				
				URL url = Utils.getRcsbURL(mpdb.group(1));
				
				StringWriter pdbWriter = new StringWriter();
				IOUtils.copy(url.openStream(), pdbWriter, "UTF-8");
				pdbWriter.close();
				
				return pdbWriter.toString();
			}
			else if(xmlFile.isFile()) {
			
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
