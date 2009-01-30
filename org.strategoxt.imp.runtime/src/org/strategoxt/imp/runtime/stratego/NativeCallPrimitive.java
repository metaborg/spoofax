package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class NativeCallPrimitive extends AbstractPrimitive {

	private final NativeCallHelper caller = new NativeCallHelper();
	
	public NativeCallPrimitive() {
		super("SSL_EXT_call", 0, 2);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		try {
			if (!isTermString(tvars[0]) || !isTermList(tvars[1])) return false;
			
			String[] commandArgs = toCommandArgs(tvars);
			
			// I/O setup
			SSLLibrary op = (SSLLibrary) env.getOperatorRegistry(SSLLibrary.REGISTRY_NAME);
			IOAgent io = op.getIOAgent();
			File dir = io.openFile(io.getWorkingDir());
			OutputStream stdout = io.getOutputStream(IOAgent.CONST_STDOUT);
			OutputStream stderr = io.getOutputStream(IOAgent.CONST_STDERR);
			
			// Invocation
			int returnCode = caller.call(commandArgs, dir, stdout, stderr);
			env.setCurrent(env.getFactory().makeInt(returnCode));
			return true;
			
		} catch (InterruptedException e) {
			throw new InterpreterException("SSL_EXT_CALL system call interrupted", e);
		} catch (IOException e) {
			return false;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private String[] toCommandArgs(IStrategoTerm[] tvars) throws IllegalArgumentException {
		IStrategoList args = (IStrategoList) tvars[1];
		String[] result = new String[1 + args.size()];
		result[0] = javaString(tvars[0]);
		
		for (int i = 0; i < args.size(); i++) {
			if (!isTermString(args.get(i)))
				throw new IllegalArgumentException();
			result[i+1] = javaString(args.get(i));
		}
		
		return result;
	}
}
