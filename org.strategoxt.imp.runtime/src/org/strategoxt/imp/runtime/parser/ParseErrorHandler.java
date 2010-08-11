package org.strategoxt.imp.runtime.parser;

import static java.lang.Math.min;
import static org.spoofax.jsglr.Term.applAt;
import static org.spoofax.jsglr.Term.listAt;
import static org.spoofax.jsglr.Term.termAt;

import java.util.ArrayList;
import java.util.List;

import lpg.runtime.IToken;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.interpreter.terms.TermConverter;
import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.MultiBadTokenException;
import org.spoofax.jsglr.ParseTimeoutException;
import org.spoofax.jsglr.RecoveryConnector;
import org.spoofax.jsglr.RegionRecovery;
import org.spoofax.jsglr.TokenExpectedException;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.imp.generator.simplify_ambiguity_report_0_0;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AsfixAnalyzer;
import org.strategoxt.imp.runtime.parser.ast.AstMessageHandler;
import org.strategoxt.imp.runtime.parser.ast.MarkerSignature;
import org.strategoxt.imp.runtime.parser.ast.ProductionAttributeReader;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenizer;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.lang.Context;
import org.strategoxt.stratego_aterm.stratego_aterm;
import org.strategoxt.stratego_sglr.implode_asfix_0_0;
import org.strategoxt.stratego_sglr.stratego_sglr;

import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermInt;
import aterm.ATermList;

