package org.metaborg.core.testing;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Writes Teamcity messages.
 */
public class TeamCityWriter {

    private final PrintStream output;
    @Nullable private final String flowID;

    /**
     * Creates a new instance of {@link TeamCityWriter}
     * that writes to standard out and has no flow ID.
     */
    public TeamCityWriter() {
        this(System.out, null);
    }

    /**
     * Creates a new instance of {@link TeamCityWriter}.
     *
     * @param output The stream to write to.
     * @param flowID he flow ID that indicates which messages belong together; or null.
     */
    public TeamCityWriter(PrintStream output, @Nullable String flowID) {
        if (output == null)
            throw new IllegalArgumentException("Argument output must not be null.");

        this.output = output;
        this.flowID = flowID;
    }

    /**
     * Sends a TeamCity message.
     *
     * @param name The message name.
     * @param attributes A list of pairs of attribute names and values.
     *      When a value is null, it is ignored. Values are escaped properly.
     */
    public void send(String name, Attribute... attributes) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
        List<Attribute> expandedAttributes = new ArrayList<>(Arrays.asList(attributes));
        expandedAttributes.add(new Attribute("timestamp", timestamp));
        expandedAttributes.add(new Attribute("flowId", flowID));

        String message = buildMessage(name, expandedAttributes);
        output.println(message);
    }

    /**
     * Builds a TeamCity message.
     * @param name The name of the message.
     * @param attributes The attributes of the message.
     * @return The built message.
     */
    private String buildMessage(String name, List<Attribute> attributes) {
        String attributeString = attributes.stream()
                .filter(attribute -> attribute.value != null)
                .map(attribute -> attribute.name + "='" + escape(attribute.value) + "'")
                .collect(Collectors.joining(" "));
        return "\n##teamcity[" + name + " " + attributeString + "]";
    }

    /**
     * Escapes the value for use in TeamCity messages.
     *
     * @param value The value to escape.
     * @return The escaped string.
     */
    @Nullable private String escape(@Nullable Object value) {
        if (value == null) return null;

        return value.toString()
                // The order matters! We must escape the pipe "|" first.
                .replace("|", "||")
                .replace("[", "|[")
                .replace("]", "|]")
                .replace("'", "|'")
                .replace("\r", "|r")
                .replace("\n", "|n");
    }

    /**
     * An attribute in a Teamcity message.
     */
    public static final class Attribute {
        public final String name;
        @Nullable public final Object value;

        public Attribute(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }
}
