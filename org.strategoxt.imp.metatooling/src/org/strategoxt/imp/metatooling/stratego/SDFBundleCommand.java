package org.strategoxt.imp.metatooling.stratego;

import static org.spoofax.interpreter.terms.IStrategoTerm.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.compat.NativeCallHelper;
import org.strategoxt.stratego_xtc.xtc_command_1_0;
import org.syntax_definition.sdf.Activator;

/**
 * Overrides the xtc-command strategy to use sdf2table
 * from the SDF plugin.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SDFBundleCommand extends xtc_command_1_0 {
	
	private final xtc_command_1_0 proceed = xtc_command_1_0.instance;

	private File binaryPrefix;
	
	private String binaryPostfix;
	
	private void init() throws IOException {
		if (binaryPostfix != null) return;
		Activator sdfBundle = Activator.getInstance();
		binaryPrefix = sdfBundle.getBinaryPrefix();
		binaryPostfix = sdfBundle.getBinaryPostfix();
	}
	
	@Override
	public IStrategoTerm invoke(Context context, IStrategoTerm args,
			org.strategoxt.lang.Strategy command) {
		
		try {
			init();
		} catch (IOException e) {
			Environment.logException("Could not determine the prefix path for the native tool bundle", e);
			return proceed.invoke(context, args, command);
		}
		
		IStrategoTerm commandTerm = command.invoke(context, args);
		if (commandTerm.getTermType() != STRING)
			return proceed.invoke(context, args, command);
		
		String commandName = ((IStrategoString) commandTerm).stringValue();
		if (!new File(binaryPrefix + "/" + commandName + binaryPostfix).exists())
			return proceed.invoke(context, args, command);
		
		if (args.getTermType() != LIST)
			return null;
		
		return invokeSDF2Table(context, commandName, ((IStrategoList) args).getAllSubterms())
			? args
			: null;
	}

	private boolean invokeSDF2Table(Context context, String command, IStrategoTerm[] argList) throws StrategoException {
		// HACK: concatenating all command-line arguments...
		StringBuilder allArgs = new StringBuilder();
		String[] commandArgs = new String[argList.length + 1];
		int i = 1;
		for (IStrategoTerm arg : argList) {
			if (arg.getTermType() != STRING) return false;
			allArgs.append(' ');
			allArgs.append(((IStrategoString) arg).stringValue());
			commandArgs[i++] = ((IStrategoString) arg).stringValue();
		}
		
		try {
			/*
			InputStream result = Tools.exec("sdf2table " + allArgs, System.in);
			// Synchronously copy the std error stream
			PrintStream stderr = context.getIOAgent().getOutputStream(IOAgent.CONST_STDERR);
			new NativeCallHelper.StreamCopier(result, stderr).run();
			*/
			commandArgs[0] = binaryPrefix + "/" + command + binaryPostfix;
			String[] environment = { "PATH=" + binaryPrefix.getAbsolutePath(), 
					                 "LD_LIBRARY_PATH=" + binaryPrefix.getParentFile().getAbsolutePath() + "/lib" };
			IOAgent io = context.getIOAgent();
			PrintStream stdout = io.getOutputStream(IOAgent.CONST_STDOUT);
			PrintStream stderr = io.getOutputStream(IOAgent.CONST_STDERR);
			int result = new NativeCallHelper().call(commandArgs, environment, new File(io.getWorkingDir()), stdout, stderr);
			return result == 0;
		} catch (IOException e) {
			return false;
		} catch (InterruptedException e) {
			throw new StrategoException("Could not call sdf2table", e);
		}
	}
}
