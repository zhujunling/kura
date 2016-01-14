package org.eclipse.kura.test.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlFileUtil {

	private static final Logger s_logger = LoggerFactory.getLogger(ControlFileUtil.class);
	
	private static ControlFileUtil s_instance = null;
	
	private static String controlFileName;
	
	private ControlFileUtil() {
		
	}
	
	private ControlFileUtil(String fileName) {
		controlFileName = fileName;
		File file = new File(controlFileName);
		try {
			file.getParentFile().mkdirs();
			file.createNewFile();
		} catch (IOException e) {
			s_logger.error("Error creating control file.");
		}
	}
	
	public static ControlFileUtil getInstance(String fileName) {
		if (s_instance == null) {
			return new ControlFileUtil(fileName);
		}
		else return s_instance;
	}
	
	public String getControlLine() {
		BufferedReader br = null;
		String controlLine = null;
		
		try {
			br = new BufferedReader(new FileReader(controlFileName));
			controlLine = br.readLine();
		} catch (FileNotFoundException e) {
			s_logger.error("Error opening control file.", e);
		} catch (IOException e) {
			s_logger.error("Error reading control file.", e);
		} finally {
			try {
				br.close();
			} catch (IOException ioe) {
				s_logger.error("Error closing control file.", ioe);
			}
		}
		return controlLine;
	}
	
	public void writeControlLine(String controlLine) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(controlFileName, "UTF-8");
			pw.write(controlLine);
			pw.close();
		} catch (FileNotFoundException e) {
			s_logger.error("Error writing control file.");
		} catch (UnsupportedEncodingException e) {
			s_logger.error("Error writing control file.");
		} finally {
			pw.close();
		}
	}
}
