package org.metaborg.spoofax.core.messages;

import static org.spoofax.interpreter.core.Tools.*;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.*;
import static org.spoofax.terms.attachments.OriginAttachment.tryGetOrigin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.parser.jsglr.JSGLRSourceRegionFactory;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.strategoxt.imp.generator.postprocess_feedback_results_0_0;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.lang.Context;

public class MessageFactory {
    public static Collection<IMessage> makeMessages(FileObject file, MessageSeverity severity,
        IStrategoList msgs) {
        final Collection<IMessage> result = new ArrayList<IMessage>(msgs.getSubtermCount());

        final Context context = new Context();
        sdf2imp.init(context);
        final IStrategoList processedMsgs =
            (IStrategoList) postprocess_feedback_results_0_0.instance.invoke(context, msgs);

        for(IStrategoTerm msg : processedMsgs.getAllSubterms()) {
            IStrategoTerm term;
            String message;
            if(isTermTuple(msg) && msg.getSubtermCount() == 2) {
                term = termAt(msg, 0);
                IStrategoString messageTerm = termAt(msg, 1);
                message = messageTerm.stringValue();
            } else {
                term = msg;
                message = msg.toString() + " (no tree node indicated)";
            }

            final ISimpleTerm node = minimizeMarkerSize(getClosestAstNode(term));

            if(node != null) {
                final IToken left = getLeftToken(node);
                final IToken right = getRightToken(node);
                result.add(newAnalysisMessage(file, left, right, message, severity));
            } else {
                result.add(newAnalysisMessageAtTop(file, message, severity));
            }
        }

        return result;
    }


    public static Message newMessage(FileObject file, IToken left, IToken right, String msg,
        MessageSeverity severity, MessageType type) {
        String sourceText;
        ISourceRegion region;
        try {
            sourceText = getSourceTextFromTokens(left, right);
            if(sourceText == null) {
                sourceText = getSourceTextFromResource(file);
            }
            region = JSGLRSourceRegionFactory.fromSourceText(left, right, sourceText);
        } catch(IOException e) {
            sourceText = null;
            region = JSGLRSourceRegionFactory.fromTokens(left, right);
        }
        return new Message(msg, severity, type, file, sourceText, region, null);
    }


    public static Message newParseMessage(FileObject file, IToken left, IToken right, String msg,
        MessageSeverity severity) {
        return newMessage(file, left, right, msg, MessageSeverity.ERROR, MessageType.PARSER_MESSAGE);
    }

    public static Message newParseError(FileObject file, IToken left, IToken right, String msg) {
        return newParseMessage(file, left, right, msg, MessageSeverity.ERROR);
    }

    public static Message newParseWarning(FileObject file, IToken left, IToken right, String msg) {
        return newParseMessage(file, left, right, msg, MessageSeverity.WARNING);
    }


    public static Message newAnalysisMessage(FileObject file, IToken left, IToken right, String msg,
        MessageSeverity severity) {
        return newMessage(file, left, right, msg, severity, MessageType.ANALYSIS_MESSAGE);
    }

    public static Message newAnalysisError(FileObject file, IToken left, IToken right, String msg) {
        return newAnalysisMessage(file, left, right, msg, MessageSeverity.ERROR);
    }

    public static Message newAnalysisWarning(FileObject file, IToken left, IToken right, String msg) {
        return newAnalysisMessage(file, left, right, msg, MessageSeverity.WARNING);
    }

    public static Message newAnalysisNote(FileObject file, IToken left, IToken right, String msg) {
        return newAnalysisMessage(file, left, right, msg, MessageSeverity.NOTE);
    }


    private static Message newAtTop(FileObject file, String msg, MessageType type, MessageSeverity severity) {
        String sourceText;
        try {
            sourceText = getSourceTextFromResource(file);
        } catch(IOException e) {
            sourceText = null;
        }
        return new Message(msg, severity, type, file, sourceText, new SourceRegion(0, 0, 1, 0), null);
    }


    private static Message newErrorAtTop(FileObject file, String msg, MessageType type) {
        return newAtTop(file, msg, type, MessageSeverity.ERROR);
    }

    private static Message newWarningAtTop(FileObject file, String msg, MessageType type) {
        return newAtTop(file, msg, type, MessageSeverity.WARNING);
    }


    public static Message newParseErrorAtTop(FileObject file, String msg) {
        return newErrorAtTop(file, msg, MessageType.PARSER_MESSAGE);
    }

    public static Message newParseWarningAtTop(FileObject file, String msg) {
        return newWarningAtTop(file, msg, MessageType.PARSER_MESSAGE);
    }


    public static Message newAnalysisMessageAtTop(FileObject file, String msg, MessageSeverity severity) {
        return newAtTop(file, msg, MessageType.ANALYSIS_MESSAGE, severity);
    }

    public static Message newAnalysisErrorAtTop(FileObject file, String msg) {
        return newErrorAtTop(file, msg, MessageType.ANALYSIS_MESSAGE);
    }

    public static Message newAnalysisWarningAtTop(FileObject file, String msg) {
        return newWarningAtTop(file, msg, MessageType.ANALYSIS_MESSAGE);
    }


    public static Message newBuilderErrorAtTop(FileObject file, String msg) {
        return newErrorAtTop(file, msg, MessageType.BUILDER_MESSAGE);
    }

    public static Message newBuilderWarningAtTop(FileObject file, String msg) {
        return newWarningAtTop(file, msg, MessageType.BUILDER_MESSAGE);
    }


    private static @Nullable String getSourceTextFromTokens(IToken left, IToken right) {
        String input = null;
        input = left.getTokenizer().getInput();
        if(input == null) {
            input = right.getTokenizer().getInput();
        }
        return input;
    }

    private static String getSourceTextFromResource(FileObject resource) throws IOException {
        return IOUtils.toString(resource.getContent().getInputStream());
    }


    /**
     * Given a Stratego term, get the first AST node associated with any of its subterms, doing a depth-first
     * search.
     */
    private static ISimpleTerm getClosestAstNode(IStrategoTerm term) {
        if(hasImploderOrigin(term)) {
            return tryGetOrigin(term);
        } else if(term == null) {
            return null;
        } else {
            for(int i = 0; i < term.getSubtermCount(); i++) {
                ISimpleTerm result = getClosestAstNode(termAt(term, i));
                if(result != null)
                    return result;
            }
            return null;
        }
    }

    private static ISimpleTerm minimizeMarkerSize(ISimpleTerm node) {
        // TODO: prefer lexical nodes when minimizing marker size? (e.g., not 'private')
        if(node == null)
            return null;
        while(getLeftToken(node).getLine() < getRightToken(node).getLine()) {
            if(node.getSubtermCount() == 0)
                break;
            node = node.getSubterm(0);
        }
        return node;
    }
}
