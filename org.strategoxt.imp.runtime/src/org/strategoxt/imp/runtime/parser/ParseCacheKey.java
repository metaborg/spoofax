package org.strategoxt.imp.runtime.parser;

import java.util.Arrays;

/**
 * A tuple class ;)
 * 
 * Used as a key in the parse caching table.
 */
public class ParseCacheKey {
	private final Object parseTable;
	private final String startSymbol;
	private final char[] input;
	private final String filename; // essential to keep a consistent ast/tokens/resource mapping
	private final boolean recoveryEnabled;
	
	public ParseCacheKey(Object parseTable, String startSymbol, char[] input,
			String filename, boolean recoveryEnabled) {
		this.parseTable = parseTable;
		this.startSymbol = startSymbol;
		this.input = input;
		this.filename = filename;
		this.recoveryEnabled = recoveryEnabled;
	}

	@Override
	public boolean equals(Object obj) {
		ParseCacheKey other = (ParseCacheKey) obj;
		return parseTable.equals(other.parseTable)
			&& Arrays.equals(input, other.input)
			&& (filename == null ? other.filename == null : filename.equals(other.filename))
			&& (startSymbol == null ? other.startSymbol == null : startSymbol.equals(other.startSymbol))
			&& (recoveryEnabled == other.recoveryEnabled);
	}
	
	@Override
	public int hashCode() {
		// (Ignores parse table hash code)
		return 12125125
			* (startSymbol == null ? 42 : startSymbol.hashCode())
			* (filename == null ? 42 : filename.hashCode())
			^ Arrays.hashCode(input);
	}
}