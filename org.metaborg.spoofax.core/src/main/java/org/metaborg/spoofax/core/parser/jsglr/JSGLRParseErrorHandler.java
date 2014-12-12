package org.metaborg.spoofax.core.parser.jsglr;

import static org.spoofax.jsglr.client.imploder.AbstractTokenizer.*;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.*;
import static org.spoofax.terms.Term.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.MessageFactory;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
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
import org.strategoxt.lang.Context;
import org.strategoxt.stratego_aterm.stratego_aterm;
import org.strategoxt.stratego_sglr.stratego_sglr;

import com.google.common.collect.Sets;

/**
 * SGLR parse error reporting for a particular SGLR Parse controller and file.
 */
public class JSGLRParseErrorHandler {
    private static final int LARGE_REGION_SIZE = 8;
    private static final String LARGE_REGION_START =
        "Region could not be parsed because of subsequent syntax error(s) indicated below";

    private static Context asyncAmbReportingContext;

    private final IStrategoConstructor ambCons;
    private final JSGLRI source;
    private final Collection<IMessage> messages = Sets.newHashSet();
    private volatile boolean isRecoveryFailed = true;


    public JSGLRParseErrorHandler(JSGLRI source, ITermFactory termFactory) {
        this.source = source;
        this.ambCons = termFactory.makeConstructor("amb", 1);
    }


    public void reset() {
        messages.clear();
    }

    /**
     * Informs the parse error handler that recovery is unavailable. This information is reflected in any
     * parse error messages.
     */
    public void setRecoveryFailed(boolean recoveryFailed) {
        this.isRecoveryFailed = recoveryFailed;
    }

    public boolean isRecoveryFailed() {
        return isRecoveryFailed;
    }

