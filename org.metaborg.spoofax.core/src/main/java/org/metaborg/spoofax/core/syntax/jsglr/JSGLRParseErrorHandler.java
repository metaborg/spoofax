package org.metaborg.spoofax.core.syntax.jsglr;

import static org.spoofax.jsglr.client.imploder.AbstractTokenizer.findLeftMostTokenOnSameLine;
import static org.spoofax.jsglr.client.imploder.AbstractTokenizer.findRightMostTokenOnSameLine;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getTokenizer;
import static org.spoofax.terms.Term.tryGetConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.ISourceRegion;
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

import com.google.common.collect.Lists;

public class JSGLRParseErrorHandler {
    private static final int LARGE_REGION_SIZE = 8;
    private static final String LARGE_REGION_START =
        "Region could not be parsed because of subsequent syntax error(s) indicated below";

    private final JSGLRI parser;
    private final FileObject resource;
    private final boolean hasRecoveryRules;
    private final IStrategoConstructor ambiguityConstructor;
    private final Collection<IMessage> messages = Lists.newArrayList();

    private boolean recoveryFailed;


    public JSGLRParseErrorHandler(JSGLRI parser, ITermFactory termFactory, FileObject resource, boolean hasRecoveryRules) {
        this.parser = parser;
        this.resource = resource;
        this.hasRecoveryRules = hasRecoveryRules;
        this.ambiguityConstructor = termFactory.makeConstructor("amb", 1);
    }


    public Iterable<IMessage> messages() {
        return messages;
    }

    public void setRecoveryFailed(boolean recoveryFailed) {
        this.recoveryFailed = recoveryFailed;
    }


    /*
     * Non-fatal (recoverable) errors
     */

