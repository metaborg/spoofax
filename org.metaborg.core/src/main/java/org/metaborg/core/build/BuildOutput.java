package org.metaborg.core.build;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.transform.ITransformUnit;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BuildOutput<P extends IParseUnit, A extends IAnalyzeUnit, T extends ITransformUnit<?>>
    implements IBuildOutput<P, A, T> {
    private boolean success = true;
    public final BuildState state;
    public final Set<FileName> removedResources = Sets.newHashSet();
    public final Set<FileName> includedResources = Sets.newHashSet();
    public final Collection<FileObject> changedResources = Lists.newLinkedList();
    public final Collection<P> parseResults = Lists.newLinkedList();
    public final Collection<A> analysisResults = Lists.newLinkedList();
    public final Collection<T> transformResults = Lists.newLinkedList();
    public final Collection<IMessage> extraMessages = Lists.newLinkedList();


    public BuildOutput(BuildState state) {
        this.state = state;
    }


    @Override public boolean success() {
        return success;
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

    @Override public Iterable<P> parseResults() {
        return parseResults;
    }

    @Override public Iterable<A> analysisResults() {
        return analysisResults;
    }

    @Override public Iterable<T> transformResults() {
        return transformResults;
    }

    @Override public Iterable<IMessage> extraMessages() {
        return extraMessages;
    }

    @Override public Iterable<IMessage> allMessages() {
        final Collection<IMessage> messages = Lists.newLinkedList();
        for(P result : parseResults) {
            Iterables.addAll(messages, result.messages());
        }
        for(A result : analysisResults) {
            Iterables.addAll(messages, result.messages());
        }
        for(T result : transformResults) {
            Iterables.addAll(messages, result.messages());
        }
        return messages;
    }


    public void add(boolean success, Iterable<FileName> removedResources, Iterable<FileName> includedResources,
        Iterable<FileObject> changedResources, Iterable<P> parseResults, Iterable<A> analysisResults,
        Iterable<T> transformResults, Iterable<IMessage> extraMessages) {
        this.success &= success;
        Iterables.addAll(this.removedResources, removedResources);
        Iterables.addAll(this.includedResources, includedResources);
        Iterables.addAll(this.changedResources, changedResources);
        Iterables.addAll(this.parseResults, parseResults);
        Iterables.addAll(this.analysisResults, analysisResults);
        Iterables.addAll(this.transformResults, transformResults);
        Iterables.addAll(this.extraMessages, extraMessages);
    }
}
