package org.metaborg.spoofax.core.analysis;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.spoofax.core.syntax.JSGLRSourceRegionFactory;
import org.spoofax.interpreter.core.StackTracer;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.Term;
import org.spoofax.terms.TermVisitor;
import org.spoofax.terms.attachments.OriginAttachment;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;

public class AnalysisCommon {
    public static String analysisFailedMessage(HybridInterpreter interpreter) {
        final StackTracer stackTracer = interpreter.getContext().getStackTracer();
        return "Analysis failed\nStratego stack trace:\n" + stackTracer.getTraceString();
    }

    public static Collection<IMessage> messages(FileObject resource, MessageSeverity severity,
        IStrategoTerm messagesTerm) {
        final Collection<IMessage> messages = Lists.newArrayListWithExpectedSize(messagesTerm.getSubtermCount());

        for(IStrategoTerm term : messagesTerm.getAllSubterms()) {
            final IStrategoTerm originTerm;
            final String message;
            if(term.getSubtermCount() == 2) {
                originTerm = term.getSubterm(0);
                message = toString(term.getSubterm(1));
            } else {
                originTerm = term;
                message = toString(term) + " (no tree node indicated)";
            }

            final ISimpleTerm node = minimizeMarkerSize(getClosestAstNode(originTerm));

            if(node != null) {
                final IToken left = ImploderAttachment.getLeftToken(node);
                final IToken right = ImploderAttachment.getRightToken(node);
                final ISourceRegion region = JSGLRSourceRegionFactory.fromTokens(left, right);
                messages.add(MessageFactory.newAnalysisMessage(resource, region, message, severity, null));
            } else {
                messages.add(MessageFactory.newAnalysisMessageAtTop(resource, message + " (no origin information)",
                    severity, null));
            }
        }

        return messages;
    }


    private static String toString(IStrategoTerm term) {
        if(term instanceof IStrategoString) {
            final IStrategoString messageStringTerm = (IStrategoString) term;
            return messageStringTerm.stringValue();
        } else if(term instanceof IStrategoList) {
            final StringBuilder sb = new StringBuilder();
            boolean first = true;
            for(IStrategoTerm subterm : term) {
                if(!first) {
                    sb.append(' ');
                }
                sb.append(toString(subterm));
                first = false;
            }
            return sb.toString();
        } else {
            return term.toString();
        }
    }

    private static ISimpleTerm minimizeMarkerSize(ISimpleTerm node) {
        // TODO: prefer lexical nodes when minimizing marker size? (e.g., not 'private')
        if(node == null)
            return null;
        while(ImploderAttachment.getLeftToken(node).getLine() < ImploderAttachment.getRightToken(node).getLine()) {
            if(node.getSubtermCount() == 0)
                break;
            node = node.getSubterm(0);
        }
        return node;
    }

    /**
     * Given a Stratego term, get the first AST node associated with any of its subterms, doing a depth-first search.
     */
    private static ISimpleTerm getClosestAstNode(IStrategoTerm term) {
        if(ImploderAttachment.hasImploderOrigin(term)) {
            return OriginAttachment.tryGetOrigin(term);
        } else if(term == null) {
            return null;
        } else {
            for(int i = 0; i < term.getSubtermCount(); i++) {
                ISimpleTerm result = getClosestAstNode(Tools.termAt(term, i));
                if(result != null)
                    return result;
            }
            return null;
        }
    }


    public static Collection<IMessage> ambiguityMessages(final FileObject resource, IStrategoTerm ast) {
        final Collection<IMessage> messages = Lists.newLinkedList();
        final TermVisitor termVisitor = new TermVisitor() {
            private IStrategoTerm ambStart;

            @Override public void preVisit(IStrategoTerm term) {
                if(ambStart == null && "amb".equals(Term.tryGetName(term))) {
                    final IToken left = getLeftToken(term);
                    final IToken right = getRightToken(term);
                    final ISourceRegion region = JSGLRSourceRegionFactory.fromTokens(left, right);
                    final String text = "Fragment is ambiguous: " + ambToString(term);

                    final IMessage message = MessageFactory.newAnalysisWarning(resource, region, text, null);
                    messages.add(message);
                    ambStart = term;
                }
            }

            @Override public void postVisit(IStrategoTerm term) {
                if(term == ambStart) {
                    ambStart = null;
                }
            }

            private String ambToString(IStrategoTerm amb) {
                final String result = amb.toString();
                return result.length() > 5000 ? result.substring(0, 5000) + "..." : result;
            }
        };
        termVisitor.visit(ast);
        return messages;
    }
}
