package org.metaborg.core.project.settings;

import java.io.IOException;

import org.metaborg.core.language.LanguageIdentifier;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer for the {@link LanguageIdentifier} class.
 */
@Deprecated
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
