package org.metaborg.core.language;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

public class LanguageNameUtils {
    private static ILogger logger = LoggerUtils.logger(LanguageNameUtils.class);

    static final Pattern idPattern = Pattern.compile("[A-Za-z0-9._\\-]+");
    public static final String errorDescription = "may only consist of alphanumeral and _ - . characters";

    public static boolean validId(String id) {
        final Matcher matcher = idPattern.matcher(id);
        if(!matcher.matches()) {
            return false;
        }
        return true;
    }

    /**
     * Tries to find a language implementation, first trying to parse name as a groupId:id, then by using it as a
     * language name.
     * 
     * @return A language implementation, or null if none was found.
     */
    public static ILanguageImpl tryGetLanguageLegacy(String languageOrName, ILanguageService languageService) {
        ILanguageImpl impl = null;
        try {
            LanguageName language = LanguageName.parse(languageOrName);
            impl = languageService.getImpl(language);
        } catch(RuntimeException e) {
        }
        if(impl == null) {
            ILanguage language = languageService.getLanguage(languageOrName);
            if(language != null) {
                impl = language.activeImpl();
                final String replacement = impl != null ? impl.id().name().toString() : "a language identifier";
                logger.warn("Language name was used where language identifier was expected. Use {} instead of '{}'",
                        replacement, languageOrName);
            }
        }
        return impl;
    }

}