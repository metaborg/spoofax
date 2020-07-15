package org.metaborg.spoofax.core.syntax;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ILanguageImplConfig;
import org.metaborg.core.config.JSGLRVersion;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * A {@link ILanguageImpl} class that delegates to the given one. It has two extra fields with overrides that are used
 * in the {@link JSGLRParseService}. This class is only really used as a key that's distinguishing the langImpl from a
 * version with overrides. The class also has a {@link LanguageImplementationWithParserOverride#matcher(ILanguageImpl)}
 * method to construct a matcher object based on a language implementation, which matches any other object of this class
 * with the same wrapped language implementation.
 */
final class LanguageImplementationWithParserOverride implements ILanguageImpl {
    private ILanguageImpl langImpl;
    private @Nullable ImploderImplementation overrideImploder;
    private @Nullable JSGLRVersion overrideJSGLRVersion;
    private boolean matcher;

    LanguageImplementationWithParserOverride(ILanguageImpl langImpl, @Nullable ImploderImplementation overrideImploder,
        @Nullable JSGLRVersion overrideJSGLRVersion) {
        this.langImpl = langImpl;
        this.overrideImploder = overrideImploder;
        this.overrideJSGLRVersion = overrideJSGLRVersion;
        this.matcher = false;
    }

    private LanguageImplementationWithParserOverride(ILanguageImpl langImpl) {
        this.langImpl = langImpl;
        this.overrideImploder = null;
        this.overrideJSGLRVersion = null;
        this.matcher = true;
    }

    /**
     * A matcher object of this class is equal to any other object of this class with the same langImpl. Useful for
     * evicting entries from a map based when you only have the langImpl.
     */
    static LanguageImplementationWithParserOverride matcher(ILanguageImpl langImpl) {
        return new LanguageImplementationWithParserOverride(langImpl);
    }

    @Override public boolean hasFacet(Class<? extends IFacet> type) {
        return langImpl.hasFacet(type);
    }

    @Override public LanguageIdentifier id() {
        return langImpl.id();
    }

    @Override public int sequenceId() {
        return langImpl.sequenceId();
    }

    @Override public <T extends IFacet> Iterable<T> facets(Class<T> type) {
        return langImpl.facets(type);
    }

    @Override public Iterable<FileObject> locations() {
        return langImpl.locations();
    }

    @Override public Iterable<ILanguageComponent> components() {
        return langImpl.components();
    }

    @Override public <T extends IFacet> Iterable<FacetContribution<T>> facetContributions(Class<T> type) {
        return langImpl.facetContributions(type);
    }

    @Override public ILanguage belongsTo() {
        return langImpl.belongsTo();
    }

    @Override public ILanguageImplConfig config() {
        return langImpl.config();
    }

    @Override public <T extends IFacet> T facet(Class<T> type) {
        return langImpl.facet(type);
    }

    @Override public <T extends IFacet> FacetContribution<T> facetContribution(Class<T> type) {
        return langImpl.facetContribution(type);
    }

    @Override public Iterable<IFacet> facets() {
        return langImpl.facets();
    }

    @Override public Iterable<FacetContribution<IFacet>> facetContributions() {
        return langImpl.facetContributions();
    }

    @Override public int hashCode() {
        // N.B. this intentionally collides the hash with any other override object with the same langImpl. This is
        //      necessary for the matcher object to work in hashsets/maps, as the set/map will use the equals method for
        //      objects with a collision. We don't expect many language implementations with different overrides to
        //      exist in the JSGLRParseService cache at the same time.
        return 31 * langImpl.hashCode();
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        LanguageImplementationWithParserOverride other = (LanguageImplementationWithParserOverride) obj;
        if(langImpl == null) {
            if(other.langImpl != null)
                return false;
        } else if(!langImpl.equals(other.langImpl))
            return false;
        // N.B. if one of the objects is a matcher, it matches with any override.
        if(this.matcher || other.matcher) {
            return true;
        }
        if(overrideImploder != other.overrideImploder)
            return false;
        if(overrideJSGLRVersion != other.overrideJSGLRVersion)
            return false;
        return true;
    }

    @Override public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("LangImplKey for the ParseService cache. based on ");
        stringBuilder.append(langImpl.toString());
        if(overrideImploder != null) {
            stringBuilder.append("; overridden imploder: ").append(overrideImploder.toString());
        }
        if(overrideJSGLRVersion != null) {
            stringBuilder.append("; overridden JSGLR version: ").append(overrideJSGLRVersion.toString());
        }
        return stringBuilder.toString();
    }
}
