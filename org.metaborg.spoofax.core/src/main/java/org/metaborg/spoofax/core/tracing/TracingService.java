package org.metaborg.spoofax.core.tracing;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceLocation;
import org.metaborg.spoofax.core.syntax.JSGLRSourceRegionFactory;
import org.metaborg.spoofax.core.syntax.SourceAttachment;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.util.TermUtils;
import org.spoofax.terms.visitor.AStrategoTermVisitor;
import org.spoofax.terms.visitor.IStrategoTermVisitor;
import org.spoofax.terms.visitor.StrategoTermVisitee;

import mb.jsglr.shared.IToken;
import mb.jsglr.shared.ImploderAttachment;


public class TracingService implements ISpoofaxTracingService {
    private final IResourceService resourceService;


    @jakarta.inject.Inject public TracingService(IResourceService resourceService) {
        this.resourceService = resourceService;
    }


    @Override public ISourceLocation location(IStrategoTerm fragment) {
        final IStrategoTerm origin = origin(fragment);
        if(origin == null) {
            return tokenLocation(fragment);
        }
        return tokenLocation(origin);
    }

    private ISourceLocation tokenLocation(IStrategoTerm fragment) {
        final IToken left = ImploderAttachment.getLeftToken(fragment);
        final IToken right = ImploderAttachment.getRightToken(fragment);
        if(left == null || right == null) {
            return null;
        }
        final ISourceRegion region = JSGLRSourceRegionFactory.fromTokens(left, right);
        final FileObject resource = SourceAttachment.getResource(fragment, resourceService);
        return new SourceLocation(region, resource);
    }


    @Override public IStrategoTerm origin(IStrategoTerm fragment) {
        return OriginAttachment.getOrigin(fragment);
    }


    @Override public Iterable<IStrategoTerm> fragments(ISpoofaxParseUnit result, ISourceRegion region) {
        if(!result.valid()) {
            throw new MetaborgRuntimeException(
                "Cannot get fragments for parse unit " + result + " because it is invalid");
        }
        return toTerms(result.ast(), region);
    }

    @Override public Iterable<IStrategoTerm> fragments(ISpoofaxAnalyzeUnit result, ISourceRegion region) {
        if(!result.valid()) {
            throw new MetaborgRuntimeException(
                "Cannot get fragments for analyze unit " + result + " because it is invalid");
        }
        if(!result.hasAst()) {
            return Iterables2.empty();
        }
        return toTerms(result.ast(), region);
    }

    @Override public Iterable<IStrategoTerm> fragments(ISpoofaxTransformUnit<?> result, ISourceRegion region) {
        if(!result.valid()) {
            throw new MetaborgRuntimeException(
                "Cannot get fragments for transform unit " + result + " because it is invalid");
        }
        return toTerms(result.ast(), region);
    }

    @Override public Iterable<IStrategoTerm> toTerms(IStrategoTerm ast, final ISourceRegion region) {
        if(ast == null || region == null) {
            return Iterables2.empty();
        }
        final Collection<IStrategoTerm> parsed = new LinkedList<>();
        final IStrategoTermVisitor visitor = new AStrategoTermVisitor() {
            @Override public boolean visit(IStrategoTerm term) {
                final ISourceLocation location = location(term);
                if(location != null && location.region().contains(region)) {
                    parsed.add(term);
                    return false;
                }
                return true;
            }
        };
        StrategoTermVisitee.bottomup(visitor, ast);
        return parsed;
    }


    @Override public Iterable<IStrategoTerm> fragmentsWithin(ISpoofaxParseUnit result, ISourceRegion region) {
        if(!result.valid()) {
            throw new MetaborgRuntimeException(
                "Cannot get fragments for parse unit " + result + " because it is invalid");
        }
        return toTermsWithin(result.ast(), region);
    }


    @Override public Iterable<IStrategoTerm> fragmentsWithin(ISpoofaxAnalyzeUnit result, ISourceRegion region) {
        if(!result.valid()) {
            throw new MetaborgRuntimeException(
                "Cannot get fragments for analyze unit " + result + " because it is invalid");
        }
        return toTermsWithin(result.ast(), region);
    }


    @Override public Iterable<IStrategoTerm> fragmentsWithin(ISpoofaxTransformUnit<?> result, ISourceRegion region) {
        if(!result.valid()) {
            throw new MetaborgRuntimeException(
                "Cannot get fragments for transform unit " + result + " because it is invalid");
        }
        return toTermsWithin(result.ast(), region);
    }

    @Override public Iterable<IStrategoTerm> toTermsWithin(IStrategoTerm ast, final ISourceRegion region) {
        if(ast == null || region == null) {
            return Iterables2.empty();
        }
        final Collection<IStrategoTerm> parsed = new LinkedList<>();
        final IStrategoTermVisitor visitor = new AStrategoTermVisitor() {
            @Override public boolean visit(IStrategoTerm term) {
                if(TermUtils.isList(term, 1)) {
                    // try element instead of singleton list
                    return true;
                }
                final ISourceLocation location = location(term);
                if(location != null && region.contains(location.region())) {
                    parsed.add(term);
                    // no need to check the children, as this term is already within the given region
                    return false;
                }
                return true;
            }
        };
        StrategoTermVisitee.topdown(visitor, ast);
        return parsed;
    }
}
