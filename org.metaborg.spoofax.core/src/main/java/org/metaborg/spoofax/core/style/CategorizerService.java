package org.metaborg.spoofax.core.style;

import java.util.List;

import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.messages.ISourceRegion;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.syntax.jsglr.JSGLRSourceRegionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class CategorizerService implements ICategorizerService<IStrategoTerm, IStrategoTerm> {
    private static final Logger logger = LoggerFactory.getLogger(CategorizerService.class);


    @Inject public CategorizerService() {

    }


    @Override public Iterable<IRegionCategory<IStrategoTerm>> categorize(ILanguage language,
        ParseResult<IStrategoTerm> parseResult) {
        final StylerFacet facet = language.facet(StylerFacet.class);
        final List<IRegionCategory<IStrategoTerm>> regionCategories = Lists.newLinkedList();

        final ImploderAttachment rootImploderAttachment = ImploderAttachment.get(parseResult.result);
        final ITokenizer tokenzier = rootImploderAttachment.getLeftToken().getTokenizer();
        final int tokenCount = tokenzier.getTokenCount();
        for(int i = 0; i < tokenCount; ++i) {
            final IToken token = tokenzier.getTokenAt(i);
            final ISimpleTerm term = token.getAstNode();

            final ICategory category;
            if(term == null) {
                category = tokenCategory(token);
            } else {
                final IStrategoTerm strategoTerm = (IStrategoTerm) term;
                final ImploderAttachment imploderAttachment = ImploderAttachment.get(strategoTerm);
                final String sort = imploderAttachment.getSort();
                if(strategoTerm.getTermType() == IStrategoTerm.APPL) {
                    final String cons = ((IStrategoAppl) strategoTerm).getConstructor().getName();

                    if(facet.hasSortConsStyle(sort, cons)) {
                        category = new SortConsCategory(sort, cons);
                    } else if(facet.hasConsStyle(cons)) {
                        category = new ConsCategory(cons);
                    } else if(facet.hasSortStyle(sort)) {
                        category = new SortCategory(sort);
                    } else {
                        category = tokenCategory(token);
                    }
                } else {
                    if(facet.hasSortStyle(sort)) {
                        category = new SortCategory(sort);
                    } else {
                        category = tokenCategory(token);
                    }
                }
            }

            if(category != null) {
                final ISourceRegion region = JSGLRSourceRegionFactory.fromToken(token);
                regionCategories.add(new RegionCategory<IStrategoTerm>(null, region, category));
            }
        }

        return regionCategories;
    }

    @Override public Iterable<IRegionCategory<IStrategoTerm>> categorize(ILanguage language,
        AnalysisFileResult<IStrategoTerm, IStrategoTerm> analysisResult) {
        throw new UnsupportedOperationException();
    }


    private ICategory tokenCategory(IToken token) {
        switch(token.getKind()) {
            case IToken.TK_IDENTIFIER:
                return new TokenCategory("TK_IDENTIFIER");
            case IToken.TK_NUMBER:
                return new TokenCategory("TK_NUMBER");
            case IToken.TK_STRING:
                return new TokenCategory("TK_STRING");
            case IToken.TK_ERROR_KEYWORD:
            case IToken.TK_KEYWORD:
                return new TokenCategory("TK_KEYWORD");
            case IToken.TK_OPERATOR:
                return new TokenCategory("TK_OPERATOR");
            case IToken.TK_VAR:
                return new TokenCategory("TK_VAR");
            case IToken.TK_ERROR_LAYOUT:
            case IToken.TK_LAYOUT:
                return new TokenCategory("TK_LAYOUT");
            case IToken.TK_ERROR:
                return new TokenCategory("TK_ERROR");
            case IToken.TK_UNKNOWN:
                return new TokenCategory("TK_UNKNOWN");
            default:
                logger.debug("Unhandled token kind " + token.getKind());
            case IToken.TK_EOF:
            case IToken.TK_ERROR_EOF_UNEXPECTED:
            case IToken.TK_ESCAPE_OPERATOR:
            case IToken.TK_RESERVED:
            case IToken.TK_NO_TOKEN_KIND:
                return null;
        }
    }
}
