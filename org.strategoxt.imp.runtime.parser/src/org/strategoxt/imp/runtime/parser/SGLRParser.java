package org.strategoxt.imp.runtime.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import lpg.runtime.Monitor;
import lpg.runtime.PrsStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.parser.IParser;
import org.spoofax.interpreter.adapter.aterm.WrappedATermFactory;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLR;
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.Debug;

import aterm.ATerm;
import aterm.ATermFactory;

/**
 * IParser implementation for SGLR.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */ 
public class SGLRParser implements IParser {	
	private static final int EOFT_SYMBOL = -1;
	
	private final static WrappedATermFactory wrappedFactory
		= new WrappedATermFactory();
	
	private final static ATermFactory factory
		= wrappedFactory.getFactory();
	
	private final static AsfixConverter converter = new AsfixConverter(wrappedFactory);
	
	private final SGLR parser;
	
	private final String startSymbol;
	
	private final PrsStream parseStream = new PrsStream();
	
	// Simple accessors

	public int getEOFTokenKind() {
		return EOFT_SYMBOL;
	}

	public PrsStream getParseStream() {
		return parseStream;
	}
	
	public static ATermFactory getFactory() {
		return factory;
	}
	
	public SGLRParser(ParseTable parseTable, String startSymbol) {	
		parser = new SGLR(factory, parseTable);		
		this.startSymbol = startSymbol;
	}
	
	public ATerm parse(IPath input) throws SGLRException, IOException {
		InputStream stream = new FileInputStream(input.toOSString());
		ATerm asfix;
		
		try {
			Debug.startTimer();
			
			asfix = parser.parse(stream, startSymbol);
		} finally {
			Debug.stopTimer("File parsed");
			stream.close();
		}
		
		return converter.implode(asfix);
	}
	
	// LPG compatibility

	@Deprecated
	public SGLR parser(Monitor monitor, int error_repair_count) {
		// TODO: Return SGLR Parser implementation? 
		throw new UnsupportedOperationException();
	}
}
