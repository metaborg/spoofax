package org.metaborg.spoofax.core.config.language;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import mb.statix.spoofax.IStatixProjectConfig;
import mb.statix.spoofax.SolverMode;
import mb.statix.spoofax.StatixProjectConfig;

public class StatixProjectConfigReaderWriter {

    private static final ILogger logger = LoggerUtils.logger(StatixProjectConfigReaderWriter.class);

    private static final String PROP_CONCURRENT = "concurrent";
    private static final String PROP_MODES = "modes";
    private static final String PROP_MESSAGE_TRACE_LENGTH = "message-trace-length";
    private static final String PROP_MESSAGE_TERM_DEPTH = "message-term-depth";

    public static IStatixProjectConfig read(HierarchicalConfiguration<ImmutableNode> config) {
        Collection<String> parallelLanguages = config.getList(String.class, PROP_CONCURRENT, Collections.emptyList());
        Map<String, SolverMode> modes = new HashMap<>();
        if(!parallelLanguages.isEmpty()) {
            logger.warn("Option runtime.statix.concurrent is deprecated. Use runtime.statix.modes.<lang>.<mode> instead.");
        }
        parallelLanguages.forEach(languageName -> modes.put(languageName, SolverMode.CONCURRENT));
        config.getKeys(PROP_MODES).forEachRemaining(modeKey -> {
            String languageName = modeKey.substring(PROP_MODES.length() + 1);
            SolverMode mode = SolverMode.valueOf(config.getString(modeKey).toUpperCase());
            modes.put(languageName, mode);
        });
        Integer messageStacktraceLength = config.getInteger(PROP_MESSAGE_TRACE_LENGTH, null);
        Integer messageTermDepth = config.getInteger(PROP_MESSAGE_TERM_DEPTH, null);
        return new StatixProjectConfig(modes, messageStacktraceLength, messageTermDepth);
    }

    public static void write(IStatixProjectConfig statixConfig, HierarchicalConfiguration<ImmutableNode> config) {
        final Map<String, SolverMode> languageModes = statixConfig.languageModes(null);
        if(languageModes != null) {
            for(String languageName : languageModes.keySet()) {
                config.setProperty(String.format("%s.%s", PROP_MODES, languageName), languageModes.get(languageName));
            }
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