package org.metaborg.core.transform;

import java.util.Collection;

import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.messages.IMessage;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class TransformResults<V, T> {
    public final Iterable<TransformResult<V, T>> results;
    public final ITransformGoal goal;


    public TransformResults(Iterable<TransformResult<V, T>> results, ITransformGoal goal) {
        this.results = results;
        this.goal = goal;
    }


    public Iterable<IMessage> messages() {
        final Collection<IMessage> messages = Lists.newLinkedList();
        for(TransformResult<V, T> result : results) {
            Iterables.addAll(messages, result.messages);
        }
        return messages;
    }
}
