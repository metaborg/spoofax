package org.strategoxt.imp.runtime.stratego;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.Disambiguator;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.shared.SGLRException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.strategoxt.lang.compat.sglr.JSGLR_parse_string_compat;

/**
 * Parses strings to abstract syntax trees. 
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPParseStringPrimitive extends JSGLR_parse_string_compat {

	private final SourceMappings mappings;
	
	private Map<ParseTable, Object> isNoRecoveryWarned =
		new WeakHashMap<ParseTable, Object>();

	private final Disambiguator filterSettings;

	protected IMPParseStringPrimitive(Disambiguator filterSettings, 
			AtomicBoolean recoveryEnabled, SourceMappings mappings) {
		super(filterSettings, recoveryEnabled);
		this.filterSettings = filterSettings;
		this.mappings = mappings;
	}

	@Override
	protected IStrategoTerm call(IContext env, IStrategoString inputTerm,
			ParseTable table, String startSymbol)
			throws InterpreterException, IOException, SGLRException {
		
		// TODO: Optimize - cache tree builder... (like in superclass)
		
		String input = inputTerm.stringValue();
		String path = getLastPath();		
		JSGLRI parser = new JSGLRI(table, startSymbol);
		parser.setUseRecovery(isRecoveryEnabled());
		parser.setDisambiguator(filterSettings);
		parser.setImplodeEnabled(true);
		if (isRecoveryEnabled() && !parser.getParseTable().hasRecovers()) {
			assert table.hashCode() == System.identityHashCode(table);
			if (!isNoRecoveryWarned.containsKey(table)) {
				Environment.logWarning("No recovery rules available in parse table for " + NAME);
				isNoRecoveryWarned.put(table, null);
			}
		}
		
		File inputFile = mappings.getInputFile(inputTerm);
		if (inputFile != null) path = inputFile.getAbsolutePath();
		
		return parser.parse(input, path);
	}
}
