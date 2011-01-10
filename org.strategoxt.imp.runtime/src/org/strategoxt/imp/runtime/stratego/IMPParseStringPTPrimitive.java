package org.strategoxt.imp.runtime.stratego;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.Disambiguator;
import org.spoofax.jsglr.client.NoRecoveryRulesException;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.LazyTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.strategoxt.imp.runtime.stratego.SourceMappings.MappableTerm;
import org.strategoxt.lang.compat.sglr.JSGLR_parse_string_pt_compat;

/**
 * Parses strings to asfix trees, caching the internal IStrategoTerm
 * for implosion with {@link IMPImplodeAsfixStrategy}. 
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPParseStringPTPrimitive extends JSGLR_parse_string_pt_compat {
	
	private final SourceMappings mappings;
	
	private Map<ParseTable, Object> isNoRecoveryWarned =
		new WeakHashMap<ParseTable, Object>();

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
			parser.setUseRecovery(true);
		} catch (NoRecoveryRulesException e) {
			assert table.hashCode() == System.identityHashCode(table);
			if (!isNoRecoveryWarned.containsKey(table)) {
				Environment.logException(NAME + ": warning - no recovery rules available in parse table", e);
				isNoRecoveryWarned.put(table, null);
			}
		}
		char[] inputChars = input.toCharArray();
		
		final IStrategoTerm asfix = parser.parseNoImplode(inputChars, path);
		MappableTerm result = new MappableTerm(new LazyTerm() {
			@Override
			protected IStrategoTerm init() {
				Environment.logWarning("Parse tree was converted to StrategoTerm format");
				return Environment.getATermConverter().convert(asfix);
			}
		});

		File inputFile = mappings.getInputFile(inputTerm);
		if (inputFile == null)
			Environment.logWarning("Could not determine original file name for parsed string");
		
		mappings.putInputTerm(result, asfix);
		mappings.putInputChars(result, inputChars);
		mappings.putInputFileForTree(result, inputFile);
		mappings.putTokenizer(result, parser.getCurrentTokenizer());
		
		return result;
	}
}
