package org.metaborg.core.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.analysis.IAnalyzeUnitUpdate;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.transform.ITransformUnit;
import org.metaborg.util.iterators.Iterables2;

public class BuildOutput<P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate, T extends ITransformUnit<?>>
    implements IBuildOutputInternal<P, A, AU, T> {
    private boolean success = true;
    public BuildState state;
    public final Set<FileName> removedResources = new HashSet<FileName>();
    public final Set<FileName> includedResources = new HashSet<FileName>();
    public final Collection<FileObject> changedResources = new ArrayList<>();
    public final Collection<P> parseResults = new ArrayList<>();
    public final Collection<A> analysisResults = new ArrayList<>();
    public final Collection<AU> analysisUpdates = new ArrayList<>();
    public final Collection<T> transformResults = new ArrayList<>();
    public final Collection<IMessage> extraMessages = new LinkedList<>();


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

    @Override public Collection<A> analysisResults() {
        return Collections.unmodifiableCollection(analysisResults);
    }

    @Override public Iterable<AU> analysisUpdates() {
        return analysisUpdates;
    }

    @Override public Iterable<T> transformResults() {
        return transformResults;
    }

    @Override public Iterable<IMessage> extraMessages() {
        return extraMessages;
    }

    @Override public Iterable<IMessage> allMessages() {
        final Collection<IMessage> messages = new LinkedList<>();
        for(P result : parseResults) {
            Iterables2.addAll(messages, result.messages());
        }
        for(A result : analysisResults) {
            Iterables2.addAll(messages, result.messages());
        }
        for(T result : transformResults) {
            Iterables2.addAll(messages, result.messages());
        }
        return messages;
    }


    @Override public void setState(BuildState state) {
        this.state = state;
    }

    @Override public void add(boolean success, Iterable<FileName> removedResources,
        Iterable<FileName> includedResources, Iterable<FileObject> changedResources, Iterable<P> parseResults,
        Iterable<A> analysisResults, Iterable<AU> analysisUpdates, Iterable<T> transformResults,
        Iterable<IMessage> extraMessages) {
        this.success &= success;
        Iterables2.addAll(this.removedResources, removedResources);
        Iterables2.addAll(this.includedResources, includedResources);
        Iterables2.addAll(this.changedResources, changedResources);
        Iterables2.addAll(this.parseResults, parseResults);
        Iterables2.addAll(this.analysisResults, analysisResults);
        Iterables2.addAll(this.analysisUpdates, analysisUpdates);
        Iterables2.addAll(this.transformResults, transformResults);
        Iterables2.addAll(this.extraMessages, extraMessages);
    }
}
