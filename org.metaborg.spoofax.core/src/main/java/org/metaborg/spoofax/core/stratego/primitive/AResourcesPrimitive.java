package org.metaborg.spoofax.core.stratego.primitive;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.dependency.MissingDependencyException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.metaborg.util.collection.Cache;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.tuple.Tuple2;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.io.binary.TermReader;
import org.spoofax.terms.util.TermUtils;

public abstract class AResourcesPrimitive extends ASpoofaxContextPrimitive implements AutoCloseable {

    private static final ILogger log = LoggerUtils.logger(AResourcesPrimitive.class);

    private final IResourceService resourceService;
    private final Cache<FileObject, Tuple2<Long, IStrategoTerm>> fileCache;

    public AResourcesPrimitive(String name, IResourceService resourceService) {
        super(name, 2, 0);
        this.resourceService = resourceService;
        this.fileCache = new Cache<>(32);
    }

    @Override public void close() {
        fileCache.clear();
    }

    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
            ITermFactory factory, IContext context) throws MetaborgException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
            ITermFactory factory, org.spoofax.interpreter.core.IContext strategoContext)
            throws MetaborgException, IOException {
        final IContext context = metaborgContext(strategoContext);
        if(context == null) {
            throw new MetaborgException("Cannot execute primitive " + name + ", no Spoofax context was set");
        }

        final ITermFactory TF = strategoContext.getFactory();
        final Strategy nameToPathStr = svars[0];
        final Strategy importStr = svars[1];
        final TermReader termReader = new TermReader(TF);

        final List<FileObject> locations = locations(context);

        final Deque<IStrategoTerm> names = new ArrayDeque<>(parseNames(current));
        final Map<IStrategoTerm, IStrategoTerm> resources = new HashMap<>();
        while(!names.isEmpty()) {
            final IStrategoTerm name = names.pop();
            if(!resources.containsKey(name)) {
                final String path = resourcePath(strategoContext, nameToPathStr, name);
                final IStrategoTerm resource;
                if((resource = loadResource(locations, path, termReader).orElse(null)) == null) {
                    return null;
                }
                resources.put(name, resource);
                names.addAll(resourceImports(strategoContext, importStr, resource));
            }
        }

        return TF.makeList(resources.entrySet().stream().map(e -> TF.makeTuple(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));
    }

    protected abstract List<FileObject> locations(IContext context) throws MissingDependencyException;

    private Optional<IStrategoTerm> loadResource(List<FileObject> locations, String path, TermReader termReader) {
        for(FileObject location : locations) {
            final FileObject file;
            try {
                file = resourceService.resolve(location, path);
            } catch(MetaborgRuntimeException e) {
                continue;
            }
            final IStrategoTerm term;
            try(FileContent content = file.getContent()) {
                final Tuple2<Long, IStrategoTerm> cacheEntry = fileCache.get(file);
                if(cacheEntry != null && !(cacheEntry._1() < content.getLastModifiedTime())) {
                    term = cacheEntry._2();
                } else {
                    term = termReader.parseFromStream(content.getInputStream());
                    fileCache.put(file, Tuple2.of(content.getLastModifiedTime(), term));
                }
            } catch(IOException e) {
                fileCache.remove(file);
                continue;
            }
            return Optional.of(term);
        }
        log.error("Could not find {}", path);
        return Optional.empty();
    }

    private String resourcePath(org.spoofax.interpreter.core.IContext strategoContext, Strategy s, IStrategoTerm name)
            throws MetaborgException {
        strategoContext.setCurrent(name);
        try {
            if(!s.evaluate(strategoContext)) {
                throw new MetaborgException("Strategy failed to get path for name " + name);
            }
        } catch(InterpreterException e) {
            throw new MetaborgException(e);
        }
        IStrategoTerm current = strategoContext.current();
        if(!TermUtils.isString(current)) {
            throw new MetaborgException("Expected path string, got " + current);
        }
        return TermUtils.toJavaString(current);

    }

    private List<IStrategoTerm> resourceImports(org.spoofax.interpreter.core.IContext strategoContext, Strategy s,
            IStrategoTerm resource) throws MetaborgException {
        strategoContext.setCurrent(resource);
        try {
            if(!s.evaluate(strategoContext)) {
                return Collections.emptyList();
            }
        } catch(InterpreterException e) {
            throw new MetaborgException(e);
        }
        return parseNames(strategoContext.current());
    }

    private List<IStrategoTerm> parseNames(IStrategoTerm current) throws MetaborgException {
        if(!TermUtils.isList(current)) {
            throw new MetaborgException("Expected list of names, got " + current);
        }
        return Arrays.asList(current.getAllSubterms());
    }

}