    /**
     * Report WATER + INSERT errors from parse tree
     */
    public void gatherNonFatalErrors(IStrategoTerm top) {
        ITokenizer tokenizer = getTokenizer(top);
        for(int i = 0, max = tokenizer.getTokenCount(); i < max; i++) {
            IToken token = tokenizer.getTokenAt(i);
            String error = token.getError();
            if(error != null) {
                if(error == ITokenizer.ERROR_SKIPPED_REGION) {
                    i = findRightMostWithSameError(token, null);
                    reportSkippedRegion(token, tokenizer.getTokenAt(i));
                } else if(error.startsWith(ITokenizer.ERROR_WARNING_PREFIX)) {
                    i = findRightMostWithSameError(token, null);
                    reportWarningAtTokens(token, tokenizer.getTokenAt(i), error);
                } else if(error.startsWith(ITokenizer.ERROR_WATER_PREFIX)) {
                    i = findRightMostWithSameError(token, ITokenizer.ERROR_WATER_PREFIX);
                    reportErrorAtTokens(token, tokenizer.getTokenAt(i), error);
                } else {
                    i = findRightMostWithSameError(token, null);
                    // UNDONE: won't work for multi-token errors (as seen in
                    // SugarJ)
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
        for(int max = tokenizer.getTokenCount(); i + 1 < max; i++) {
            String error = tokenizer.getTokenAt(i + 1).getError();
            if(error != expectedError && (error == null || prefix == null || !error.startsWith(prefix)))
                break;
        }
        return i;
    }

    /**
     * Report recoverable errors (e.g., inserted brackets).
     * 
     * @param outerBeginOffset
     *            The begin offset of the enclosing construct.
     */
    private void gatherAmbiguities(IStrategoTerm term) {
        new TermVisitor() {
            IStrategoTerm ambStart;

            public void preVisit(IStrategoTerm term) {
                if(ambStart == null && ambCons == tryGetConstructor(term)) {
                    reportAmbiguity(term);
                    ambStart = term;
                }
            }

            @Override public void postVisit(IStrategoTerm term) {
                if(term == ambStart)
                    ambStart = null;
            }
        }.visit(term);
    }

    private void reportAmbiguity(IStrategoTerm amb) {
        reportWarningAtTokens(getLeftToken(amb), getRightToken(amb), "Fragment is ambiguous: "
            + ambToString(amb));
    }

    private String ambToString(IStrategoTerm amb) {
        String result = amb.toString();

        if(asyncAmbReportingContext == null) {
            Context context = new Context();
            asyncAmbReportingContext = stratego_sglr.init(context);
            stratego_aterm.init(asyncAmbReportingContext);
            sdf2imp.init(asyncAmbReportingContext);
        }

        IStrategoTerm message = simplify_ambiguity_report_0_0.instance.invoke(asyncAmbReportingContext, amb);
        if(message != null)
            result = asJavaString(message);

        return result.length() > 5000 ? result.substring(0, 5000) + "..." : result;
    }

    private void reportSkippedRegion(IToken left, IToken right) {
        // Find a parse failure(s) in the given token range
        int line = left.getLine();
        int reportedLine = -1;
        for(BadTokenException e : getCollectedErrorsInRegion(source, left, right, true)) {
            gatherException(left.getTokenizer(), e); // use double dispatch
            if(reportedLine == -1)
                reportedLine = e.getLineNumber();
        }

        if(reportedLine == -1) {
            // Report entire region
            reportErrorAtTokens(left, right, ITokenizer.ERROR_SKIPPED_REGION);
        } else if(reportedLine - line >= LARGE_REGION_SIZE) {
            // Warn at start of region
            reportErrorAtTokens(findLeftMostTokenOnSameLine(left), findRightMostTokenOnSameLine(left),
                LARGE_REGION_START);
        }
    }

    private static List<BadTokenException> getCollectedErrorsInRegion(JSGLRI parser, IToken left,
        IToken right, boolean alsoOutside) {
        final List<BadTokenException> results = new ArrayList<BadTokenException>();
        final int line = left.getLine();
        final int endLine = right.getLine() + (alsoOutside ? RegionRecovery.NR_OF_LINES_TILL_SUCCESS : 0);
        for(BadTokenException e : parser.getParser().getCollectedErrors()) {
            if(e.getLineNumber() >= line && e.getLineNumber() <= endLine)
                results.add(e);
        }
        return results;
    }

    private void reportTokenExpected(ITokenizer tokenizer, TokenExpectedException exception) {
        String message = exception.getShortMessage();
        reportErrorNearOffset(tokenizer, exception.getOffset(), message);
    }

    private void reportBadToken(ITokenizer tokenizer, BadTokenException exception) {
        String message;
        if(exception.isEOFToken() || tokenizer.getTokenCount() <= 1) {
            message = exception.getShortMessage();
        } else {
            IToken token = tokenizer.getTokenAtOffset(exception.getOffset());
            token = findNextNonEmptyToken(token);
            message = ITokenizer.ERROR_WATER_PREFIX + ": " + token.toString().trim();
        }
        reportErrorNearOffset(tokenizer, exception.getOffset(), message);
    }

    private void reportMultiBadToken(ITokenizer tokenizer, MultiBadTokenException exception) {
        for(BadTokenException e : exception.getCauses()) {
            gatherException(tokenizer, e); // use double dispatch
        }
    }

    private void reportTimeOut(ITokenizer tokenizer, ParseTimeoutException exception) {
        String message = "Internal parsing error: " + exception.getMessage();
        reportErrorAtFirstLine(message);
        reportMultiBadToken(tokenizer, exception);
        reportAllParseFailures(tokenizer);
    }

    private void reportAllParseFailures(ITokenizer tokenizer) {
        for(BadTokenException e : source.getParser().getCollectedErrors()) {
            // tokenizer.getLexStream().getInputChars() may contain SKIPPED_CHAR
            // characters,
            // so we have to use the message provided by the exceptions directly
            String message = e.getMessage();
            reportErrorNearOffset(tokenizer, e.getOffset(), message);
        }
    }

    public void gatherException(ITokenizer tokenizer, Exception exception) {
        try {
            throw exception;
        } catch(ParseTimeoutException e) {
            reportTimeOut(tokenizer, (ParseTimeoutException) exception);
        } catch(TokenExpectedException e) {
            reportTokenExpected(tokenizer, (TokenExpectedException) exception);
        } catch(MultiBadTokenException e) {
            reportMultiBadToken(tokenizer, (MultiBadTokenException) exception);
        } catch(BadTokenException e) {
            reportBadToken(tokenizer, (BadTokenException) exception);
        } catch(Exception e) {
            String message = "Internal parsing error: " + exception;
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
        for(int i = token.getIndex(), max = tokenizer.getTokenCount(); i < max; i++) {
            result = tokenizer.getTokenAt(i);
            if(result.getLength() != 0 && !Token.isWhiteSpace(result))
                break;
        }
        return result;
    }

    private void reportErrorAtTokens(final IToken left, final IToken right, String message) {
        final FileObject file = source.getFile();
        if(left.getStartOffset() > right.getEndOffset()) {
            if(left != right) {
                reportErrorNearOffset(left.getTokenizer(), left.getStartOffset(), message);
            } else {
                String message2 = message + getErrorExplanation();
                messages.add(MessageFactory.newParseError(file, left, right, message2));
            }
        } else {
            String message2 = message + getErrorExplanation();
            messages.add(MessageFactory.newParseError(file, left, right, message2));
        }
    }

    private void reportWarningAtTokens(final IToken left, final IToken right, final String message) {
        final FileObject file = source.getFile();
        messages.add(MessageFactory.newParseWarning(file, left, right, message));
    }

    private void reportErrorAtFirstLine(String message) {
        final FileObject file = source.getFile();
        final String message2 = message + getErrorExplanation();
        messages.add(MessageFactory.newParseErrorAtTop(file, message2));
    }

    private String getErrorExplanation() {
        final String message2;
        boolean hasRecovers = false;
        hasRecovers = source.getConfig().getParseTableProvider().parseTable().hasRecovers();
        if(isRecoveryFailed) {
            message2 = " (recovery failed)";
        } else if(!hasRecovers) {
            message2 = " (no recovery rules in parse table)";
        } else {
            message2 = "";
        }
        return message2;
    }

    public Iterable<IMessage> getCollectedMessages() {
        return messages;
    }
}
