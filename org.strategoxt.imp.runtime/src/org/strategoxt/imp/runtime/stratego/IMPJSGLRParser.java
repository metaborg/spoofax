package org.strategoxt.imp.runtime.stratego;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.jsglr.JSGLRLibrary;
import org.spoofax.interpreter.library.jsglr.JSGLRPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.parser.SGLRParser;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPJSGLRParser extends JSGLRPrimitive {

	protected IMPJSGLRParser() {
		super("JSGLR_parse_string_pt", 1, 4);
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
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
		
		SGLRParser parser = new SGLRParser(pt, startSymbol);
		
		InputStream inputBytes = new ByteArrayInputStream(input.getBytes());
		char[] inputChars = new char[input.length()];
		input.getChars(0, input.length(), inputChars, 0);
		
		try {
			env.setCurrent(parser.parse(inputBytes, inputChars, path).getTerm());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SGLRException e) {
			e.printStackTrace();
		}
		return false;
	}
}
