package org.strategoxt.imp.runtime.parser;

import java.io.IOException;
import java.io.InputStream;

import org.spoofax.compiler.Compiler;
import org.spoofax.interpreter.Interpreter;
import org.spoofax.interpreter.InterpreterException;
import org.spoofax.interpreter.adapter.aterm.WrappedATerm;
import org.spoofax.interpreter.adapter.aterm.WrappedATermFactory;
import org.spoofax.interpreter.library.jsglr.JSGLRLibrary;
import org.strategoxt.imp.runtime.Debug;

import aterm.ATerm;

/**
 * Class to convert an Asfix tree to another format.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class AsfixConverter {
	private final WrappedATermFactory factory; 
	
	private final Interpreter interpreter;
	
	public AsfixConverter(WrappedATermFactory factory) {
		this.factory = factory;

		try {
			interpreter = new Interpreter(factory);
			
			interpreter.addOperatorRegistry("JSGLR", new JSGLRLibrary(factory));
			interpreter.load(Compiler.sharePath()  + "/libstratego-lib.ctree");
			interpreter.load(Compiler.sharePath()  + "/libstratego-sglr.ctree");	
			
			InputStream imploder = SGLRParser.class.getResourceAsStream("/str/call-implode-asfix.ctree");
			
			interpreter.load(imploder);
		} catch (IOException x) {
			throw new RuntimeException(x); // shouldn't happen
		} catch (InterpreterException x) {
			throw new RuntimeException(x); // shouldn't happen in release builds
		}
	}
	
	/**
	 * Asfix-implode the given term.
	 * 
	 * @return  The output of the asfix-implosion, or the identity if this
	 *          transformation was not applicable.
	 */
	public ATerm implode(ATerm asfix) {
		try {
			Debug.startTimer("implode-asfix");

			interpreter.setCurrent(factory.wrapTerm(asfix));
			interpreter.invoke("main_0_0");
			WrappedATerm wrappedTerm = (WrappedATerm) interpreter.current();

			return wrappedTerm.getATerm();
		} catch (InterpreterException x) {
			throw new RuntimeException("implode-asfix failed", x);
		} finally {
			Debug.stopTimer("implode-asfix completed");
		}
	}
}