/**
 * SGLR parse error reporting for a particular SGLR Parse controller and file. 
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class ParseErrorHandler {
	
	/**
	 * The constructor used for "water" recovery rules.
	 */
	public static final String WATER = "WATER";
	
	/**
	 * The constructor used for "insertion" recovery rules.
	 */
	public static final String INSERT = "INSERTION";
	
	/**
	 * The constructor used for "end insertion" recovery rules.
	 */
	public static final String INSERT_END = "INSERTEND";
	
	public static final String DEPRECATED = "deprecated";
	
	/**
	 * The parse stream character that indicates a character has
	 * been skipped by the parser.
	 */
	public static final char SKIPPED_CHAR = (char) -1;
	
	/**
	 * The parse stream character that indicates EOF was unexpected.
	 */
	public static final char UNEXPECTED_EOF_CHAR = (char) -2;

	public static final String UNEXPECTED_TOKEN_POSTFIX = "' not expected here";
	
	public static final String UNEXPECTED_TOKEN_PREFIX = "Syntax error, '";
	
	public static final String UNEXPECTED_REGION = "Could not parse this fragment: misplaced construct(s)";
	
	public static final int PARSE_ERROR_DELAY = min(StrategoObserver.OBSERVER_DELAY + 50, 800);
	
	private static final int LARGE_REGION_SIZE = 8;
	
	private static final String LARGE_REGION_START =
		"Region could not be parsed because of subsequent syntax error(s) indicated below";
	
	private static Context asyncAmbReportingContext;
	
	private final AstMessageHandler handler = new AstMessageHandler(AstMessageHandler.PARSE_MARKER_TYPE);
	
	private final ProductionAttributeReader prodReader = new ProductionAttributeReader();

	private final SGLRParseController source;
	
	private volatile boolean isRecoveryFailed = true;
	
	private int offset;
	
	private boolean inLexicalContext;
	
	private volatile int additionsVersionId;
	
	private List<Runnable> errorReports = new ArrayList<Runnable>();
	
	private boolean rushNextUpdate;

	public ParseErrorHandler(SGLRParseController source) {
		this.source = source;
	}
	
	public void clearErrors() {
		handler.clearMarkers(source.getResource());
	}
	
	/**
	 * Informs the parse error handler that recovery is unavailable.
	 * This information is reflected in any parse error messages.
	 */
	public void setRecoveryFailed(boolean recoveryFailed) {
		this.isRecoveryFailed = recoveryFailed;
	}
	
	public boolean isRecoveryFailed() {
		return isRecoveryFailed;
	}
	
	/**
	 * Sets whether to report the next batch of delayed errors directly. 
	 */
	public void setRushNextUpdate(boolean rushNextUpdate) {
		this.rushNextUpdate = rushNextUpdate;
	}
	
	/**
	 * Report WATER + INSERT errors from parse tree
	 */
	public void gatherNonFatalErrors(char[] inputChars, SGLRTokenizer tokenizer, ATerm top) {
		try {
			errorReports.clear();
			offset = 0;
			reportSkippedFragments(inputChars, tokenizer);
			ATermAppl asfix = termAt(top, 0);
			reportRecoveredErrors(tokenizer, asfix, 0, 0);
		} catch (RuntimeException e) {
			reportError(tokenizer, e);
		}
	}
	
	/**
	 * @see AstMessageHandler#commitDeletions()
	 */
	public void commitDeletions() {
		//   - must not be synchronized; uses resource lock
		//   - when ran directly from the main thread, it may block other
		//     UI threads that already have a lock on my resource,
		//     but are waiting to run in the UI thread themselves
		//   - reporting errors at startup may trigger the above condition,
		//     at least for files with an in-workspace editor(?)
		//   - also see SGLRParseController.onParseCompleted
		assert source.getParseLock().isHeldByCurrentThread();
		assert !Environment.getStrategoLock().isHeldByCurrentThread() : "Potential deadlock";
		
		processErrorReportsQueue();
		handler.commitDeletions();
	}
	
	/**
	 * @see AstMessageHandler#commitMultiErrorLineAdditions()
	 */
	public void commitMultiErrorLineAdditions() {
		// Threading concerns: see commitDeletions()
		assert source.getParseLock().isHeldByCurrentThread();
		assert !Environment.getStrategoLock().isHeldByCurrentThread() : "Potential deadlock";

		processErrorReportsQueue();
		handler.commitMultiErrorLineAdditions();
	}

	/**
	 * @see AstMessageHandler#commitAdditions()
	 */
	public void commitAdditions() {
		// Threading concerns: see commitDeletions()
		assert source.getParseLock().isHeldByCurrentThread();
		assert !Environment.getStrategoLock().isHeldByCurrentThread() : "Potential deadlock";
		
		handler.commitAdditions();
	}

	/**
	 * Schedules delayed error marking for errors not committed yet.
	 * 
	 * @see AstMessageHandler#commitAllChanges()
	 */
	public void scheduleCommitAllChanges() {
		processErrorReportsQueue();

		final int expectedVersion = ++additionsVersionId;

		final List<MarkerSignature> markers = handler.getAdditions();
		if (markers.isEmpty()) return;
		
		Job job = new Job("Report parse errors") {
			@Override
			protected IStatus run(IProgressMonitor monitor2) {
				if (additionsVersionId != expectedVersion) return Status.OK_STATUS;
				
				source.getParseLock().lock();
				try {
					if (additionsVersionId != expectedVersion) return Status.OK_STATUS;
					synchronized (handler.getSyncRoot()) {
						if (additionsVersionId != expectedVersion) return Status.OK_STATUS;
	
						List<IMarker> addedMarkers = handler.asyncCommitAdditions(markers);
						
						if (additionsVersionId != expectedVersion)
							handler.asyncDeleteMarkers(addedMarkers); // rollback
					}
					//source.forceRecolor(); // Adding markers corrupts coloring sometimes
					//EditorState editor = source.getEditor();
					//if (editor != null)
					//	AstMessageHandler.processEditorRecolorEvents(editor.getEditor());
				} finally {
					source.getParseLock().unlock();
				}
				
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		if (rushNextUpdate) {
			rushNextUpdate = false;
			job.schedule(0);
		} else {
			job.schedule((long) (PARSE_ERROR_DELAY * (isRecoveryFailed ? 1.5 : 1)));
		}
	}

	private void processErrorReportsQueue() {
		for (Runnable marker : errorReports) {
			marker.run();
		}
		errorReports.clear();
	}
	
	public void abortScheduledCommit() {
		additionsVersionId++;
	}

    /**
     * Report recoverable errors (e.g., inserted brackets).
     * 
	 * @param outerBeginOffset  The begin offset of the enclosing construct.
     */
	private void reportRecoveredErrors(SGLRTokenizer tokenizer, ATermAppl term, int outerStartOffset, int outerStartOffset2) {
		// TODO: Nicer error messages; merge consecutive error tokens etc.
		int startOffset = offset;
		
		if ("amb".equals(term.getAFun().getName())) {
			// Report errors in first ambiguous branch and update offset
			ATermList ambs = termAt(term, 0);
			reportRecoveredErrors(tokenizer, (ATermAppl) ambs.getFirst(), startOffset, outerStartOffset);
			
			reportAmbiguity(tokenizer, term, startOffset);
			return;
		}
		
		ATermAppl prod = termAt(term, 0);
		ATermAppl rhs = termAt(prod, 1);
		ATermAppl attrs = termAt(prod, 2);
		ATermList contents = termAt(term, 1);
		boolean lexicalStart = false;
		
		if (!inLexicalContext && AsfixAnalyzer.isLexicalNode(rhs) || AsfixAnalyzer.isVariableNode(rhs)) {
			inLexicalContext = lexicalStart = true;
		}
		
		// Recursively visit the subtree and update the offset
		for (int i = 0; i < contents.getLength(); i++) {
			ATerm child = contents.elementAt(i);
			if (child.getType() == ATerm.INT) {
				offset += 1;				
			} else {
				reportRecoveredErrors(tokenizer, (ATermAppl) child, startOffset, outerStartOffset);
			}
		}
		
		//post visit: report error
		String cons = prodReader.getConsAttribute(attrs);
		if (WATER.equals(cons)
				|| prodReader.getAttribute(attrs, "reject") != null) {
			IToken token = tokenizer.makeErrorToken(startOffset, offset - 1);
			tokenizer.changeTokenKinds(startOffset, offset - 1, TokenKind.TK_LAYOUT, TokenKind.TK_ERROR);
			reportErrorAtTokens(token, token, UNEXPECTED_TOKEN_PREFIX + token + UNEXPECTED_TOKEN_POSTFIX);
		} else if (INSERT_END.equals(cons)) {
			IToken token = tokenizer.makeErrorToken(startOffset, offset - 1);
			tokenizer.changeTokenKinds(startOffset, offset - 1, TokenKind.TK_LAYOUT, TokenKind.TK_ERROR);
			reportErrorAtTokens(token, token, "Syntax error, closing of '" + token + "' is expected here");
		} else if (INSERT.equals(cons)
				|| (prodReader.getAttribute(attrs, "recover") != null
				    && !prodReader.getSort(rhs).startsWith(WATER))) {
			IToken token = tokenizer.makeErrorTokenSkipLayout(startOffset, offset, outerStartOffset2);
			String inserted;
			if (rhs.getName().equals("lit")) {
				inserted = applAt(rhs, 0).getName();
			} else if (rhs.getName().equals("char-class")) {
				inserted = toString(listAt(rhs, 0));
			} else {
				inserted = prodReader.getSort(rhs);
				if (inserted == null)
					inserted = "token";
			}
			if (token.getLine() == tokenizer.getLexStream().getLine(outerStartOffset2) && !token.toString().equals(inserted)) {
				reportErrorAtTokens(token, token, "Syntax error, expected: '" + inserted + "'");
			} else {
				// Had to backtrack to the last token of the current line,
				// (or reporting a missing } at another })
				reportErrorAtTokens(token, token, "Syntax error, insert '" + inserted + "' to complete construct");
			}
		} else if (getDeprecatedProductionMessage(attrs) != null) {
			IToken token = tokenizer.makeErrorToken(startOffset, offset - 1);
			reportWarningAtTokens(token, token, getDeprecatedProductionMessage(attrs));
		}
		
		if (lexicalStart) inLexicalContext = false;
	}
	
	private static String toString(ATermList chars) {
		// TODO: move to SSL_implode_string.call() ?
        StringBuilder result = new StringBuilder(chars.getLength());

        while (chars.getFirst() != null) {
        	ATermInt v = (ATermInt) chars.getFirst();
            result.append((char) v.getInt());
            chars = chars.getNext();
        }
        
        return result.toString();
    }
	
	private void reportAmbiguity(SGLRTokenizer tokenizer, ATermAppl amb, int startOffset) {
		if (!inLexicalContext) {
			IToken token = tokenizer.makeErrorToken(startOffset, offset - 1);
			reportWarningAtTokens(token, token, "Fragment is ambiguous: " + ambToString(amb));
		}
	}

	private String ambToString(ATermAppl amb) {
		if (asyncAmbReportingContext == null) {
			Context context = new Context();
			context.getCompatManager().setATermFactory(Environment.getATermFactory());
			asyncAmbReportingContext = stratego_sglr.init(context);
			stratego_aterm.init(asyncAmbReportingContext);
			sdf2imp.init(asyncAmbReportingContext);
		}

		if (Environment.getStrategoLock().isHeldByCurrentThread()) {
			// Potential deadlock (occurs when parsing a file for the first time, when it's probably safe)
			return amb.toString();
		}
		
		synchronized (asyncAmbReportingContext) {
			ITermFactory factory = asyncAmbReportingContext.getFactory();
			IStrategoTerm result;
			
			Environment.getStrategoLock().lock();
			try {
				result = TermConverter.convert(factory, Environment.getATermConverter().convert(amb));
			} finally {
				Environment.getStrategoLock().unlock();
			}
			
			result = factory.makeAppl(factory.makeConstructor("parsetree", 2), result, factory.makeInt(2));
			result = implode_asfix_0_0.instance.invoke(asyncAmbReportingContext, result);
			return ambToSimplifiedString(result);
		}
	}
	
	private static String ambToSimplifiedString(IStrategoTerm amb) {
		assert Thread.holdsLock(asyncAmbReportingContext);
		
		IStrategoTerm message = simplify_ambiguity_report_0_0.instance.invoke(asyncAmbReportingContext, amb);
		return message == null ? amb.toString() : ((IStrategoString) message).stringValue();
	}
	
	private void reportSkippedFragments(char[] inputChars, SGLRTokenizer tokenizer) {
		char[] processedChars = tokenizer.getLexStream().getInputChars();

		for (int i = 0; i < processedChars.length; i++) {
			char c = processedChars[i];
			if (c == SKIPPED_CHAR) {
				// Recovered by skipping a region
				int beginSkipped = i;
				int endSkipped = i;
				processedChars[i] = inputChars[i]; // restore input char
				while (++i < processedChars.length) {
					c = processedChars[i];
					if (c == SKIPPED_CHAR) {
						endSkipped = i;
						processedChars[i] = inputChars[i]; // restore input char
					} else if (!RecoveryConnector.isLayoutCharacter(c)) {
						break;
					}
				}
				reportSkippedFragment(inputChars, tokenizer, beginSkipped, endSkipped);
			} else if (c == UNEXPECTED_EOF_CHAR) {
				// Recovered using a forced reduction
				IToken token = tokenizer.makeErrorTokenBackwards(i);
				if (token.getStartOffset() == 0) break; // be less complainy about single-token files
				reportErrorAtTokens(token, token, "End of file unexpected");
			}
		}
		
		// Report forced reductions
		int treeEnd = tokenizer.getParseStream().getTokenAt(tokenizer.getParseStream().getStreamLength() - 1).getEndOffset();
		if (treeEnd < processedChars.length) {
			IToken token = tokenizer.makeErrorToken(treeEnd + 1, processedChars.length);
			reportErrorAtTokens(token, token, "Could not parse the remainder of this file");
			tokenizer.changeTokenKinds(treeEnd + 1, processedChars.length, TokenKind.TK_LAYOUT, TokenKind.TK_ERROR);
		}
	}

	private void reportSkippedFragment(char[] inputChars, SGLRTokenizer tokenizer, int beginSkipped, int endSkipped) {
		IToken token = tokenizer.makeErrorToken(beginSkipped, endSkipped);
		int line = token.getLine();
		int endLine = token.getEndLine() + RegionRecovery.NR_OF_LINES_TILL_SUCCESS;
		int reportedLine = -1;
		for (BadTokenException e : source.getParser().getParser().getCollectedErrors()) {
			if (e.getLineNumber() >= line && e.getLineNumber() <= endLine) {
				// tokenizer.getLexStream().getInputChars() may contain SKIPPED_CHAR characters,
				// so we switch it with inputChars
				char[] processedChars = tokenizer.getLexStream().getInputChars();
				tokenizer.getLexStream().setInputChars(inputChars);
				reportError(tokenizer, (Exception) e); // use double dispatch
				tokenizer.getLexStream().setInputChars(processedChars);
				if (reportedLine == -1)
					reportedLine = e.getLineNumber();
			}
		}
		tokenizer.changeTokenKinds(beginSkipped, endSkipped, TokenKind.TK_LAYOUT, TokenKind.TK_ERROR);
		if (reportedLine == -1) {
			// Report entire region
			reportErrorAtTokens(token, token, UNEXPECTED_REGION);
		} else if (reportedLine - line >= LARGE_REGION_SIZE) {
			IToken firstToken = token.getIPrsStream().getTokenAtCharacter(beginSkipped);
			IToken lastToken = tokenizer.getLastTokenOnSameLine(firstToken);
			reportErrorAtTokens(firstToken, lastToken, LARGE_REGION_START);
		}
	}
		
	public void reportError(SGLRTokenizer tokenizer, TokenExpectedException exception) {
		String message = exception.getShortMessage();
		IToken token = tokenizer.makeErrorToken(exception.getOffset());
		
		reportErrorAtTokens(token, token, message);
	}
	
	public void reportError(SGLRTokenizer tokenizer, BadTokenException exception) {
		IToken token = tokenizer.makeErrorToken(exception.getOffset());
		String message = exception.isEOFToken()
			? exception.getShortMessage()
			: "Syntax error near unexpected token '" + token + "'";
		reportErrorAtTokens(token, token, message);
	}
	
	public void reportError(SGLRTokenizer tokenizer, MultiBadTokenException exception) {
		for (BadTokenException e : exception.getCauses()) {
			reportError(tokenizer, (Exception) e); // use double dispatch
		}
	}
	
	public void reportError(SGLRTokenizer tokenizer, ParseTimeoutException exception) {
		String message = "Internal parsing error: " + exception.getMessage();
		reportErrorAtFirstLine(message);
		reportError(tokenizer, (MultiBadTokenException) exception);
		reportCollectedErrorsDirectly(tokenizer);
		setRushNextUpdate(true);
	}
	
	private void reportCollectedErrorsDirectly(SGLRTokenizer tokenizer) {
		for (BadTokenException e : source.getParser().getParser().getCollectedErrors()) {
			// tokenizer.getLexStream().getInputChars() may contain SKIPPED_CHAR characters,
			// so we have to use the message provided by the exceptions directly
			IToken token = tokenizer.makeErrorToken(e.getOffset());
			String message = e.getMessage();
			reportErrorAtTokens(token, token, message);
		}
	}
	 
	public void reportError(SGLRTokenizer tokenizer, Exception exception) {
		try {
			throw exception;
		} catch (ParseTimeoutException e) {
			reportError(tokenizer, (ParseTimeoutException) exception);
		} catch (TokenExpectedException e) {
			reportError(tokenizer, (TokenExpectedException) exception);
		} catch (MultiBadTokenException e) {
			reportError(tokenizer, (MultiBadTokenException) exception);
		} catch (BadTokenException e) {
			reportError(tokenizer, (BadTokenException) exception);
		} catch (Exception e) {
			String message = "Internal parsing error: " + exception;
			Environment.logException("Internal parsing error: " + exception.getMessage(), exception);
			reportErrorAtFirstLine(message);
			reportCollectedErrorsDirectly(tokenizer);
		}
	}
	
	private void reportErrorAtTokens(final IToken left, final IToken right, String message) {
		assert source.getParseLock().isHeldByCurrentThread();
		final String message2 = message + getErrorExplanation();
		
		errorReports.add(new Runnable() {
			public void run() {
				handler.addMarker(source.getResource(), left, right, message2, IMarker.SEVERITY_ERROR);
			}
		});
	}
	
	private void reportWarningAtTokens(final IToken left, final IToken right, final String message) {
		assert source.getParseLock().isHeldByCurrentThread();

		errorReports.add(new Runnable() {
			public void run() {
				handler.addMarker(source.getResource(), left, right, message, IMarker.SEVERITY_WARNING);
			}
		});
	}
	
	private void reportErrorAtFirstLine(String message) {
		assert source.getParseLock().isHeldByCurrentThread();
		final String message2 = message + getErrorExplanation();
		
		errorReports.add(new Runnable() {
			public void run() {
				handler.addMarkerFirstLine(source.getResource(), message2, IMarker.SEVERITY_ERROR);
			}
		});
	}	

	private String getErrorExplanation() {
		final String message2;
		if (isRecoveryFailed) {
			message2 = " (recovery failed)";
		} else if (!source.getParser().getParseTable().hasRecovers()) {
			message2 = " (no recovery rules in parse table)";
		} else {
			message2 = "";
		}
		return message2;
	}
	
	private static String getDeprecatedProductionMessage(ATermAppl attrs) {
		if ("attrs".equals(attrs.getName())) {
			ATermList attrList = termAt(attrs, 0);
			while (!attrList.isEmpty()) {
				ATermAppl attr = (ATermAppl) attrList.getFirst();
				attrList = attrList.getNext();
				if (attr.getName().equals("term")) {
					ATermAppl details = applAt(attr, 0);
					if (details.getName().equals("deprecated")) {
						if (details.getChildCount() == 1) {
							details = termAt(details, 0);
							return "Deprecated syntactic construct:" + details.getName();
						} else {
							return "Deprecated syntactic construct";
						}
					}
				}
			}
		}
		return null;
	}
}
