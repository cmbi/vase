package nl.ru.cmbi.hssp.tools.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

/**
 * utility class handling the reading and writing from/to files and some other
 * general utils.
 * 
 * @author bvroling
 * 
 */
public final class ToolBox {
	/** constructor. */
	private ToolBox() {
	}

	/** logger. */
	private static final Logger LOG = Logger.getLogger(ToolBox.class);

	static public InputStream getStringAsInputStream(String text) {
		/*
		 * Convert String to InputString using ByteArrayInputStream class. This
		 * class constructor takes the string byte array which can be done by
		 * calling the getBytes() method.
		 */
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(text.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.error(e);
		}
		return is;
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		// Get the size of the file
		long length = file.length();
		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}
		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];
		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}
		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "
					+ file.getName());
		}
		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	static public List<String> getInsideContents(String fileName) {
		ClassLoader loader = ToolBox.class.getClassLoader();
		InputStream stream = loader.getResourceAsStream(fileName);

		if (stream == null) {
			loader = ClassLoader.getSystemClassLoader();
			stream = loader.getResourceAsStream(fileName);
		}
		// stream = loader.getResourceAsStream("spring.xml");
		StringBuffer contents = new StringBuffer();
		List<String> lines = new ArrayList<String>();
		BufferedReader input = null;
		try {
			input = new BufferedReader(new InputStreamReader(stream));
			String line = null; // not declared within while loop
			while ((line = input.readLine()) != null) {
				lines.add(line);
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}
		} catch (Exception e) {
			LOG.fatal(e);
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (Exception e) {
				LOG.warn(e);
			}
		}
		return lines;
	}

	/**
	 * Fetch the entire contents of a text file, and return it in a List of
	 * Strings.
	 * 
	 * @param fileName
	 *            file to read
	 * @throws IOException
	 *             thrown when file cannot be read
	 * @return list of lines
	 */
	@SuppressWarnings("unchecked")
	public static List<String> getContentsFromFile(final String fileName)
			throws IOException {
		List<String> lines;
		File file = new File(fileName);
		lines = FileUtils.readLines(file);
		file = null;
		LOG.info("Succesfully read file " + fileName);
		return lines;
	}

	public static StringBuilder getStringBuilderFromFile(final String fileName)
			throws IOException {
		// int STRINGBUILDER_SIZE = 403228383; // big!
		File file = new File(fileName);
		LineIterator it = FileUtils.lineIterator(file);
		StringBuilder sb = new StringBuilder(); // STRINGBUILDER_SIZE
		while (it.hasNext()) {
			sb.append(it.nextLine());
			sb.append("\n");
		}
		it.close();
		return sb;
	}

	/**
	 * appends a string to a file.
	 * 
	 * @param filename
	 *            file to append to
	 * @param text
	 *            text to append
	 * @param append
	 *            append text or start empty
	 * @throws IOException
	 *             thrown when something goes wrong
	 */
	public static void writeToFile(final String filename, final String text,
			final boolean append) throws IOException {
		if (text == null) {
			LOG.warn("writing null to " + filename);
		}
		BufferedWriter out = new BufferedWriter(
				new FileWriter(filename, append));
		out.write(text);
		out.close();
		LOG.debug("written text to " + filename);
	}

	/**
	 * get a buffered reader for reading large files.
	 * 
	 * @param fileName
	 *            file to wrap with a buffered reader
	 * @return buffered reader object
	 */
	public static BufferedReader getBufferedReader(final String fileName) {
		FileInputStream fstream = null;
		try {
			fstream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			LOG.error(e);
			e.printStackTrace();
		}
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		return br;
	}

	public static BufferedReader getBufferedReader(final File hmmResultsFile) {
		FileInputStream fstream = null;
		try {
			fstream = new FileInputStream(hmmResultsFile);
		} catch (FileNotFoundException e) {
			LOG.error(e);
			e.printStackTrace();
		}
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		return br;
	}

	/**
	 * appends a list of strings to a file.
	 * 
	 * @param filename
	 *            file to append to
	 * @param text
	 *            list of strings to append
	 * @param append
	 *            append or start empty
	 * @param separator
	 *            separator
	 * @throws IOException
	 *             thrown when something goes wrong
	 */
	public static void writeToFile(final String filename,
			final List<String> text, final boolean append,
			final String separator) throws IOException {

		// empty file
		if (append == false) {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename,
					false));
			out.write("");
			out.close();
		}

		StringBuilder stringBuilder = new StringBuilder();
		int count = 1;
		for (String line : text) {
			stringBuilder.append(line + separator);
			count++;

			if (count % 5000 == 0) { // good for small memory usage
				String textToWrite = stringBuilder.toString();

				BufferedWriter out = new BufferedWriter(new FileWriter(
						filename, true));
				out.write(textToWrite);
				out.close();
				LOG.debug("written text to " + filename);
				stringBuilder = new StringBuilder();
			}
		}
		String textToWrite = stringBuilder.toString();
		BufferedWriter out = new BufferedWriter(new FileWriter(filename, true));
		out.write(textToWrite);
		out.close();
		LOG.debug("written text to " + filename);
	}

	/**
	 * removes empty strings from a list of strings.
	 * 
	 * @param lines
	 *            lines to be filtered
	 * @return lines without empty lines
	 */
	public static List<String> removeEmptyStrings(final List<String> lines) {
		while (lines.remove("")) {
		}
		return lines;
	}

	// public static List<String> convertMsfToFasta(String fileName) {
	// try {
	// //prepare a BufferedReader for file io
	// BufferedReader br = new BufferedReader(new FileReader(fileName));
	//       
	// String format = "msf";
	// String alphabet = "protein";
	//       
	// /*
	// * get a Sequence Iterator over all the sequences in the file.
	// * SeqIOTools.fileToBiojava() returns an Object. If the file read
	// * is an alignment format like MSF and Alignment object is returned
	// * otherwise a SequenceIterator is returned.
	// */
	// SequenceIterator iter =
	// (SequenceIterator)SeqIOTools.fileToBiojava(format,alphabet, br);
	// }
	// catch (FileNotFoundException ex) {
	// //can't find file specified by args[0]
	// ex.printStackTrace();
	// }catch (BioException ex) {
	// //error parsing requested format
	// ex.printStackTrace();
	// }
	// return null;
	// }

	public static String slugify(final String text) {
		String result = text.replaceAll("[^\\w\\s-]", "").trim().toLowerCase();
		result = result.replaceAll("[-\\s]", "-");
		return result;
	}

	public static List<String> convertListObjectToListString(final List objects) {
		List<String> stringList = new ArrayList<String>();
		for (Object object : objects) {
			stringList.add(object.toString());
		}
		// TODO Auto-generated method stub
		return stringList;
	}

	public static List<Integer> convertStringToListInteger(final String list,
			String sep) {
		List<Integer> numbers = new ArrayList<Integer>();
		String[] parts = list.split(sep);
		for (String p : parts) {
			try {
				numbers.add(Integer.parseInt(p));
			} catch (Exception e) {
				LOG.error(e);
			}
		}
		// TODO Auto-generated method stub
		return numbers;
	}

	public static StringBuilder readFileFromUrl(String urlString) {
		StringBuilder lines = new StringBuilder();
		try {
			URL url = new URL(urlString);
			BufferedReader in = new BufferedReader(new InputStreamReader(url
					.openStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				lines.append(inputLine);
				lines.append("\n");
			}
			in.close();

		} catch (MalformedURLException e) {
			LOG.error(e);
		} catch (IOException e) {
			LOG.error(e);
		}
		return lines;
	}

	public static StringBuilder readFile(String fileName) {
		StringBuilder lines = new StringBuilder();
		try {
			BufferedReader in = getBufferedReader(fileName);

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				lines.append(inputLine);
				lines.append("/n");
			}
			in.close();

		} catch (IOException e) {
			LOG.error(e);
		}
		return lines;
	}

	public static void writeBinaryToFile(byte[] binaryData, String fileName) {
		try {
			OutputStream output = null;
			try {
				output = new FileOutputStream(fileName);
				output.write(binaryData);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				output.close();
			}
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	public static Object readObjectFromFile(String fileName) {
		Object object = null;
		try {
			InputStream input = new FileInputStream(fileName);
			ObjectInputStream objeInput = new ObjectInputStream(input);
			object = objeInput.readObject();
			input.close();
		} catch (FileNotFoundException e) {
			LOG.info(e);
		} catch (IOException e) {
			LOG.info(e);
		} catch (ClassNotFoundException e) {
			LOG.info(e);
		}
		return object;
	}
}
