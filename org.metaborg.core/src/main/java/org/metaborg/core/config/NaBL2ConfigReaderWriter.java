package org.metaborg.core.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;
import org.metaborg.util.config.NaBL2Config;
import org.metaborg.util.config.NaBL2DebugConfig;
import org.metaborg.util.config.NaBL2DebugConfig.Flag;

import com.google.common.collect.Lists;

public final class NaBL2ConfigReaderWriter {

    private static final String PROP_DEBUG = "debug";
    private static final String PROP_INCREMENTAL = "incremental";

    public static NaBL2Config read(HierarchicalConfiguration<ImmutableNode> config) {
        final boolean incremental = config.getBoolean(PROP_INCREMENTAL, false);
        final List<String> flags = readFlags(config.getString(PROP_DEBUG, ""));
        final NaBL2DebugConfig debug = NaBL2DebugConfig.of(NaBL2DebugConfig.Flag.valuesOf(flags));
        return new NaBL2Config(incremental, debug);
    }

    public static void write(NaBL2Config nabl2Config, HierarchicalConfiguration<ImmutableNode> config) {
        if(nabl2Config.incremental()) {
            config.setProperty(PROP_INCREMENTAL, nabl2Config.incremental());
        }
        if(!nabl2Config.debug().flags().isEmpty()) {
            config.setProperty(PROP_DEBUG, nabl2Config.debug().flags());
        }
    }

    private static List<String> readFlags(String flags) {
        return Arrays.asList(flags.trim().split("\\s+")).stream().filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static Collection<IMessage> validate(ImmutableConfiguration config, MessageBuilder mb) {
        final String allFlags = String.join(" ", Arrays.asList(Flag.values()).stream().map(Flag::name)
                .map(String::toLowerCase).collect(Collectors.toList()));
        List<IMessage> messages = Lists.newArrayList();
        for(String flag : readFlags(config.getString(PROP_DEBUG, ""))) {
            try {
                Flag.valueOf(flag.toUpperCase());
            } catch(IllegalArgumentException ex) {

                messages.add(mb.withMessage("Invalid NaBL2 debug flag: " + flag + ", must be one of: " + allFlags + ".")
                        .build());
            }
        }
        return messages;
    }

}
