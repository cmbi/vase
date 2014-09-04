package nl.ru.cmbi.vase.tools.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.IOUtils;

public class DataProvider {
	
	public enum CompressionType{ NONE, BZ2, GZ };
	
	private URL url;
	private File file;
	private CompressionType compression = CompressionType.NONE;
	
	
	public DataProvider(File file) {
		
		this.file=file;
		this.url =null;
	}

	public DataProvider(URL url) {
		
		this.url = url;
		this.file=null;
	}

	public DataProvider(File file,CompressionType compression) {

		this.file=file;
		this.url =null;
		this.compression=compression;
	}
	
	public URL getURL() {
		
		return this.url;
	}
	
	public InputStream getInputstream() throws IOException {
		
		if(this.url!=null) {
			
			return url.openStream();
		}
		else if(this.file!=null) {
			
			switch(this.compression) {
			case BZ2:
				return new BZip2CompressorInputStream(new FileInputStream(this.file));
			case GZ:
				return new GZIPInputStream(new FileInputStream(this.file));
			default:
				return new FileInputStream(this.file);
			}
		}
		else return null;
	}

	public String getContents() throws IOException {
		
		StringWriter writer = new StringWriter();
		InputStream in = getInputstream();
		IOUtils.copy(in, writer);
		in.close();
		
		return writer.toString();
	}
}
