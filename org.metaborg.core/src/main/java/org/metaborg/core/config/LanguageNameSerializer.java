package org.metaborg.core.config;

import java.io.IOException;

import org.metaborg.core.language.LanguageName;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class LanguageNameSerializer extends StdSerializer<LanguageName> {
    private static final long serialVersionUID = 2719180337465919831L;


    public LanguageNameSerializer() {
        super(LanguageName.class);
    }

    @Override public void serialize(final LanguageName value, final JsonGenerator generator,
        final SerializerProvider provider) throws IOException {
        generator.writeString(value.toString());
    }

}