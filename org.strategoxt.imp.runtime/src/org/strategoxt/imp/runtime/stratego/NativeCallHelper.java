package org.strategoxt.imp.runtime.stratego;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.strategoxt.imp.runtime.Environment;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class NativeCallHelper {	
	public int call(String[] commandArgs, File workingDir, OutputStream outStream, OutputStream errorStream)
			throws InterruptedException, IOException {
		
		Process process = Runtime.getRuntime().exec(commandArgs, null, workingDir);
		Thread t1 = new StreamCopier(process.getInputStream(), outStream);
		Thread t2 = new StreamCopier(process.getErrorStream(), errorStream);
		t1.start();
		t2.start();
		
		int result = process.waitFor();
		t1.join();
		t2.join();
	
		return result;
	}
}

class StreamCopier extends Thread {
	private final InputStream input;
	private final OutputStream output;

	public StreamCopier(InputStream input, OutputStream output) {
		this.input = input;
		this.output = output;
	}

	@Override
	public synchronized void run() {
		try {
			InputStreamReader streamReader = new InputStreamReader(input);
			BufferedReader reader = new BufferedReader(streamReader);
			PrintWriter writer = new PrintWriter(output);
			
			// NOTE: This might block if exceptionally long lines are printed
			String line;
			while ((line = reader.readLine()) != null) {
				writer.println(line);
			}
			
			reader.close();
			writer.flush();
			if (output != System.out && output != System.err)
				writer.close();
		} catch (IOException e) {
			Environment.logException("IO Exception redirecting output from Process", e);
		}
	}
}