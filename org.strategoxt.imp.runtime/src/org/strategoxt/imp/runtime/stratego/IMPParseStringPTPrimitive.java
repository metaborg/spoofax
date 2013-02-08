/*
 * Licensed under the GNU Lesser General Public License, v2.1
 */
package org.strategoxt.imp.runtime.stratego;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.Disambiguator;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.jsglr.shared.TokenExpectedException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.spoofax.interpreter.library.jsglr.STRSGLR_parse_string_pt;

/**
 * Parses strings to asfix trees.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class IMPParseStringPTPrimitive extends STRSGLR_parse_string_pt {
	
	private final SourceMappings mappings;
	
	private Map<ParseTable, Object> isNoRecoveryWarned =
		new WeakHashMap<ParseTable, Object>();

	protected IMPParseStringPTPrimitive(Disambiguator filterSettings, 
			AtomicBoolean recoveryEnabled, SourceMappings mappings) {
		super(filterSettings, recoveryEnabled);
		this.mappings = mappings;
	}

	@Override
	protected IStrategoTerm doParse(IContext env, IStrategoString inputTerm, ParseTable table, String startSymbol, String path) throws InterpreterException, TokenExpectedException, BadTokenException, SGLRException {
		
		String input = Tools.asJavaString(inputTerm);
		JSGLRI parser = new JSGLRI(table, startSymbol);
		parser.setUseRecovery(isRecoveryEnabled());
		parser.setDisambiguator(getFilterSettings());
		parser.setImplodeEnabled(false);
		if (isRecoveryEnabled() && !parser.getParseTable().hasRecovers()) {
			assert table.hashCode() == System.identityHashCode(table);
			if (!isNoRecoveryWarned.containsKey(table)) {
				Environment.logWarning("No recovery rules available in parse table for " + NAME);
				isNoRecoveryWarned.put(table, null);
			}
		}
		
		File inputFile = mappings.getInputFile(inputTerm);
		if (inputFile != null) path = inputFile.getAbsolutePath();

		try {
			return parser.parse(input, path);
		} catch (IOException e) {
			throw new SGLRException(parser.getParser(), e.getMessage(), e);
		}
	}
}
