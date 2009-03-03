package org.strategoxt.imp.runtime.stratego;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.jsglr.JSGLRLibrary;
import org.spoofax.interpreter.library.jsglr.JSGLRPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.TermConverter;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.JSGLRI;

import aterm.ATerm;

/**
 * Supports parsing with JSGLR with a custom ITermFactory (i.e., not WrappedATermFactory).
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPJSGLRPrimitive extends JSGLRPrimitive {
	
	private final TermConverter termConverter = new TermConverter(Environment.getTermFactory());
	
	private final Map<IStrategoTerm, char[]> inputCharMap = new WeakHashMap<IStrategoTerm, char[]>();

	private final Map<IStrategoTerm, ATerm> inputTermMap = new WeakHashMap<IStrategoTerm, ATerm>();

	public static final String NAME = "JSGLR_parse_string_pt";

	protected IMPJSGLRPrimitive() {
		super(NAME, 1, 4);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		// TODO: Use svars[0] onError argument?
		
		if(!Tools.isTermString(tvars[0]))
			return false;
		if(!Tools.isTermInt(tvars[1]))
			return false;
		if(!Tools.isTermString(tvars[2]))
			return false;
		
		String input = Tools.asJavaString(tvars[0]);
		String path = Tools.isTermString(tvars[3]) ? tvars[3].toString() : "String";
		
		JSGLRLibrary lib = getLibrary(env);
		ParseTable pt = lib.getParseTable(Tools.asJavaInt(tvars[1]));
		String startSymbol = tvars[2].toString();
		
		JSGLRI parser = new JSGLRI(pt, startSymbol);
		
		char[] inputChars = new char[input.length()];
		input.getChars(0, input.length(), inputChars, 0);
		
		try {
			ATerm asfix = parser.parseNoImplode(inputChars, path);
			IStrategoTerm result = Environment.getWrappedATermFactory().wrapTerm(asfix);
			result = termConverter.convert(result);

			inputTermMap.put(result, asfix);
			inputCharMap.put(result, inputChars);
			
			env.setCurrent(result);
			return true;
		} catch (IOException e) {
            Environment.logException("Could not parse " + path, e);
		} catch (SGLRException e) {
			Environment.logException("Could not parse " + path, e);
		}
		return false;
	}
	
	public char[] getInputChars(IStrategoTerm asfix) {
		return inputCharMap.get(asfix);
	}
	
	public ATerm getInputTerm(IStrategoTerm asfix) {
		return inputTermMap.get(asfix);
	}
}
