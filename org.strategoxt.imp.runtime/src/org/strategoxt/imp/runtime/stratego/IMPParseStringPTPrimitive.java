package org.strategoxt.imp.runtime.stratego;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

import org.spoofax.interpreter.adapter.aterm.WrappedATermFactory;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.TermConverter;
import org.spoofax.jsglr.Disambiguator;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLRException;
import org.spoofax.jsglr.StructureRecoveryAlgorithm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.strategoxt.lang.LazyTerm;
import org.strategoxt.lang.compat.sglr.JSGLR_parse_string_pt_compat;

import aterm.ATerm;

/**
 * Parses strings to asfix trees, caching the internal ATerm
 * for implosion with {@link IMPImplodeAsfixStrategy}. 
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPParseStringPTPrimitive extends JSGLR_parse_string_pt_compat {
	
	private final TermConverter termConverter = new TermConverter(Environment.getTermFactory());
	
	private final Map<IStrategoTerm, char[]> inputCharMap = new WeakHashMap<IStrategoTerm, char[]>();

	private final Map<IStrategoTerm, ATerm> inputTermMap = new WeakHashMap<IStrategoTerm, ATerm>();

	protected IMPParseStringPTPrimitive(WrappedATermFactory factory, Disambiguator filterSettings) {
		super(factory, filterSettings);
	}

	@Override
	public IStrategoTerm call(IContext env, String input,
			ParseTable table, String startSymbol, boolean outputWrappedATerm)
			throws InterpreterException, IOException, SGLRException {
		
		// TODO: new "jsglr enable recovery" strategy in jsglr-parser.str
		// FIXME: recovery is always enabled in Stratego right now
		//        (and once it isn't, how would caching factor into that?)
		
		String path = getLastPath();		
		JSGLRI parser = new JSGLRI(table, startSymbol);
		parser.setRecoverHandler(new StructureRecoveryAlgorithm());
		char[] inputChars = input.toCharArray();
		
		final ATerm asfix = parser.parseNoImplode(inputChars, path);
		IStrategoTerm result = new LazyTerm() {
			@Override
			protected IStrategoTerm init() {
				return Environment.getWrappedATermFactory().wrapTerm(asfix);
			}			
		};
			
		result = termConverter.convert(result);

		inputTermMap.put(result, asfix);
		inputCharMap.put(result, inputChars);
		
		return result;
	}
	
	public char[] getInputChars(IStrategoTerm asfix) {
		return inputCharMap.get(asfix);
	}
	
	public ATerm getInputTerm(IStrategoTerm asfix) {
		return inputTermMap.get(asfix);
	}
}
