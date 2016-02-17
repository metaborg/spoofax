package org.metaborg.core.project.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;

import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;

/**
 * Reads, writes, and creates configurations Apache Commons {@link HierarchicalConfiguration} objects.
 */
public abstract class AConfigurationReaderWriter {
    private static final Charset DefaultCharset = StandardCharsets.UTF_8;


    /**
     * Reads a configuration from the specified file.
     *
     * @param source
     *            The source file.
     * @return The read configuration.
     */
    public HierarchicalConfiguration<ImmutableNode> read(FileObject source, @Nullable FileObject rootFolder)
        throws IOException, ConfigurationException {
        try(InputStream input = source.getContent().getInputStream()) {
            return read(input, rootFolder);
        }
    }

    /**
     * Reads a configuration from the specified input stream.
     *
     * @param input
     *            The input stream.
     * @param rootFolder
     *            The root folder; or <code>null</code>.
     * @return The read configuration.
     */
    public HierarchicalConfiguration<ImmutableNode> read(InputStream input, @Nullable FileObject rootFolder)
        throws IOException, ConfigurationException {
        try(Reader reader = new BufferedReader(new InputStreamReader(input, DefaultCharset))) {
            return read(reader, rootFolder);
        }
    }

    /**
     * Reads a configuration from the specified reader.
     *
     * @param reader
     *            The reader.
     * @param rootFolder
     *            The root folder; or <code>null</code>.
     * @return The read configuration.
     */
    public HierarchicalConfiguration<ImmutableNode> read(Reader reader, @Nullable FileObject rootFolder)
        throws IOException, ConfigurationException {
        JacksonConfiguration resultConfig = createConfiguration(null, rootFolder);
        resultConfig.read(reader);
        return resultConfig;
    }

    /**
     * Writes a configuration to the specified writer.
     *
     * @param configuration
     *            The configuration to write.
     * @param destination
     *            The destination file.
     * @param rootFolder
     *            The root folder.
     */
    public void write(HierarchicalConfiguration<ImmutableNode> configuration, FileObject destination,
        @Nullable FileObject rootFolder) throws IOException, ConfigurationException {
        try(OutputStream output = destination.getContent().getOutputStream()) {
            write(configuration, output, rootFolder);
        }
    }

    /**
     * Writes a configuration to the specified writer.
     *
     * @param configuration
     *            The configuration to write.
     * @param output
     *            The output stream.
     * @param rootFolder
     *            The root folder; or <code>null</code>.
     */
    public void write(HierarchicalConfiguration<ImmutableNode> configuration, OutputStream output,
        @Nullable FileObject rootFolder) throws IOException, ConfigurationException {
        try(Writer writer = new OutputStreamWriter(output, DefaultCharset)) {
            write(configuration, writer, rootFolder);
        }
    }

    /**
     * Writes a configuration to the specified writer.
     *
     * @param configuration
     *            The configuration to write.
     * @param writer
     *            The writer.
     * @param rootFolder
     *            The root folder; or <code>null</code>.
     */
    public void write(HierarchicalConfiguration<ImmutableNode> configuration, Writer writer,
        @Nullable FileObject rootFolder) throws IOException, ConfigurationException {
        JacksonConfiguration resultConfig = createConfiguration(configuration, rootFolder);
        resultConfig.write(writer);
    }

    /**
     * Creates a configuration object.
     *
     * @param sourceConfiguration
     *            The source configuration; or <code>null</code>.
     * @param rootFolder
     *            The root folder; or <code>null</code>.
     * @return The created configuration object.
     */
    public JacksonConfiguration createConfiguration(
        @Nullable HierarchicalConfiguration<ImmutableNode> sourceConfiguration, @Nullable FileObject rootFolder) {
        JacksonConfiguration config = createNewConfiguration(sourceConfiguration);
        if(rootFolder != null) {
            config.getInterpolator().registerLookup("path", new PathLookup(rootFolder));
        }
        return config;
    }


    /**
     * Creates a configuration object.
     *
     * @param sourceConfiguration
     *            The source configuration; or <code>null</code>.
     * @return The created configuration object.
     */
    protected abstract JacksonConfiguration createNewConfiguration(
        @Nullable HierarchicalConfiguration<ImmutableNode> sourceConfiguration);
}
