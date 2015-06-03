package org.metaborg.spoofax.core.processing;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.transform.TransformResult;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BuildOutput<P, A, T> implements IBuildOutput<P, A, T> {
    public final Set<FileName> removedResources = Sets.newHashSet();
    public final Collection<FileObject> changedResources = Lists.newLinkedList();
    public final Collection<ParseResult<P>> parseResults = Lists.newLinkedList();
    public final Collection<AnalysisResult<P, A>> analysisResults = Lists.newLinkedList();
    public final Collection<TransformResult<AnalysisFileResult<P, A>, T>> transformResults = Lists.newLinkedList();
    public final Collection<IMessage> extraMessages = Lists.newLinkedList();


    @Override public Set<FileName> removedResources() {
        return removedResources;
    }

    @Override public Iterable<FileObject> changedResources() {
        return changedResources;
    }

    @Override public Iterable<ParseResult<P>> parseResults() {
        return parseResults;
    }

    @Override public Iterable<AnalysisResult<P, A>> analysisResults() {
        return analysisResults;
    }

    @Override public Iterable<TransformResult<AnalysisFileResult<P, A>, T>> transformResults() {
        return transformResults;
    }

    @Override public Iterable<IMessage> extraMessages() {
        return extraMessages;
    }

    
    public void add(Set<FileName> removedResources, Collection<FileObject> changedResources,
        Collection<ParseResult<P>> parseResults, Collection<AnalysisResult<P, A>> analysisResults,
        Collection<TransformResult<AnalysisFileResult<P, A>, T>> transformResults, Collection<IMessage> extraMessages) {
        this.removedResources.addAll(removedResources);
        this.changedResources.addAll(changedResources);
        this.parseResults.addAll(parseResults);
        this.analysisResults.addAll(analysisResults);
        this.transformResults.addAll(transformResults);
        this.extraMessages.addAll(extraMessages);
    }
}
