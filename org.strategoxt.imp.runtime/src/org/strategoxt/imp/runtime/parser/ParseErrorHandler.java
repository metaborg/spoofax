package org.strategoxt.imp.runtime.parser;

import static java.lang.Math.min;
import static org.spoofax.jsglr.client.imploder.AbstractTokenizer.findLeftMostTokenOnSameLine;
import static org.spoofax.jsglr.client.imploder.AbstractTokenizer.findRightMostTokenOnSameLine;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getTokenizer;
import static org.spoofax.terms.Term.asJavaString;
import static org.spoofax.terms.Term.tryGetConstructor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.MultiBadTokenException;
import org.spoofax.jsglr.client.ParseTimeoutException;
import org.spoofax.jsglr.client.RegionRecovery;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.Token;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.jsglr.shared.TokenExpectedException;
import org.spoofax.terms.TermVisitor;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.imp.generator.simplify_ambiguity_report_0_0;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AstMessageBatch;
import org.strategoxt.imp.runtime.parser.ast.AstMessageHandler;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.lang.Context;
import org.strategoxt.stratego_aterm.stratego_aterm;
import org.strategoxt.stratego_sglr.stratego_sglr;

/**
 * SGLR parse error reporting for a particular SGLR Parse controller and file. 
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class ParseErrorHandler {
	
	public static final int PARSE_ERROR_DELAY = min(StrategoObserver.OBSERVER_DELAY + 50, 800);
	
	private static final int LARGE_REGION_SIZE = 8;
	
	private static final String LARGE_REGION_START =
		"Region could not be parsed because of subsequent syntax error(s) indicated below";
	
	private static Context asyncAmbReportingContext;
	
	private final IStrategoConstructor ambCons = Environment.getTermFactory().makeConstructor("amb", 1); 
	
	private final AstMessageHandler handler = new AstMessageHandler(AstMessageHandler.PARSE_MARKER_TYPE);

	private final SGLRParseController source;
	
	private volatile boolean isRecoveryFailed = true;
	
	private volatile int additionsVersionId;
	
	private boolean rushNextUpdate;

	public ParseErrorHandler(SGLRParseController source) {
		this.source = source;
	}
	
	public void clearErrors() {
		assert source.getParseLock().isHeldByCurrentThread();
		
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
	public void gatherNonFatalErrors(IStrategoTerm top) {
		assert source.getParseLock().isHeldByCurrentThread();
		ITokenizer tokenizer = getTokenizer(top);
		for (int i = 0, max = tokenizer.getTokenCount(); i < max; i++) {
			IToken token = tokenizer.getTokenAt(i);
			String error = token.getError();
			if (error != null) {
				if (error == ITokenizer.ERROR_SKIPPED_REGION) {
					i = findRightMostWithSameError(token, null);
					reportSkippedRegion(token, tokenizer.getTokenAt(i));
				} else if (error.startsWith(ITokenizer.ERROR_WARNING_PREFIX)) {
					i = findRightMostWithSameError(token, null);
					reportWarningAtTokens(token, tokenizer.getTokenAt(i), error);
				} else if (error.startsWith(ITokenizer.ERROR_WATER_PREFIX)) {
					i = findRightMostWithSameError(token, ITokenizer.ERROR_WATER_PREFIX);
					reportErrorAtTokens(token, tokenizer.getTokenAt(i), error);
				} else {
					i = findRightMostWithSameError(token, null);
					// UNDONE: won't work for multi-token errors (as seen in SugarJ)
					reportErrorAtTokens(token, tokenizer.getTokenAt(i), error);
				}
			}
		}
		gatherAmbiguities(top);
	}

	private static int findRightMostWithSameError(IToken token, String prefix) {
		String expectedError = token.getError();
		ITokenizer tokenizer = token.getTokenizer();
		int i = token.getIndex();
		for (int max = tokenizer.getTokenCount(); i + 1 < max; i++) {
			String error = tokenizer.getTokenAt(i + 1).getError();
			if (error != expectedError
					&& (error == null || prefix == null || !error.startsWith(prefix)))
				break;
		}
		return i;
	}

    /**
     * Report recoverable errors (e.g., inserted brackets).
     * 
	 * @param outerBeginOffset  The begin offset of the enclosing construct.
     */
	private void gatherAmbiguities(IStrategoTerm term) {
		new TermVisitor() {
			IStrategoTerm ambStart;
			
			public void preVisit(IStrategoTerm term) {
				if (ambStart == null && ambCons == tryGetConstructor(term)) {
					reportAmbiguity(term);
					ambStart = term;
				}
			}
			
			@Override
			public void postVisit(IStrategoTerm term) {
				if (term == ambStart) ambStart = null;
			}
		}.visit(term);
	}
	
	private void reportAmbiguity(IStrategoTerm amb) {
		reportWarningAtTokens(getLeftToken(amb), getRightToken(amb),
				"Fragment is ambiguous: " + ambToString(amb));
	}

	private String ambToString(IStrategoTerm amb) {
		String result = amb.toString();
		
		if (!Environment.getStrategoLock().isHeldByCurrentThread()) {
			// ^ avoid potential deadlock (occurs when parsing a file for the first time, when it's probably safe)
			synchronized (ParseErrorHandler.class) {
				Environment.getStrategoLock().lock();
				try {
					if (asyncAmbReportingContext == null) {
						Context context = new Context();
						asyncAmbReportingContext = stratego_sglr.init(context);
						stratego_aterm.init(asyncAmbReportingContext);
						sdf2imp.init(asyncAmbReportingContext);
					}

					IStrategoTerm message = simplify_ambiguity_report_0_0.instance.invoke(asyncAmbReportingContext, amb);
					if (message != null)
						result = asJavaString(message);
				} finally {
					Environment.getStrategoLock().unlock();
				}
			}
		}
		
		return result.length() > 5000 ? result.substring(0, 5000) + "..." : result;
	}

	private void reportSkippedRegion(IToken left, IToken right) {
		// Find a parse failure(s) in the given token range
		int line = left.getLine();
		int reportedLine = -1;
		for (BadTokenException e : getCollectedErrorsInRegion(source.getParser(), left, right, true)) {
			reportException(left.getTokenizer(), e); // use double dispatch
			if (reportedLine == -1)
				reportedLine = e.getLineNumber();
		}
			
		if (reportedLine == -1) {
			// Report entire region
			reportErrorAtTokens(left, right, ITokenizer.ERROR_SKIPPED_REGION);
		} else if (reportedLine - line >= LARGE_REGION_SIZE) {
			// Warn at start of region
			reportErrorAtTokens(findLeftMostTokenOnSameLine(left),
					findRightMostTokenOnSameLine(left), LARGE_REGION_START);
		}
	}
	
	private static List<BadTokenException> getCollectedErrorsInRegion(JSGLRI parser, IToken left, IToken right, boolean alsoOutside) {
		assert JSGLRI.class.isInstance(parser) : "required for SugarJ, as it overrides getCollectedErrors()";
		List<BadTokenException> results = new ArrayList<BadTokenException>();
		int line = left.getLine();
		int endLine = right.getLine() + (alsoOutside ? RegionRecovery.NR_OF_LINES_TILL_SUCCESS : 0);
		for (BadTokenException e : parser.getCollectedErrors()) {
			if (e.getLineNumber() >= line && e.getLineNumber() <= endLine)
				results.add(e);
		}
		return results;
	}

	/**
	 * Schedules delayed error marking for errors not committed yet.
	 * 
	 * @see AstMessageHandler#commitAllChanges()
	 */
	public void scheduleCommitAllChanges() {
		assert source.getParseLock().isHeldByCurrentThread();
		
		final int expectedVersion = ++additionsVersionId;
		final AstMessageBatch batch = handler.closeBatch();
		final boolean wasRushNextUpdate = rushNextUpdate;
		
		rushNextUpdate = false;
		
		if (batch.isEmpty()) return;
		
		UIJob immediateJob = new UIJob("Report parse errors") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				// Commit additions that can reuse an existing marker,
				// additions on the same line as an existing marker and
				// deletions immediately.
				if (additionsVersionId != expectedVersion) return Status.OK_STATUS;
				batch.commitReuseableAdditions();
				batch.commitMultiErrorLineAdditions();
				batch.commitDeletions();

				if (batch.isEmpty()) return Status.OK_STATUS;
				
				if (wasRushNextUpdate) {
					batch.commitAdditions();
				} else {
					UIJob delayedJob = new UIJob("Report parse errors") {
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							// Commit all other additions after a delay.
							// (to prevent flickering error markers)
							if (additionsVersionId != expectedVersion) return Status.OK_STATUS;
							batch.commitAdditions();
							return Status.OK_STATUS;
						}
					};
					delayedJob.setSystem(true);
					delayedJob.schedule((long) (PARSE_ERROR_DELAY * (isRecoveryFailed ? 1.5 : 1)));
				}
				return Status.OK_STATUS;
			}
		};
		immediateJob.setSystem(true);
		immediateJob.schedule(0);
	}

	public void abortScheduledCommit() {
		additionsVersionId++;
	}
		
	private void reportTokenExpected(ITokenizer tokenizer, TokenExpectedException exception) {
		String message = exception.getShortMessage();
		reportErrorNearOffset(tokenizer, exception.getOffset(), message);
	}
	
	private void reportBadToken(ITokenizer tokenizer, BadTokenException exception) {
		String message;
		if (exception.isEOFToken() || tokenizer.getTokenCount() <= 1) {
			message = exception.getShortMessage();
		} else {
			IToken token = tokenizer.getTokenAtOffset(exception.getOffset());
			token = findNextNonEmptyToken(token);
			message = ITokenizer.ERROR_WATER_PREFIX + ": " + token.toString().trim();
		}
		reportErrorNearOffset(tokenizer, exception.getOffset(), message);
	}
	
	private void reportMultiBadToken(ITokenizer tokenizer, MultiBadTokenException exception) {
		for (BadTokenException e : exception.getCauses()) {
			reportException(tokenizer, e); // use double dispatch
		}
	}
	
	private void reportTimeOut(ITokenizer tokenizer, ParseTimeoutException exception) {
		String message = "Internal parsing error: " + exception.getMessage();
		reportErrorAtFirstLine(message);
		reportMultiBadToken(tokenizer, exception);
		reportAllParseFailures(tokenizer);
		setRushNextUpdate(true);
	}
	
	private void reportAllParseFailures(ITokenizer tokenizer) {
		for (BadTokenException e : source.getParser().getParser().getCollectedErrors()) {
			// tokenizer.getLexStream().getInputChars() may contain SKIPPED_CHAR characters,
			// so we have to use the message provided by the exceptions directly
			String message = e.getMessage();
			reportErrorNearOffset(tokenizer, e.getOffset(), message);
		}
	}

	public void reportException(ITokenizer tokenizer, Exception exception) {
		try {
			throw exception;
		} catch (ParseTimeoutException e) {
			reportTimeOut(tokenizer, (ParseTimeoutException) exception);
		} catch (TokenExpectedException e) {
			reportTokenExpected(tokenizer, (TokenExpectedException) exception);
		} catch (MultiBadTokenException e) {
			reportMultiBadToken(tokenizer, (MultiBadTokenException) exception);
		} catch (BadTokenException e) {
			reportBadToken(tokenizer, (BadTokenException) exception);
		} catch (Exception e) {
			String message = "Internal parsing error: " + exception;
			Environment.logException("Internal parsing error: " + exception.getMessage(), exception);
			reportErrorAtFirstLine(message);
			reportAllParseFailures(tokenizer);
		}
	}

	private void reportErrorNearOffset(ITokenizer tokenizer, int offset, String message) {
		IToken errorToken = tokenizer.getErrorTokenOrAdjunct(offset);
		reportErrorAtTokens(errorToken, errorToken, message);
	}
	 
	private static IToken findNextNonEmptyToken(IToken token) {
		ITokenizer tokenizer = token.getTokenizer();
		IToken result = null;
		for (int i = token.getIndex(), max = tokenizer.getTokenCount(); i < max; i++) {
			result = tokenizer.getTokenAt(i);
			if (result.getLength() != 0 && !Token.isWhiteSpace(result)) break;
		}
		return result;
	}
	
	private void reportErrorAtTokens(final IToken left, final IToken right, String message) {
		assert source.getParseLock().isHeldByCurrentThread();
		
		if (left.getStartOffset() > right.getEndOffset()) {
			reportErrorNearOffset(left.getTokenizer(), left.getStartOffset(), message);
		} else {
			String message2 = message + getErrorExplanation();
			handler.addMarker(source.getResource(), left, right, message2, IMarker.SEVERITY_ERROR);
		}
	}
	
	private void reportWarningAtTokens(final IToken left, final IToken right, final String message) {
		assert source.getParseLock().isHeldByCurrentThread();

		handler.addMarker(source.getResource(), left, right, message, IMarker.SEVERITY_WARNING);
	}
	
	private void reportErrorAtFirstLine(String message) {
		assert source.getParseLock().isHeldByCurrentThread();
		final String message2 = message + getErrorExplanation();
		
		handler.addMarkerFirstLine(source.getResource(), message2, IMarker.SEVERITY_ERROR);
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
}
