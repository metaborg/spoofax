package org.metaborg.spoofax.core.style;

import java.util.LinkedList;
import java.util.List;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.style.ICategory;
import org.metaborg.core.style.IRegionCategory;
import org.metaborg.core.style.RegionCategory;
import org.metaborg.spoofax.core.syntax.JSGLRSourceRegionFactory;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokens;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.ParentAttachment;
import org.spoofax.terms.util.TermUtils;

public class CategorizerService implements ISpoofaxCategorizerService {
    private static final ILogger logger = LoggerUtils.logger(CategorizerService.class);


    @Override public Iterable<IRegionCategory<IStrategoTerm>> categorize(ILanguageImpl language,
        ISpoofaxParseUnit parseResult) {
        final List<IRegionCategory<IStrategoTerm>> regionCategories = new LinkedList<>();
        if(!parseResult.valid()) {
            return regionCategories;
        }

        final StylerFacet facet = language.facet(StylerFacet.class);
        if(facet == null) {
            logger.error("Cannot categorize input of {}, it does not have a styler facet", language);
            // GTODO: throw exception instead
            return regionCategories;
        }

        final ImploderAttachment rootImploderAttachment = ImploderAttachment.get(parseResult.ast());
        if(rootImploderAttachment == null) {
            logger.error("Cannot categorize input {} of {}, it does not have an imploder attachment", parseResult,
                language);
            // GTODO: throw exception instead
            return regionCategories;
        }
        final ITokens tokenizer = rootImploderAttachment.getLeftToken().getTokenizer();
        if(tokenizer == null) {
            logger.error("Cannot categorize input {} of {}, it does not have a tokenizer", parseResult, language);
            // GTODO: throw exception instead
            return regionCategories;
        }
        for(IToken token : tokenizer) {
            final ICategory category = category(facet, token);
            if(category != null) {
                final ISourceRegion region = JSGLRSourceRegionFactory.fromToken(token);
                final IStrategoTerm term = (IStrategoTerm) token.getAstNode();
                regionCategories.add(new RegionCategory<>(region, category, term));
            }
        }

        return regionCategories;
    }

    @Override public Iterable<IRegionCategory<IStrategoTerm>> categorize(ILanguageImpl language,
        ISpoofaxAnalyzeUnit analysisResult) {
        throw new UnsupportedOperationException();
    }


    private ICategory category(StylerFacet facet, IToken token) {
        IStrategoTerm term = (IStrategoTerm) token.getAstNode();
        if(term == null) {
            return tokenCategory(token);
        }

        if(!TermUtils.isAppl(term) && !TermUtils.isTuple(term) && !TermUtils.isList(term)) {
            // Try to use the parent of terminal nodes, mimicking behavior of old Spoofax/IMP runtime.
            final IStrategoTerm parentTerm = ParentAttachment.getParent(term);
            if(parentTerm != null) {
                final ICategory category = sortConsCategory(facet, parentTerm);
                if(category != null) {
                    return category;
                }
            }
        }

        final ICategory category = sortConsCategory(facet, term);
        if(category != null) {
            return category;
        }

        return tokenCategory(token);
    }

    private ICategory sortConsCategory(StylerFacet facet, IStrategoTerm term) {
        final ImploderAttachment imploderAttachment = ImploderAttachment.get(term);
        final String sort = imploderAttachment.getSort();
        if(sort == null) {
            return null;
        }
        // LEGACY: for some reason, when using concrete syntax extensions, all sorts are appended with _sort.
        final String massagedSort = sort.replace("_sort", "");
        if(TermUtils.isAppl(term)) {
            final String cons = ((IStrategoAppl) term).getConstructor().getName();
            if(facet.hasSortConsStyle(massagedSort, cons)) {
                return new SortConsCategory(massagedSort, cons);
            } else if(facet.hasConsStyle(cons)) {
                return new ConsCategory(cons);
            } else if(facet.hasSortStyle(massagedSort)) {
                return new SortCategory(massagedSort);
            }
            return null;
        }

        if(facet.hasSortStyle(massagedSort)) {
            return new SortCategory(massagedSort);
        }

        return null;
    }

    private ICategory tokenCategory(IToken token) {
        switch(token.getKind()) {
            case TK_IDENTIFIER:
                return new TokenCategory("TK_IDENTIFIER");
            case TK_NUMBER:
                return new TokenCategory("TK_NUMBER");
            case TK_STRING:
                return new TokenCategory("TK_STRING");
            case TK_ERROR_KEYWORD:
            case TK_KEYWORD:
                return new TokenCategory("TK_KEYWORD");
            case TK_OPERATOR:
                return new TokenCategory("TK_OPERATOR");
            case TK_VAR:
                return new TokenCategory("TK_VAR");
            case TK_ERROR_LAYOUT:
            case TK_LAYOUT:
                return new TokenCategory("TK_LAYOUT");
            default:
                logger.debug("Unhandled token kind " + token.getKind());
            case TK_UNKNOWN:
            case TK_ERROR:
            case TK_EOF:
            case TK_ERROR_EOF_UNEXPECTED:
            case TK_ESCAPE_OPERATOR:
            case TK_RESERVED:
            case TK_NO_TOKEN_KIND:
                return null;
        }
    }
}
