package org.metaborg.core.context;

import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class ContextUtils {
    private static final Logger logger = LoggerFactory.getLogger(ContextUtils.class);

    /**
     * Gets a set of all contexts for given resources.
     * 
     * @param resources
     *            Resources to get contexts for.
     * @param languageIdentifier
     *            Language identifier service.
     * @param contextService
     *            Context service.
     * @return Set of all contexts.
     */
    public static Set<IContext> getAll(Iterable<FileObject> resources, ILanguageIdentifierService languageIdentifier,
        IContextService contextService) {
        final Set<IContext> contexts = Sets.newHashSet();
        for(FileObject resource : resources) {
            final ILanguage language = languageIdentifier.identify(resource);
            if(language == null) {
                logger.error("Could not identify language for {}", resource);
                continue;
            }
            try {
                contexts.add(contextService.get(resource, language));
            } catch(ContextException e) {
                final String message = String.format("Could not retrieve context for %s", resource);
                logger.error(message, e);
            }
        }
        return contexts;
    }
}
