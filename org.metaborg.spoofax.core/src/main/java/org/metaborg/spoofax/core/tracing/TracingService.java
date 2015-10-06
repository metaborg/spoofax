package org.metaborg.spoofax.core.tracing;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceLocation;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.core.transform.TransformResult;
import org.metaborg.spoofax.core.syntax.JSGLRSourceRegionFactory;
import org.metaborg.spoofax.core.syntax.SourceAttachment;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.visitor.AStrategoTermVisitor;
import org.spoofax.terms.visitor.IStrategoTermVisitor;
import org.spoofax.terms.visitor.StrategoTermVisitee;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class TracingService implements ISpoofaxTracingService {
    private final IResourceService resourceService;


    @Inject public TracingService(IResourceService resourceService) {
        this.resourceService = resourceService;
    }


    @Override public @Nullable ISourceLocation fromParsed(IStrategoTerm fragment) {
        final IToken left = ImploderAttachment.getLeftToken(fragment);
        final IToken right = ImploderAttachment.getRightToken(fragment);
        if(left == null || right == null) {
            return null;
        }
        final ISourceRegion region = JSGLRSourceRegionFactory.fromTokens(left, right);
        final FileObject resource = SourceAttachment.getResource(fragment, resourceService);
        return new SourceLocation(region, resource);
    }

    @Override public @Nullable ISourceLocation fromAnalyzed(IStrategoTerm fragment) {
        return fromOrigin(fragment);
    }

    @Override public @Nullable ISourceLocation fromTransformed(IStrategoTerm fragment) {
        return fromOrigin(fragment);
    }

    private @Nullable ISourceLocation fromOrigin(IStrategoTerm fragment) {
        final IStrategoTerm origin = origin(fragment);
        if(origin == null) {
            return fromParsed(fragment);
        }
        return fromParsed(origin);
    }


    @Override public @Nullable IStrategoTerm originFromAnalyzed(IStrategoTerm fragment) {
        return origin(fragment);
    }

    @Override public @Nullable IStrategoTerm originFromTransformed(IStrategoTerm fragment) {
        return origin(fragment);
    }

    private @Nullable IStrategoTerm origin(IStrategoTerm fragment) {
        return OriginAttachment.getOrigin(fragment);
    }


    @Override public Iterable<IStrategoTerm> toParsed(ParseResult<IStrategoTerm> result, final ISourceRegion region) {
        return toTerms(result.result, region, false);
    }

    @Override public Iterable<IStrategoTerm> toAnalyzed(AnalysisFileResult<IStrategoTerm, IStrategoTerm> result,
        ISourceRegion region) {
        return toTerms(result.result, region, true);
    }

    @Override public Iterable<IStrategoTerm> toTransformed(TransformResult<?, IStrategoTerm> result,
        ISourceRegion region) {
        return toTerms(result.result, region, true);
    }

    private Iterable<IStrategoTerm> toTerms(IStrategoTerm ast, final ISourceRegion region, final boolean origin) {
        if(ast == null || region == null) {
            return Iterables2.empty();
        }
        final Collection<IStrategoTerm> parsed = Lists.newLinkedList();
        final IStrategoTermVisitor visitor = new AStrategoTermVisitor() {
            @Override public boolean visit(IStrategoTerm term) {
                final ISourceLocation location;
                if(origin) {
                    location = fromOrigin(term);
                } else {
                    location = fromParsed(term);
                }
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
}
