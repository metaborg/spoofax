package org.metaborg.spoofax.core.style;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.messages.ISourceRegion;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.visitor.AStrategoTermVisitor;
import org.spoofax.terms.visitor.StrategoTermVisitee;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class SourceCategorizerService implements ISourceCategorizerService<IStrategoTerm, IStrategoTerm> {
    private static final Logger logger = LogManager.getLogger(SourceCategorizerService.class);

    private final ISyntaxService<IStrategoTerm> syntaxService;


    @Inject public SourceCategorizerService(ISyntaxService<IStrategoTerm> syntaxService) {
        this.syntaxService = syntaxService;
    }


    @Override public Iterable<IRegionCategory<IStrategoTerm>> categorize(ILanguage language,
        ParseResult<IStrategoTerm> parseResult) {
        final StylerFacet facet = language.facet(StylerFacet.class);
        final List<IRegionCategory<IStrategoTerm>> regionCategories = Lists.newLinkedList();

        StrategoTermVisitee.accept(new AStrategoTermVisitor() {
            @Override public boolean visit(IStrategoAppl term) {
                final ImploderAttachment imploderAttachment = ImploderAttachment.get(term);
                if(imploderAttachment == null) {
                    logger.warn("Cannot retrieve origin location information for term " + term.toString());
                    return true;
                }
                final String sort = imploderAttachment.getSort();
                final String cons = term.getConstructor().getName();
                final ISourceRegion region = syntaxService.region(term);
                
                final ICategory category;
                if(facet.hasSortConsStyle(sort, cons)) {
                    category = new SortConsCategory(sort, cons);
                } else if(facet.hasConsStyle(cons)) {
                    category = new ConsCategory(cons);
                } else if(facet.hasSortStyle(sort)) {
                    category = new SortCategory(sort);
                } else {
                    category = null;
                }
                
                if(category != null) {
                    regionCategories.add(new RegionCategory<IStrategoTerm>(term, region, category));
                    return false;
                }
                
                return true;
            }

            @Override public boolean visit(IStrategoTerm term) {
                final ImploderAttachment imploderAttachment = ImploderAttachment.get(term);
                if(imploderAttachment == null) {
                    logger.warn("Cannot retrieve origin information for term " + term.toString());
                    return true;
                }
                final String sort = imploderAttachment.getSort();
                final ISourceRegion region = syntaxService.region(term);
                
                final ICategory category;
                if(facet.hasSortStyle(sort)) {
                    category = new SortCategory(sort);
                } else {
                    final IToken token = imploderAttachment.getLeftToken();
                    switch(token.getKind()) {
                        case IToken.TK_IDENTIFIER:
                            category = new TokenCategory("TK_IDENTIFIER");
                            break;
                        case IToken.TK_NUMBER:
                            category = new TokenCategory("TK_NUMBER");
                            break;
                        case IToken.TK_STRING:
                            category = new TokenCategory("TK_STRING");
                            break;
                        case IToken.TK_KEYWORD:
                            category = new TokenCategory("TK_KEYWORD");
                            break;
                        case IToken.TK_OPERATOR:
                            category = new TokenCategory("TK_OPERATOR");
                            break;
                        case IToken.TK_VAR:
                            category = new TokenCategory("TK_VAR");
                            break;
                        case IToken.TK_LAYOUT:
                            category = new TokenCategory("TK_LAYOUT");
                            break;
                        case IToken.TK_ERROR:
                            category = new TokenCategory("TK_ERROR");
                            break;
                        case IToken.TK_UNKNOWN:
                            category = new TokenCategory("TK_UNKNOWN");
                            break;
                        default:
                            category = null;
                    }
                }
                
                if(category != null) {
                    regionCategories.add(new RegionCategory<IStrategoTerm>(term, region, category));
                    return false;
                }
                
                return true;
            }
        }, parseResult.result);

        return regionCategories;
    }

    @Override public Iterable<IRegionCategory<IStrategoTerm>> categorize(ILanguage language,
        AnalysisFileResult<IStrategoTerm, IStrategoTerm> analysisResult) {
        throw new UnsupportedOperationException();
    }
}
