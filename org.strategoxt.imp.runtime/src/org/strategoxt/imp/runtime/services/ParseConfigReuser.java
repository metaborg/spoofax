package org.strategoxt.imp.runtime.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.spoofax.jsglr.client.Frame;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.client.incremental.IncrementalSortSet;
import org.spoofax.jsglr.io.SGLR;
import org.spoofax.jsglr.shared.ArrayDeque;

/**
 * Reusing the parser config to parse prefixes of the input string.
 *
 * @author Maartje de Jonge
 */
public class ParseConfigReuser {
	
	private String documentPrefix;

	private int lastOffset;

	private ArrayDeque<Frame> parserConfig;

	private final Map<String, Set<String>> injections; // cached mapping from sort to all sorts injecting into that sort, e.g. ClassBodyDec -> MethodDec
	
	private final ParseTable pt;

	public String getDocumentPrefix() {
		return documentPrefix;
	}

	public int getLastOffset() {
		return lastOffset;
	}

	public ParseConfigReuser(ParseTable pt){
		this.documentPrefix = "";
		this.lastOffset = Integer.MAX_VALUE;
		this.parserConfig = null;
		this.injections = new HashMap<String, Set<String>>();
		this.pt = pt;
	}
	
	public ArrayDeque<Frame> parsePrefix(SGLR parser, boolean useRecovery, boolean storeConfig, String document, int endOffset) {
		if(endOffset <= 0){
			return new ArrayDeque<Frame>();
		}
		assert document.length() >= endOffset - 1 || endOffset <=0;
		ArrayDeque<Frame> newParserConfig = new ArrayDeque<Frame>();
		if(document.startsWith(documentPrefix) && endOffset >= lastOffset){
			if(endOffset == this.lastOffset){
				return copyParserConfig();
			}
			else{
				try{
					newParserConfig = parser.parseInputPart(this.parserConfig, this.lastOffset, endOffset, document, useRecovery);
				}catch (Exception e) {
					//Parser failed due to unrecovered syntax errors
				}			
			}
		}
		else{
			try{
				newParserConfig = parser.parseInputPart(null, 0, endOffset, document, useRecovery);
			}catch (Exception e) {
				//Parser failed due to unrecovered syntax errors
			}			
		}
		if (storeConfig && document.length() >= endOffset + 3){
			assert document.length() >= endOffset + 3; //parser has some built in lookahead
			this.documentPrefix = document.substring(0, endOffset + 3); //substring look ahead
			this.lastOffset = endOffset;
			this.parserConfig = newParserConfig;
			return copyParserConfig();			
		}
		return newParserConfig;
	}

	private ArrayDeque<Frame> copyParserConfig() {
		ArrayDeque<Frame> stackNodes = new ArrayDeque<Frame>();
		stackNodes.addAll(parserConfig);
		return stackNodes;
	}

	public Set<String> getInjectionsFor(String wantedSort) {
		Set<String> wantedSorts = injections.get(wantedSort);
		if (wantedSorts == null) {
			final IncrementalSortSet iss = IncrementalSortSet.create(pt, true, false, wantedSort);
			wantedSorts = iss.getIncrementalSorts();
			injections.put(wantedSort, wantedSorts);
		}
		return wantedSorts;
	}
}
