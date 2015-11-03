package org.metaborg.core.project.settings;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.metaborg.core.language.LanguageIdentifier;

import java.io.IOException;

/**
 * Serializer for the {@link LanguageIdentifier} class.
 */
public final class LanguageIdentifierJacksonSerializer extends StdSerializer<LanguageIdentifier> {

    public LanguageIdentifierJacksonSerializer() {
        super(LanguageIdentifier.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(
            final LanguageIdentifier value,
            final JsonGenerator generator,
            final SerializerProvider provider) throws IOException {
        generator.writeString(value.toString());
    }
}
