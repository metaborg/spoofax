package org.metaborg.core.project.configuration;

import org.apache.commons.configuration2.convert.DefaultConversionHandler;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * Converts some Metaborg Core types to and from their {@link org.apache.commons.configuration2.Configuration}
 * representations.
 */
/* package private */ class MetaborgConversionHandler extends DefaultConversionHandler {

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T convertValue(
            final Object src, final Class<T> targetCls, final ConfigurationInterpolator ci) {

        if (targetCls == LanguageIdentifier.class) {
            return (T) convertToLanguageIdentifier(src, ci);
        } else {
            return super.convertValue(src, targetCls, ci);
        }
    }

    private LanguageIdentifier convertToLanguageIdentifier(final Object src, final ConfigurationInterpolator ci) {
        if (src == null)
            return null;

        return LanguageIdentifier.parse(src.toString());
    }
}
