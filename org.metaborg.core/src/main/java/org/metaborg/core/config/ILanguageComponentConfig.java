package org.metaborg.core.config;

import java.util.Collection;

import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * Configuration of a language component at runtime.
 * 
 * To create a new instance of this interface, use an {@link ILanguageComponentConfigBuilder} interface.
 */
public interface ILanguageComponentConfig extends IProjectConfig {
    /**
     * Gets the language identifier.
     *
     * @return The language identifier.
     */
    LanguageIdentifier identifier();

    /**
     * Gets the name of the language the component belongs to.
     *
     * @return Name of the language the component belongs to.
     */
    String name();

    /**
     * Gets the language contributions.
     *
     * @return The language contributions.
     */
    Collection<LanguageContributionIdentifier> langContribs();
    
    /**
     * Gets the languages for while files are generated.
     *
     * @return The languages for while files are generated.
     */
    Collection<IGenerateConfig> generates();
    
    /**
     * Gets whether the project depends on SDF or not.
     *
     * @return true if SDF is enabled and false otherwise.
     */
    Boolean sdfEnabled();   
    
    /**
     * Gets the (relative) path to the parse table.
     *
     * @return path to the parse table.
     */
    String parseTable();    
    
    /**
     * Gets the (relative) path to the completions parse table.
     *
     * @return path to the completions parse table.
     */
    String completionsParseTable();
    
    /**
     * Gets the file exports.
     *
     * @return The file exports.
     */
    Collection<IExportConfig> exports();
}
