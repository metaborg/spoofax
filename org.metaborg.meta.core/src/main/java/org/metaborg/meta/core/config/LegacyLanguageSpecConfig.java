package org.metaborg.meta.core.config;

import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.project.config.LegacyLanguageComponentConfig;
import org.metaborg.core.project.settings.ILegacyProjectSettings;

@Deprecated
@SuppressWarnings("deprecation")
public class LegacyLanguageSpecConfig extends LegacyLanguageComponentConfig implements ILanguageSpecConfig {
    private static final long serialVersionUID = 4321718437339177753L;


    public LegacyLanguageSpecConfig(ILegacyProjectSettings settings) {
        super(settings);
    }


    @Override public String metaborgVersion() {
        return MetaborgConstants.METABORG_VERSION;
    }
}
