package org.metaborg.core.build;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.analysis.AnalysisResult;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.core.transform.TransformResult;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BuildOutput<P, A, T> implements IBuildOutput<P, A, T> {
    public final BuildState state;
    public final Set<FileName> removedResources = Sets.newHashSet();
    public final Set<FileName> includedResources = Sets.newHashSet();
    public final Collection<FileObject> changedResources = Lists.newLinkedList();
    public final Collection<ParseResult<P>> parseResults = Lists.newLinkedList();
    public final Collection<AnalysisResult<P, A>> analysisResults = Lists.newLinkedList();
    public final Collection<TransformResult<AnalysisFileResult<P, A>, T>> transformResults = Lists.newLinkedList();
    public final Collection<IMessage> extraMessages = Lists.newLinkedList();


    public BuildOutput(BuildState state) {
        this.state = state;
    }


    @Override public BuildState state() {
        return state;
    }

    @Override public Set<FileName> removedResources() {
        return removedResources;
    }

    @Override public Set<FileName> includedResources() {
        return includedResources;
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


    public void add(Iterable<FileName> removedResources, Iterable<FileName> includedResources,
        Iterable<FileObject> changedResources, Iterable<ParseResult<P>> parseResults,
        Iterable<AnalysisResult<P, A>> analysisResults,
        Iterable<TransformResult<AnalysisFileResult<P, A>, T>> transformResults, Iterable<IMessage> extraMessages) {
        Iterables.addAll(this.removedResources, removedResources);
        Iterables.addAll(this.includedResources, includedResources);
        Iterables.addAll(this.changedResources, changedResources);
        Iterables.addAll(this.parseResults, parseResults);
        Iterables.addAll(this.analysisResults, analysisResults);
        Iterables.addAll(this.transformResults, transformResults);
        Iterables.addAll(this.extraMessages, extraMessages);
    }
}
