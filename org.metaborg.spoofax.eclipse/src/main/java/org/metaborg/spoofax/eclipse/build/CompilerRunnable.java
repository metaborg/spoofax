package org.metaborg.spoofax.eclipse.build;

import java.util.Collection;
import java.util.Map.Entry;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.MessageFactory;
import org.metaborg.spoofax.core.transform.CompileGoal;
import org.metaborg.spoofax.core.transform.ITransformer;
import org.metaborg.spoofax.core.transform.TransformerException;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class CompilerRunnable implements IWorkspaceRunnable {
    private final ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm> transformer;

    private final FileObject projectResource;
    private final Iterable<Entry<IContext, AnalysisResult<IStrategoTerm, IStrategoTerm>>> analysisResults;

    private final Collection<IMessage> extraMessages;


    CompilerRunnable(ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm> transformer, FileObject projectResource,
        Iterable<Entry<IContext, AnalysisResult<IStrategoTerm, IStrategoTerm>>> analysisResults,
        Collection<IMessage> extraMessages) {
        this.transformer = transformer;

        this.projectResource = projectResource;
        this.analysisResults = analysisResults;

        this.extraMessages = extraMessages;
    }


    @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
        final CompileGoal compileGoal = new CompileGoal();
        for(Entry<IContext, AnalysisResult<IStrategoTerm, IStrategoTerm>> entry : analysisResults) {
            final IContext context = entry.getKey();
            if(!transformer.available(compileGoal, context)) {
                SpoofaxProjectBuilder.logger.debug("No compilation required for {}", context.language());
                continue;
            }
            final AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult = entry.getValue();
            synchronized(context) {
                for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult : analysisResult.fileResults) {
                    if(fileResult.result == null) {
                        SpoofaxProjectBuilder.logger.warn("Input result for {} is null, cannot compile it",
                            fileResult.source);
                        continue;
                    }

                    try {
                        transformer.transform(fileResult, context, compileGoal);
                    } catch(TransformerException e) {
                        SpoofaxProjectBuilder.logger.error("Compilation failed", e);
                        extraMessages
                            .add(MessageFactory.newBuilderErrorAtTop(projectResource, "Compilation failed", e));
                    }
                }
                // GTODO: also compile any affected sources
            }
        }
    }
}