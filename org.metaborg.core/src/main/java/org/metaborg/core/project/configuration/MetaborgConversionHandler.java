package org.metaborg.core.project.configuration;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.convert.DefaultConversionHandler;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * Converts Metaborg Core types to and from their {@link Configuration} representations.
 */
class MetaborgConversionHandler extends DefaultConversionHandler {
    @SuppressWarnings("unchecked") @Override protected <T> T convertValue(final Object src, final Class<T> targetCls,
        final ConfigurationInterpolator ci) {

        if(targetCls == LanguageIdentifier.class) {
            return (T) convertToLanguageIdentifier(src);
        } else {
            return super.convertValue(src, targetCls, ci);
        }
    }

    private LanguageIdentifier convertToLanguageIdentifier(Object src) {
        if(src == null) {
            return null;
        }

        return LanguageIdentifier.parse(src.toString());
    }
}
