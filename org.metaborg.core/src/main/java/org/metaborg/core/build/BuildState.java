package org.metaborg.core.build;

import java.util.HashMap;
import java.util.Map;

import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.resource.IResourceService;

public class BuildState {
    private final Map<ILanguageImpl, LanguageBuildState> languageBuildStates = new HashMap<>();


    public LanguageBuildState get(IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ILanguageImpl language) {
        LanguageBuildState state = languageBuildStates.get(language);
        if(state == null) {
            state = new LanguageBuildState(resourceService, languageIdentifierService, language);
            languageBuildStates.put(language, state);
        }
        return state;
    }

    public void add(ILanguageImpl language, LanguageBuildState state) {
        this.languageBuildStates.put(language, state);
    }
}
