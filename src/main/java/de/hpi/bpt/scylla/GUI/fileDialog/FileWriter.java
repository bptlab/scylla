package de.hpi.bpt.scylla.GUI.fileDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides an interface to writing on the file system. <br>
 * It can be used e.g. by the webSwing javascript code to upload files to the server. <br>
 * Usage: <br>
 * A filewriter normally references a certain {@link #directory} where files can be written. <br>
 * To write a file, the recommended way is to request a new {@link WriteJob} with {@link #requestNewJob(String)}. <br>
 * This job allows to write to a certain file multiple times before finishing the job.
 * @author Leon Bein
 *
 */
public class FileWriter{
	
	/**Used to keep references on created non-finished write jobs, to prevent them from being garbage collected, as the only reference on them might be inside javascript.*/
	private Set<WriteJob> referenceSet;
	/**Path to the "root" directory of this file writer. Also prevents writing to another directory than this or one of this' subdirectories.*/
	private String directory;
	
	/**
	 * Creates a new file writer in the given directory
	 * @param dir : Sets {@link #directory}
	 */
	public FileWriter(String dir) {
		directory = dir;
		referenceSet = new HashSet<>();
	}
	
	/**
	 * Creates a file writer in C:
	 */
	@Deprecated
	public FileWriter() {
		this("C:/");
	}
	
	/**
	 * Tests the type of a given object and prints it to the error log
	 * @param o
	 */
	@Deprecated
	public void testType(Object o) {
		System.err.println("The type is: "+o.getClass());
	}
	
	/**
	 * Directly writes an empty file to given path
	 * @param s
	 */
	@Deprecated
	public void testwrite(String s) {
		if(s == null) {
			System.err.println("File path is null!");
			return;
		}
		try {
			write((String)s,new byte[0],false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes a String to a given file path
	 * @param path : Path of file to write
	 * @param d : Expected to be an ISO-8895-1 coded String
	 * @param override
	 * @throws IOException
	 */
	@Deprecated
	public void write(String path, Object d/*byte[] data*/, boolean override) throws IOException {
		System.err.println("The type of d is: "+d.getClass());
		System.err.println("The length of d is: "+d.toString().length());
		String fend = path.substring(path.lastIndexOf("."),path.length()-1);
		String copypath = directory+path.replace(fend, "_copy"+fend);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] data = ((String)d).getBytes(Charset.forName("ISO-8859-1"));
		baos.write(data);
		byte[] sink = baos.toByteArray();
		try (FileOutputStream fos = new FileOutputStream(copypath)) {
			System.err.println("Writing to: "+copypath);
			   fos.write(sink);
		}
	}
	
	/**
	 * Creates and opens a new job for the given file path
	 * @param fileName : Path to the file, see also {@link WriteJob#filePath}
	 * @return New job, ready to write
	 */
	public WriteJob requestNewJob(String fileName) {
		WriteJob wj = new WriteJob(fileName);
		wj.open();
		return wj;
	}
	
	public static String addCopySuffix(String fileName) {
		String copypath = new StringBuilder(fileName).insert(fileName.lastIndexOf("."),"_copy").toString();		
		return copypath;
	}
	
	/**
	 * A WriteJob represents the process of writing to a single file. <br>
	 * Its lifecycle starts with {@link #open()}ing the output stream ({@link #outputStream}). <br>
	 * It is then possible to repeatedly write bytes to the stream by either by <br>
	 * - Directly passing a byte array ({@link #writeBytes(byte[])}) <br>
	 * - Passing an ISO-8859-1 encoded String that is then decoded to byte array ({@link #writeISO_8859_1(String)}) <br>
	 * - Passing a base64 encoded String that is also decoded ({@link #writeBase64(String)}) <br>
	 * The lifecycle ends with writing the collected data to file and closing all streams ({@link #finish()})
	 * @author Leon Bein
	 *
	 */
	public class WriteJob {
		
		/** Path to file (relative to parent FileWriter's root directory)*/
		private String filePath;
		/**Main output stream*/
		private ByteArrayOutputStream outputStream;
		
		/**
		 * Creates a WriteJob for the given file path
		 * @param fileName : Sets {@link #filePath}
		 */
		private WriteJob(String fileName) {
			this.filePath = directory+fileName;
			referenceSet.add(this);
		}
		
		/**
		 * Opens all necessary streams to write to
		 */
		public void open() {
			outputStream = new ByteArrayOutputStream();
		}
		
		/**
		 * Writes bytes to output stream
		 * @param ba : Array of bytes to be written
		 * @throws IOException : If an I/O error occurs
		 * @see {@link java.io.OutputStream#write(byte[] b)}
		 */
		public synchronized void writeBytes(byte[] ba) throws IOException {
			outputStream.write(ba);
		}		
		
		/**
		 * Decodes an ISO-8859-1 encoded string to byte array and writes it with {@link #writeBytes(byte[])}
		 * @param s : ISO-8859-1 encoded string to be written
		 */
		public void writeISO_8859_1(String s) throws IOException {
			byte[] data = s.getBytes(Charset.forName("ISO-8859-1"));
			writeBytes(data);
		}
		
		/**
		 * Decodes an base64 encoded string to byte array and writes it with {@link #writeBytes(byte[])}
		 * @param s : Base64 encoded string to be written
		 */
		public void writeBase64(String data) throws IOException {
			try {
				byte[] data2 = Base64.getDecoder().decode(data);
				writeBytes(data2);
			}catch(IllegalArgumentException exc) {
				System.err.println("Error when trying to base64 decode data:");
				exc.printStackTrace();
			}
		}
		
		/**
		 * Closes output stream, writes to file, removes this object from reference set.
		 * @throws IOException : When an I/O error occurs
		 */
		public String finish() throws IOException{
			byte[] sink = outputStream.toByteArray();
			while((new File(filePath)).exists()) {
				filePath = addCopySuffix(filePath);
			}
			try (FileOutputStream fos = new FileOutputStream(filePath)) {
				System.err.println("Writing to: "+filePath);
				fos.write(sink);
			}finally {
				outputStream.close();
				referenceSet.remove(this);
			}
			return filePath;
		}
		
	}

}
