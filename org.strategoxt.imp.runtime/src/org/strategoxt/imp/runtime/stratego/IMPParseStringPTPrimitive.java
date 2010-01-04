package org.strategoxt.imp.runtime.stratego;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.Disambiguator;
import org.spoofax.jsglr.NoRecoveryRulesException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLRException;
import org.spoofax.jsglr.StructureRecoveryAlgorithm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.strategoxt.lang.LazyTerm;
import org.strategoxt.lang.compat.sglr.JSGLR_parse_string_pt_compat;

import aterm.ATerm;
import aterm.ATermFactory;

/**
 * Parses strings to asfix trees, caching the internal ATerm
 * for implosion with {@link IMPImplodeAsfixStrategy}. 
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPParseStringPTPrimitive extends JSGLR_parse_string_pt_compat {
	
	private final SourceMappings mappings;
	
	private Map<ParseTable, ParseTable> isNoRecoveryWarned = new IdentityHashMap<ParseTable, ParseTable>();

	protected IMPParseStringPTPrimitive(ATermFactory atermFactory, Disambiguator filterSettings, 
			SourceMappings mappings) {
		super(atermFactory, filterSettings);
		this.mappings = mappings;
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoString inputTerm,
			ParseTable table, String startSymbol)
			throws InterpreterException, IOException, SGLRException {
		
		// TODO: new "jsglr enable recovery" strategy in jsglr-parser.str
		// FIXME: recovery is always enabled in Stratego right now
		//        (and once it isn't, how would caching factor into that?)
		
		String input = inputTerm.stringValue();
		String path = getLastPath();		
		JSGLRI parser = new JSGLRI(table, startSymbol);
		try {
			parser.setRecoverHandler(new StructureRecoveryAlgorithm());
		} catch (NoRecoveryRulesException e) {
			if (!isNoRecoveryWarned.containsKey(table)) {
				Environment.logException(NAME + ": warning - no recovery rules available in parse table", e);
				// (we use an identity hash map to avoid parse table hashing)
				isNoRecoveryWarned.put(table, table);
			}
		}
		char[] inputChars = input.toCharArray();
		
		final ATerm asfix = parser.parseNoImplode(inputChars, path);
		IStrategoTerm result = new LazyTerm() {
			@Override
			protected IStrategoTerm init() {
				return Environment.getATermConverter().convert(asfix);
			}
		};

		mappings.putInputTerm(result, asfix);
		mappings.putInputChars(result, inputChars);
		mappings.putInputFile(result, mappings.getInputFile(inputTerm));
		mappings.putTokenizer(result, parser.getTokenizer());
		
		return result;
	}
}
