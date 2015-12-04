package org.metaborg.core.project.settings;

import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Reads/writes configurations in YAML files.
 */
public class YamlConfigurationReaderWriter {

    private static final Charset DefaultCharset = StandardCharsets.UTF_8;

    /**
     * Reads a configuration from the specified file.
     *
     * @param source The source file.
     * @return The read configuration.
     */
    public HierarchicalConfiguration<ImmutableNode> read(FileObject source) throws IOException, ConfigurationException {
        try (InputStream input = source.getContent().getInputStream()) {
            return read(input);
        }
    }

    /**
     * Reads a configuration from the specified input stream.
     *
     * @param input The input stream.
     * @return The read configuration.
     */
    public HierarchicalConfiguration<ImmutableNode> read(InputStream input) throws IOException, ConfigurationException {
        try (Reader reader = new BufferedReader(new InputStreamReader(input, DefaultCharset))) {
            return read(reader);
        }
    }

    /**
     * Reads a configuration from the specified reader.
     *
     * @param reader The reader.
     * @return The read configuration.
     */
    public HierarchicalConfiguration<ImmutableNode> read(Reader reader) throws IOException, ConfigurationException {
        JacksonConfiguration resultConfig = createConfiguration(null);
        resultConfig.read(reader);
        return resultConfig;
    }

    /**
     * Writes a configuration to the specified writer.
     *
     * @param configuration The configuration to write.
     * @param destination The destination file.
     */
    public void write(HierarchicalConfiguration<ImmutableNode> configuration, FileObject destination) throws IOException,
            ConfigurationException {
        try (OutputStream output = destination.getContent().getOutputStream()) {
            write(configuration, output);
        }
    }

    /**
     * Writes a configuration to the specified writer.
     *
     * @param configuration The configuration to write.
     * @param output The output stream.
     */
    public void write(HierarchicalConfiguration<ImmutableNode> configuration, OutputStream output) throws IOException,
            ConfigurationException {
        try (Writer writer = new OutputStreamWriter(output, DefaultCharset)) {
            write(configuration, writer);
        }
    }

    /**
     * Writes a configuration to the specified writer.
     *
     * @param configuration The configuration to write.
     * @param writer The writer.
     */
    public void write(HierarchicalConfiguration<ImmutableNode> configuration, Writer writer) throws IOException,
            ConfigurationException {
        JacksonConfiguration resultConfig = createConfiguration(configuration);
        resultConfig.write(writer);
    }

    /**
     * Creates a configuration object.
     *
     * @param sourceConfiguration The source configuration; or <code>null</code>.
     * @return The created configuration object.
     */
    private JacksonConfiguration createConfiguration(@Nullable HierarchicalConfiguration<ImmutableNode> sourceConfiguration) {
        JacksonConfiguration config = new YamlConfiguration(sourceConfiguration);
        config.setConversionHandler(new MetaborgConversionHandler());
        return config;
    }

}
