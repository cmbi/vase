/**
 * Copyright 2014 CMBI (contact: <Coos.Baakman@radboudumc.nl>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.ru.cmbi.vase.web.rest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import nl.ru.cmbi.vase.data.VASEDataObject;
import nl.ru.cmbi.vase.parse.StockholmParser;
import nl.ru.cmbi.vase.parse.VASEXMLParser;
import nl.ru.cmbi.vase.tools.util.Config;
import nl.ru.cmbi.vase.tools.util.Utils;
import nl.ru.cmbi.vase.web.WicketApplication;
import nl.ru.cmbi.vase.web.page.AlignmentPage;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
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
import org.restlet.representation.Representation;
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
		
		if(Config.isXmlOnly() || !Config.hsspPdbCacheIsSet()) {
			
			// hssp job submission is not allowed if hssp is turned off
			throw new AbortWithHttpErrorCodeException(HttpURLConnection.HTTP_NOT_FOUND);
		}
		
		// getPostParameters doesn't work for some reason
		IRequestParameters p = RequestCycle.get().getRequest().getRequestParameters();

	    StringValue pdbContents = p.getParameterValue("pdbfile");
	    if(pdbContents.toString()==null) {
	    	
			log.error("pdbfile parameter not set");

			throw new AbortWithHttpErrorCodeException(HttpURLConnection.HTTP_BAD_REQUEST);
	    }
	    
	    Form form = new Form();
	    form.add("pdb_content", pdbContents.toString());

	    String url = hsspRestURL + "/create/hssp/from_pdb/";
	    ClientResource resource = new ClientResource(url);

	    Representation rep = null;
	    try {
	    	rep = resource.post(form);
	      
	      	String content = rep.getText();
		    
		    JSONObject output=new JSONObject( content );
		    String jobID = output.getString("id");

			File pdbFile = new File(Config.getHSSPCacheDir(),jobID+".pdb.gz");
			
			OutputStream pdbOut = new GZIPOutputStream(new FileOutputStream(pdbFile));
		    IOUtils.write(pdbContents.toString(),pdbOut);
		    pdbOut.close();
		    
		    return jobID;
	      
	    } catch (ResourceException e) {
	    	log.error("HSSP REST service error: " + e.toString());
	    	throw new AbortWithHttpErrorCodeException(HttpURLConnection.HTTP_INTERNAL_ERROR);
	    } catch (JSONException e) {
	    	log.error("json error: " + e.toString());
	    	throw new AbortWithHttpErrorCodeException(HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (IOException e) {
			log.error("io error: " + e.toString());
			throw new AbortWithHttpErrorCodeException(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	    
    	/*CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    	
    	try {
    		JSONObject input = new JSONObject();
    		input.put("pdb_content", pdbContents.toString());
    		
    	    HttpPost request = new HttpPost(hsspRestURL+"/create/hssp/from_pdb/");
    	    StringEntity inputEntity = new StringEntity(input.toString(),"UTF-8");
    	    inputEntity.setContentType("application/json; charset=UTF-8");
    	    inputEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
    	    request.setHeader("Content-type", "application/json");
    	    request.setEntity(inputEntity);
    	    
    	    HttpResponse response = httpClient.execute(request);
    	    
    	    HttpEntity outputEntity = response.getEntity();
    	    
    	    if (response.getStatusLine().getStatusCode() !=  HttpURLConnection.HTTP_OK) {
    	    	
    	    	throw new Exception(response.getStatusLine().toString());
    	    }
    	    
			StringWriter writer = new StringWriter();
			IOUtils.copy( outputEntity.getContent(), writer);
			writer.close();
			
    	    httpClient.close();

			JSONObject output=new JSONObject( writer.toString() );
			return output.getString("id");
    	    
    	// handle response here...
    	} catch (Exception e) {
	    	
	    	log.error(e.getMessage(),e);
			throw new AbortWithHttpErrorCodeException(HttpURLConnection.HTTP_INTERNAL_ERROR);
    	}*/
	}
	
	@MethodMapping(value = "/status/{jobid}", httpMethod=HttpMethod.GET, produces = RestMimeTypes.TEXT_PLAIN)
	public String status(String jobid) {

		if( Config.isXmlOnly() || !Config.hsspPdbCacheIsSet()) {
			
			// hssp job submission is not allowed if hssp is turned off
			throw new AbortWithHttpErrorCodeException(HttpURLConnection.HTTP_NOT_FOUND);
		}
		
		try {
			URL url = new URL(hsspRestURL+"/job/hssp_from_pdb/"+jobid+"/status/");
			
			StringWriter writer = new StringWriter();
			IOUtils.copy( url.openStream(), writer);
			writer.close();
			
			JSONObject output=new JSONObject( writer.toString() );
			
			return output.getString("status");
			
		} catch (Exception e) {
	    	
	    	log.error(e.getMessage(),e);
			throw new AbortWithHttpErrorCodeException(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}
	
	@MethodMapping(value = "/hsspresult/{id}", httpMethod=HttpMethod.GET, produces = RestMimeTypes.TEXT_PLAIN)
	public String hssp(String id) {
		
		if( Config.isXmlOnly() ) {

			// hssp job submission is not allowed if hssp is turned off
			throw new AbortWithHttpErrorCodeException(HttpURLConnection.HTTP_NOT_FOUND);
		}
		
		try {
			URL url = new URL(hsspRestURL+"/job/hssp_from_pdb/"+id+"/result/");
			
			Writer writer = new StringWriter();
			IOUtils.copy( url.openStream(), writer);
			writer.close();
			
			JSONObject output=new JSONObject( writer.toString() );
			
			return output.getString("result");
			
		} catch (Exception e) {
			
			log.error(e.getMessage(),e);
			throw new AbortWithHttpErrorCodeException(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}
		
	
	@MethodMapping(value = "/structure/{id}", httpMethod=HttpMethod.GET, produces = RestMimeTypes.TEXT_PLAIN)
	public String structure(String id) {
		
		try {
			
			Matcher mpdb = StockholmParser.pPDBAC.matcher(id);
			if(mpdb.matches()) {
				
				URL url = Utils.getRcsbURL(mpdb.group(1));
				
				StringWriter pdbWriter = new StringWriter();
				IOUtils.copy(url.openStream(), pdbWriter, "UTF-8");
				pdbWriter.close();
				
				return pdbWriter.toString();
			}

			File xmlFile = new File(Config.getCacheDir(),id+".xml.gz");
			if(xmlFile.isFile()) {
			
				VASEDataObject data = VASEXMLParser.parse( new GZIPInputStream( new FileInputStream(xmlFile) ) );
			
				return data.getPdbContents();
			}
			if(Config.hsspPdbCacheIsSet()) {
				
				File pdbFile = new File(Config.getHSSPCacheDir(),id+".pdb.gz");
				
				if(pdbFile.isFile()) {
				
					StringWriter pdbWriter = new StringWriter();
					IOUtils.copy(new GZIPInputStream( new FileInputStream(pdbFile) ), pdbWriter, "UTF-8");
					pdbWriter.close();
					
					return pdbWriter.toString();
				}
			}
				
			log.error("no structure file for "+id);
			throw new AbortWithHttpErrorCodeException(HttpURLConnection.HTTP_NOT_FOUND);
			
		} catch (Exception e) {
			
			log.error("structure "+id+": "+e.getMessage(),e);
			
			throw new AbortWithHttpErrorCodeException(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}
}
