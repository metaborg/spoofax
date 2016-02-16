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
public final class LegacyLanguageIdentifierJacksonSerializer extends StdSerializer<LanguageIdentifier> {
    private static final long serialVersionUID = 2719180337465919831L;


    public LegacyLanguageIdentifierJacksonSerializer() {
        super(LanguageIdentifier.class);
    }


    @Override public void serialize(final LanguageIdentifier value, final JsonGenerator generator,
        final SerializerProvider provider) throws IOException {
        generator.writeString(value.toString());
    }
}