    public void gatherNonFatalErrors(IStrategoTerm top) {
        final ITokenizer tokenizer = getTokenizer(top);
        for(int i = 0, max = tokenizer.getTokenCount(); i < max; i++) {
            final IToken token = tokenizer.getTokenAt(i);
            final String error = token.getError();
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
        final String expectedError = token.getError();
        final ITokenizer tokenizer = token.getTokenizer();
        int i = token.getIndex();
        for(int max = tokenizer.getTokenCount(); i + 1 < max; i++) {
            String error = tokenizer.getTokenAt(i + 1).getError();
            if(error != expectedError && (error == null || prefix == null || !error.startsWith(prefix)))
                break;
        }
        return i;
    }

    private void reportSkippedRegion(IToken left, IToken right) {
        // Find a parse failure(s) in the given token range
        int line = left.getLine();
        int reportedLine = -1;
        for(BadTokenException e : getCollectedErrorsInRegion(left, right, true)) {
            processFatalException(left.getTokenizer(), e);
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

    private List<BadTokenException> getCollectedErrorsInRegion(IToken left, IToken right, boolean alsoOutside) {
        final List<BadTokenException> results = new ArrayList<BadTokenException>();
        final int line = left.getLine();
        final int endLine = right.getLine() + (alsoOutside ? RegionRecovery.NR_OF_LINES_TILL_SUCCESS : 0);
        for(BadTokenException e : parser.getParser().getCollectedErrors()) {
            if(e.getLineNumber() >= line && e.getLineNumber() <= endLine)
                results.add(e);
        }
        return results;
    }

    private void gatherAmbiguities(IStrategoTerm term) {
        new TermVisitor() {
            IStrategoTerm ambStart;

            public void preVisit(IStrategoTerm term) {
                if(ambStart == null && ambiguityConstructor.equals(tryGetConstructor(term))) {
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
        reportWarningAtTokens(getLeftToken(amb), getRightToken(amb), "Fragment is ambiguous: " + ambToString(amb));
    }

    private String ambToString(IStrategoTerm amb) {
        final String result = amb.toString();

        // if(asyncAmbReportingContext == null) {
        // Context context = new Context();
        // asyncAmbReportingContext = stratego_sglr.init(context);
        // stratego_aterm.init(asyncAmbReportingContext);
        // sdf2imp.init(asyncAmbReportingContext);
        // }
        // IStrategoTerm message = simplify_ambiguity_report_0_0.instance.invoke(asyncAmbReportingContext,
        // amb);
        // if(message != null)
        // result = asJavaString(message);

        return result.length() > 5000 ? result.substring(0, 5000) + "..." : result;
    }


    /*
     * Fatal errors
     */

    public void processFatalException(ITokenizer tokenizer, Exception exception) {
        try {
            throw exception;
        } catch(ParseTimeoutException e) {
            reportTimeOut(tokenizer, e);
        } catch(TokenExpectedException e) {
            reportTokenExpected(tokenizer, e);
        } catch(MultiBadTokenException e) {
            reportMultiBadToken(tokenizer, e);
        } catch(BadTokenException e) {
            reportBadToken(tokenizer, e);
        } catch(Exception e) {
            createErrorAtFirstLine("Internal parsing error: " + e);
        }
    }

    private void reportTimeOut(ITokenizer tokenizer, ParseTimeoutException exception) {
        final String message = "Internal parsing error: " + exception.getMessage();
        createErrorAtFirstLine(message);
        reportMultiBadToken(tokenizer, exception);
    }

    private void reportTokenExpected(ITokenizer tokenizer, TokenExpectedException exception) {
        final String message = exception.getShortMessage();
        reportErrorNearOffset(tokenizer, exception.getOffset(), message);
    }

    private void reportMultiBadToken(ITokenizer tokenizer, MultiBadTokenException exception) {
        for(BadTokenException e : exception.getCauses()) {
            processFatalException(tokenizer, e);
        }
    }

    private void reportBadToken(ITokenizer tokenizer, BadTokenException exception) {
        final String message;
        if(exception.isEOFToken() || tokenizer.getTokenCount() <= 1) {
            message = exception.getShortMessage();
        } else {
            IToken token = tokenizer.getTokenAtOffset(exception.getOffset());
            token = findNextNonEmptyToken(token);
            message = ITokenizer.ERROR_WATER_PREFIX + ": " + token.toString().trim();
        }
        reportErrorNearOffset(tokenizer, exception.getOffset(), message);
    }


    private void reportErrorNearOffset(ITokenizer tokenizer, int offset, String message) {
        final IToken errorToken = tokenizer.getErrorTokenOrAdjunct(offset);
        final ISourceRegion region = JSGLRSourceRegionFactory.fromTokens(errorToken, errorToken);
        reportErrorAtRegion(region, message);
    }

    private static IToken findNextNonEmptyToken(IToken token) {
        final ITokenizer tokenizer = token.getTokenizer();
        IToken result = null;
        for(int i = token.getIndex(), max = tokenizer.getTokenCount(); i < max; i++) {
            result = tokenizer.getTokenAt(i);
            if(result.getLength() != 0 && !Token.isWhiteSpace(result))
                break;
        }
        return result;
    }


    private void createErrorAtFirstLine(String message) {
        messages.add(MessageFactory.newParseErrorAtTop(resource, message + getErrorExplanation(), null));
    }

    private void reportErrorAtTokens(IToken left, IToken right, String message) {
        reportErrorAtRegion(JSGLRSourceRegionFactory.fromTokens(left, right), message);
    }

    private void reportErrorAtRegion(ISourceRegion sourceRegion, String message) {
        messages.add(MessageFactory.newParseError(resource, sourceRegion, message, null));
    }

    private void reportWarningAtTokens(IToken left, IToken right, final String message) {
        reportWarningAtRegion(JSGLRSourceRegionFactory.fromTokens(left, right), message);
    }

    private void reportWarningAtRegion(ISourceRegion sourceRegion, final String message) {
        messages.add(MessageFactory.newParseWarning(resource, sourceRegion, message, null));
    }


    private String getErrorExplanation() {
        if(recoveryFailed) {
            return " (recovery failed)";
        } else if(!hasRecoveryRules) {
            return " (no recovery rules in parse table)";
        } else {
            return "";
        }
    }
}
