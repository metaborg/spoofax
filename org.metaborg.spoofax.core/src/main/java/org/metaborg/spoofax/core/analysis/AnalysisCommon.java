package org.metaborg.spoofax.core.analysis;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.spoofax.interpreter.core.StackTracer;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermVisitor;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import javax.inject.Inject;

public class AnalysisCommon {
    private final ISpoofaxTracingService tracingService;


    @Inject public AnalysisCommon(ISpoofaxTracingService tracingService) {
        this.tracingService = tracingService;
    }


    public String analysisFailedMessage(HybridInterpreter interpreter) {
        final StackTracer stackTracer = interpreter.getContext().getStackTracer();
        return "Analysis failed\nStratego stack trace:\n" + stackTracer.getTraceString();
    }

    public Collection<IMessage> messages(@Nullable FileObject resource, MessageSeverity severity, IStrategoTerm messagesTerm) {
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

            if(originTerm != null) {
                final ISourceLocation location = tracingService.location(originTerm);
                if(location != null) {
                    final ISourceRegion region = location.region();
                    messages.add(message(resource, region, message, severity));
                } else {
                    messages.add(message(resource, message, severity));
                }
            } else {
                messages.add(message(resource, message, severity));
            }
        }

        return messages;
    }

    public Collection<IMessage> messages(MessageSeverity severity, IStrategoTerm messagesTerm) {
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

            if(originTerm != null) {
                final ISourceLocation location = tracingService.location(originTerm);
                if(location != null) {
                    final ISourceRegion region = location.region();
                    messages.add(message(location.resource(), region, message, severity));
                } else {
                    messages.add(message(null, message, severity));
                }
            } else {
                messages.add(message(null, message, severity));
            }
        }

        return messages;
    }

    public Collection<IMessage> ambiguityMessages(final FileObject resource, IStrategoTerm ast) {
        final Collection<IMessage> messages = Lists.newLinkedList();
        final TermVisitor termVisitor = new TermVisitor() {
            private IStrategoTerm ambStart;

            @Override public void preVisit(IStrategoTerm term) {
                if(ambStart == null && "amb".equals(TermUtils.asAppl(term).map(a -> a.getConstructor().getName()).orElse(null))) {
                    final String text = "Fragment is ambiguous: " + ambToString(term);
                    final ISourceLocation location = tracingService.location(term);
                    if(location != null) {
                        final ISourceRegion region = location.region();
                        messages.add(message(resource, region, text, MessageSeverity.WARNING));
                    } else {
                        messages.add(message(resource, text, MessageSeverity.WARNING));
                    }

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


    private String toString(IStrategoTerm term) {
        if(TermUtils.isString(term)) {
            final IStrategoString messageStringTerm = (IStrategoString) term;
            return messageStringTerm.stringValue();
        } else if(TermUtils.isList(term)) {
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

    private IMessage message(@Nullable FileObject resource, ISourceRegion region, String message, MessageSeverity severity) {
        return MessageFactory.newAnalysisMessage(resource, region, message, severity, null);
    }

    private IMessage message(@Nullable FileObject resource, String message, MessageSeverity severity) {
        return MessageFactory.newAnalysisMessageAtTop(resource, message + " (no origin information)", severity, null);
    }
}
