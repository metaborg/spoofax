package org.metaborg.spoofax.core.config.language;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;
import org.metaborg.meta.nabl2.config.NaBL2Config;
import org.metaborg.meta.nabl2.config.NaBL2DebugConfig;
import org.metaborg.meta.nabl2.config.NaBL2DebugConfig.Flag;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public final class NaBL2ConfigReaderWriter {

    private static final String PROP_DEBUG = "debug";
    private static final String PROP_INCREMENTAL = "incremental";

    public static NaBL2Config read(HierarchicalConfiguration<ImmutableNode> config) {
        final boolean incremental = config.getBoolean(PROP_INCREMENTAL, false);
        final NaBL2DebugConfig debug = NaBL2DebugConfig.of(readFlags(config.getString(PROP_DEBUG, "")));
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

    private static Collection<String> splitString(String string) {
        return Arrays.asList(string.trim().split("\\s+")).stream().filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static Collection<Flag> readFlags(String flagNames) {
        List<Flag> flags = Lists.newArrayList();
        for(String name : splitString(flagNames)) {
            try {
                flags.add(Flag.valueOf(name.toUpperCase()));
            } catch(IllegalArgumentException ex) {
            }
        }
        return Sets.newEnumSet(flags, Flag.class);
    }

    public static Collection<IMessage> validate(ImmutableConfiguration config, MessageBuilder mb) {
        final String allFlags = String.join(" ", Arrays.asList(Flag.values()).stream().map(Flag::name)
                .map(String::toLowerCase).collect(Collectors.toList()));
        List<IMessage> messages = Lists.newArrayList();
        for(String flag : splitString(config.getString(PROP_DEBUG, ""))) {
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
