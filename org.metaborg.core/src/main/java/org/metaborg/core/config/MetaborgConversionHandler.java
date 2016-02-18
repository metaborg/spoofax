package org.metaborg.core.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.convert.DefaultConversionHandler;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * Converts Metaborg Core types to and from their {@link Configuration} representations.
 */
class MetaborgConversionHandler extends DefaultConversionHandler {
    @SuppressWarnings("unchecked") @Override protected <T> T convertValue(Object src, Class<T> target,
        ConfigurationInterpolator interp) {
        if(target == LanguageIdentifier.class) {
            return (T) convertToLanguageIdentifier(src);
        } else {
            return super.convertValue(src, target, interp);
        }
    }

    @Override protected Object extractConversionValue(Object src, Class<?> target, ConfigurationInterpolator interp) {
        return super.extractConversionValue(src, target, interp);
    }

    @Override protected boolean isComplexObject(Object src) {
        return super.isComplexObject(src);
    }


    private LanguageIdentifier convertToLanguageIdentifier(Object src) {
        if(src == null) {
            return null;
        }

        return LanguageIdentifier.parse(src.toString());
    }
}
