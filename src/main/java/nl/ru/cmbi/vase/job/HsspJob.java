package nl.ru.cmbi.vase.job;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.ru.cmbi.hssp.Hsspsoap;
import nl.ru.cmbi.hssp.HsspsoapPortType;
import nl.ru.cmbi.vase.tools.util.Config;

public class HsspJob implements Runnable {
	
	private static Logger log = LoggerFactory.getLogger(HsspJob.class);

	private JobStatus status = JobStatus.QUEUED;
	
	private UUID uuid;
	
	public UUID getUUID() {
		
		return uuid;
	}
	public JobStatus getStatus() {
		
		return status;
	}
	
	private static final File cacheDir = new File(Config.properties.getProperty("hsspcache"));
	
	public File getHsspFile() {
		
		return new File(cacheDir, uuid.toString()+".hssp.bz2");
	}
	public File getPdbFile() {
		
		return new File(cacheDir, uuid.toString()+".pdb.gz");
	}
	public File getErrorFile() {
		
		return new File(cacheDir, uuid.toString()+".err");
	}
	
	public HsspJob(File pdbfile) {
		
		init();
		
		uuid = UUID.fromString(pdbfile.getName().replaceAll("\\..*", ""));
		
		if(getHsspFile().isFile())
			status = JobStatus.FINISHED;
	}
	
	public HsspJob(String pdbContents) {
		
		init();
		
		uuid = UUID.nameUUIDFromBytes(pdbContents.getBytes());
		
		try {
			
			Writer pdbWriter = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(getPdbFile())));
			pdbWriter.write(pdbContents);
			pdbWriter.close();
			
		} catch (Exception e) {
			
			log.error(e.getMessage(),e);
		}
	}
	
	private void init() {
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			
		    public void run() {
		    	
		    	HsspJob.this.onShutdown();
		    }
		});
	}
	
	private Process mkhsspProcess = null;
	
	private void onShutdown() {
		
		if( mkhsspProcess!=null ) {
			
			log.warn("shutdown killing mkhssp for "+uuid.toString());
			
			mkhsspProcess.destroy();

			File outputFile = getHsspFile();
			if(outputFile.isFile())
				outputFile.delete();
		}
	}
	
	private void localBuild() throws IOException {
		
		String dbString = "";
		for(String path : Config.properties.getProperty("databanks").split("\\s+")) {
			dbString += " -d "+path;
		}
		
		File outputFile = getHsspFile();

		String processString = new String( String.format("%s -o %s -a 1 -m 2500 --fetch-dbrefs %s %s",
				Config.properties.getProperty("mkhssp"),
				outputFile.getPath(),
				dbString,
				getPdbFile().getPath()) );
		
		mkhsspProcess = Runtime.getRuntime().exec(processString);

		try {
			
	    	mkhsspProcess.waitFor();
			
		} catch (InterruptedException e) {
			
			log.error("error waiting for job:"+e.getMessage(),e);
			onShutdown();
		}
	    
	    if(outputFile.isFile() && outputFile.length()>0)
	    	
	    	status = JobStatus.FINISHED;
	    
	    else {
	    	
	    	status = JobStatus.ERROR;
	    	
	    	if(outputFile.isFile())
	    		outputFile.delete();
	    	
	    	toErrorFile(mkhsspProcess.getErrorStream());
	    }
	}
	
	private void remoteBuild() throws IOException, MalformedURLException {
		
		StringWriter pdbStringWriter = new StringWriter();
		IOUtils.copy( new GZIPInputStream(new FileInputStream(getPdbFile())), pdbStringWriter);
		
        URL url = new URL(Config.properties.getProperty("hsspws"));
        log.info("Setting Hsspsoap endpoint to: {}", url);

        final HsspsoapPortType hsspsoap = new Hsspsoap(url).getHsspsoap();

        final Map<String, Object> requestContext = ((BindingProvider) hsspsoap).getRequestContext();
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url.toString());
        
        
    	String hssp = hsspsoap.getHSSPForPDBFile(pdbStringWriter.toString());
        
        Writer hsspWriter = new FileWriter(getHsspFile());
        hsspWriter.write(hssp);
        hsspWriter.close();
        
    	status = JobStatus.FINISHED;
	}

	@Override
	public void run() {
		
		File outputFile = getHsspFile();
		
		status = JobStatus.RUNNING;
		
		try {
			
			if(Config.properties.containsKey("hsspws"))
				remoteBuild();
			else if(Config.properties.containsKey("mkhssp"))
				localBuild();
			else
				log.error(uuid.toString() + ": no possibility to build hssp");
			
		} catch (Exception e) {
        	
	    	status = JobStatus.ERROR;
	    	
	    	toErrorFile(e.getMessage());
		}
	}
	

	private void toErrorFile(InputStream is) {

		try {
			
			FileWriter writer = new FileWriter(getErrorFile());
			IOUtils.copy( mkhsspProcess.getErrorStream(), writer);
			writer.close();
	    	
		} catch (IOException ex) { 
			
			log.error("unable to write error file: "+ex.getMessage(),ex);
		}
	}
	
	private void toErrorFile(String msg) {
		
		toErrorFile( new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8)) );
	}
}
