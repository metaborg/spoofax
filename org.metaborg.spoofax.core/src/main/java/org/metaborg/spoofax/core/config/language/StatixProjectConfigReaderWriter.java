package org.metaborg.spoofax.core.config.language;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import mb.statix.spoofax.IStatixProjectConfig;
import mb.statix.spoofax.StatixProjectConfig;

public class StatixProjectConfigReaderWriter {

    private static final String PROP_CONCURRENT = "concurrent";
    private static final String PROP_MESSAGE_TRACE_LENGTH = "message-trace-length";
    private static final String PROP_MESSAGE_TERM_DEPTH = "message-term-depth";

    public static IStatixProjectConfig read(HierarchicalConfiguration<ImmutableNode> config) {
        Collection<String> parallelLanguages = config.getList(String.class, PROP_CONCURRENT, null);
        Integer messageStacktraceLength = config.getInteger(PROP_MESSAGE_TRACE_LENGTH, null);
        Integer messageTermDepth = config.getInteger(PROP_MESSAGE_TERM_DEPTH, null);
        return new StatixProjectConfig(parallelLanguages, messageStacktraceLength, messageTermDepth);
    }

    public static void write(IStatixProjectConfig statixConfig, HierarchicalConfiguration<ImmutableNode> config) {
        final Set<String> parallelLanguages = statixConfig.parallelLanguages(null);
        if(parallelLanguages != null) {
            config.setProperty(PROP_CONCURRENT, parallelLanguages);
        }
        final Integer messageStacktraceLength = statixConfig.messageTraceLength(null);
        if(messageStacktraceLength != null) {
            config.setProperty(PROP_MESSAGE_TRACE_LENGTH, messageStacktraceLength);
        }
        final Integer messageTermDepth = statixConfig.messageTermDepth(null);
        if(messageTermDepth != null) {
            config.setProperty(PROP_MESSAGE_TERM_DEPTH, messageTermDepth);
        }
    }


}