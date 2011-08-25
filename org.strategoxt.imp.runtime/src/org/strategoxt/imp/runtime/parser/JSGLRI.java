
package org.strategoxt.imp.runtime.parser;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.Asfix2TreeBuilder;
import org.spoofax.jsglr.client.Disambiguator;
import org.spoofax.jsglr.client.FilterException;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.client.imploder.ITreeFactory;
import org.spoofax.jsglr.client.imploder.TermTreeFactory;
import org.spoofax.jsglr.client.imploder.TreeBuilder;
import org.spoofax.jsglr.client.incremental.IncrementalSGLR;
import org.spoofax.jsglr.client.incremental.IncrementalSGLRException;
import org.spoofax.jsglr.client.incremental.IncrementalSortSet;
import org.spoofax.jsglr.io.SGLR;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.jsglr.shared.TokenExpectedException;
import org.spoofax.terms.attachments.ParentTermFactory;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.ParseTableProvider;

/**
 * IMP IParser implementation using JSGLR, imploding parse trees to AST nodes and tokens.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */ 
public class JSGLRI extends AbstractSGLRI {
	
	private ParseTableProvider parseTable;
	
	private boolean useRecovery = false;
	
	private SGLR parser;
	
	private IncrementalSGLR<IStrategoTerm> incrementalParser;
	
	private Disambiguator disambiguator;
	
	private int timeout;
	
	private int cursorLocation;
	
	// Initialization and parsing
	
	public void setCursorLocation(int cursorLocation) {
		this.cursorLocation = cursorLocation;
	}

	public JSGLRI(ParseTableProvider parseTable, String startSymbol,
			SGLRParseController controller) {
		super(parseTable, startSymbol, controller);
		
		this.parseTable = parseTable;
		this.parser = Environment.createSGLR(getParseTable());
		this.cursorLocation = Integer.MAX_VALUE;
		resetState();
	}
	
	public JSGLRI(ParseTableProvider parseTable, String startSymbol) {
		this(parseTable, startSymbol, null);
	}
	
	public JSGLRI(ParseTable parseTable, String startSymbol,
			SGLRParseController controller) {
		this(new ParseTableProvider(parseTable), startSymbol, controller);
	}
	
	public JSGLRI(ParseTable parseTable, String startSymbol) {
		this(new ParseTableProvider(parseTable), startSymbol, null);
	}
	
	public SGLR getParser() {
		return parser;
	}
	
	public Set<BadTokenException> getCollectedErrors() {
		return getParser().getCollectedErrors();
	}
	
	@Override
	public void setStartSymbol(String startSymbol) {
		super.setStartSymbol(startSymbol);
	}
	
	/**
	 * @see SGLR#setUseStructureRecovery(boolean)
	 */
	public void setUseRecovery(boolean useRecovery) {
		this.useRecovery = useRecovery;
		parser.setUseStructureRecovery(useRecovery);
	}
	
	public ParseTable getParseTable() {
		try {
			return parseTable.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Disambiguator getDisambiguator() {
		return disambiguator;
	}
	
	public void setDisambiguator(Disambiguator disambiguator) {
		this.disambiguator = disambiguator;
	}
	
	public void setParseTable(ParseTable parseTable) {
		this.parseTable = new ParseTableProvider(parseTable);
		resetState();
	}
	
	public void setParseTable(ParseTableProvider parseTable) {
		this.parseTable = parseTable;
		resetState();
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
		resetState();
	}
	
	/**
	 * Resets the state of this parser, reinitializing the SGLR instance
	 */
	void resetState() {
		// Reinitialize parser if parsetable changed (due to .meta file)
		if (getParseTable() != parser.getParseTable()) {
			parser = Environment.createSGLR(getParseTable());
		}
		parser.setTimeout(timeout);
		if (disambiguator != null) parser.setDisambiguator(disambiguator);
		else disambiguator = parser.getDisambiguator();
		setUseRecovery(useRecovery);
		if (!isImplodeEnabled()) {
			parser.setTreeBuilder(new Asfix2TreeBuilder(Environment.getTermFactory()));
		} else {
			assert parser.getTreeBuilder() instanceof TreeBuilder;
			@SuppressWarnings("unchecked")
			ITreeFactory<IStrategoTerm> treeFactory = ((TreeBuilder) parser.getTreeBuilder()).getFactory();
			assert ((TermTreeFactory) treeFactory).getOriginalTermFactory()
				instanceof ParentTermFactory;
			if (incrementalParser == null || incrementalParser.getParser().getParseTable() != parser.getParseTable())
				incrementalParser = new IncrementalSGLR<IStrategoTerm>(parser, null, treeFactory, IncrementalSortSet.read(getParseTable()));
		}
	}
	
	@Override
	protected IStrategoTerm doParse(String input, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		// Read stream using tokenizer/lexstream
		
		if (parseTable.isDynamic()) {
			parseTable.initialize(new File(filename));
			resetState();
		}
		
		try {
			Debug.startTimer();
			IStrategoTerm result;
			try {
				//TODO: completionMode true or false depends on whether this method is called via CompletionParser
				// true means all completion productions are enabled
				// false means that only wellformed productions are enabled
				// Idee: mark wellformed productions as {completion, recover} and treat them as completion
				result = (IStrategoTerm) parser.parse(input, filename, getStartSymbol(), true, cursorLocation);
			} finally {
				Debug.stopTimer("File parsed: " + new File(filename).getName());
			}

			// UNDONE: disabled incremental parser for now
			// testIncrementalParser(input, filename, result);
			return result;
		} catch (FilterException e) {
			if (e.getCause() == null && parser.getDisambiguator().getFilterPriorities()) {
				Environment.logException("Parse filter failure - disabling priority filters and trying again", e);
				getDisambiguator().setFilterPriorities(false);
				try {
					IStrategoTerm result = (IStrategoTerm) parser.parse(input, filename, getStartSymbol());
					return result;
				} finally {
					getDisambiguator().setFilterPriorities(true);
				}
			} else {
				throw new FilterException(e.getParser(), e.getMessage(), e);
			}
		}
	}

	private void testIncrementalParser(String input, String filename, IStrategoTerm expected) {
		if (!incrementalParser.getIncrementalSorts().isEmpty()) {
			Debug.startTimer();
			try {
				IStrategoTerm oldAst = incrementalParser.getLastAst();
				IStrategoTerm incrementalResult = incrementalParser.parseIncremental(input, filename, getStartSymbol());
				if (!incrementalResult.equals(expected)) {
					Environment.logWarning("Incremental parser result inconsistent:\n\n"
							+ incrementalResult + "\n\nvs. non-incremental:\n\n"
							+ expected + "\n\n"
							+ "from:\n\n" + input
							+ "\n\nwith sorts " + incrementalParser.getIncrementalSorts());
				}
			} catch (IncrementalSGLRException e) {
				Debug.log("Could not incrementally parse AST");
			} catch (SGLRException e) {
				Environment.logWarning("Exception in incremental parser", e);
			} catch (RuntimeException e) {
				Environment.logWarning("Exception in incremental parser", e);
			} catch (Error e) {
				Environment.logException("Exception in incremental parser", e);
			} finally {
				Debug.stopTimer("Incrementally parsed: " + new File(filename).getName());
			}
		}
	}
}
