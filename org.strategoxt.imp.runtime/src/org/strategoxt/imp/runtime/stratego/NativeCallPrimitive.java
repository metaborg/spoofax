package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class NativeCallPrimitive extends AbstractPrimitive {

	public NativeCallPrimitive() {
		super("SSL_EXT_call", 0, 2);
	}
	
	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		if (!isTermString(tvars[0]) || !isTermList(tvars[1])) return false;
		
		IStrategoList args = (IStrategoList) tvars[1];
		String[] commandArgs = new String[1 + args.size()];
		commandArgs[0] = javaString(tvars[0]);
		
		for (int i = 0; i < args.size(); i++) {
			if (!isTermString(args.get(i)))
				return false;
			commandArgs[i+1] = javaString(args.get(i));
		}
		
		SSLLibrary op = (SSLLibrary) env.getOperatorRegistry(SSLLibrary.REGISTRY_NAME);
		IOAgent io = op.getIOAgent();
		File dir = io.openFile(io.getWorkingDir());
		
		try {
			Process process = Runtime.getRuntime().exec(commandArgs, null, dir);
			new StreamCopier(process.getInputStream(), io.getOutputStream(IOAgent.CONST_STDOUT)).start();
			new StreamCopier(process.getErrorStream(), io.getOutputStream(IOAgent.CONST_STDERR)).start();
			
			int result = process.waitFor();
					
			env.setCurrent(env.getFactory().makeInt(result));
		
			return true;
		} catch (InterruptedException e) {
			throw new InterpreterException("SSL_EXT_CALL system call interrupted", e);
		} catch (IOException e) {
			return false;
		}
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
			
			// NOTE: This may block if exceptionally long lines are printed
			String line;
			while ((line = reader.readLine()) != null) {
				writer.println(line);
			}
			
			reader.close();
			writer.close();
		} catch (IOException e) {
			Environment.logException("IO Exception redirecting output from Process", e);
		}
	}
}