package org.metaborg.core.build;

import java.util.Map;

import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.resource.IResourceService;

import com.google.common.collect.Maps;

public class BuildState {
    private final Map<ILanguage, LanguageBuildState> languageBuildStates = Maps.newHashMap();


    public LanguageBuildState get(IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ILanguage language) {
        LanguageBuildState state = languageBuildStates.get(language);
        if(state == null) {
            state = new LanguageBuildState(resourceService, languageIdentifierService, language);
            languageBuildStates.put(language, state);
        }
        return state;
    }

    public void add(ILanguage language, LanguageBuildState state) {
        this.languageBuildStates.put(language, state);
    }
}
