package org.metaborg.spoofax.core.stratego;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.transform.TransformResult;
import org.metaborg.core.transform.TransformException;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

public interface IStrategoCommon {
    /**
     * Invokes a Stratego strategy in given component.
     * 
     * @param service
     *            Stratego runtime service to get a runtime from.
     * @param component
     *            Component to invoke the strategy in.
     * @param context
     *            Context to initialize the runtime with.
     * @param input
     *            Input term
     * @param strategy
     *            Name of the strategy to call.
     * @return Resulting term, or null if the strategy failed.
     * @throws MetaborgException
     *             When an error occurs getting a Stratego runtime.
     * @throws MetaborgException
     *             When invoking the strategy fails unexpectedly.
     */
    public abstract IStrategoTerm invoke(ILanguageComponent component, IContext context, IStrategoTerm input,
        String strategy) throws MetaborgException;

    /**
     * Invokes a Stratego strategy in components of given language implementation. Returns the first result that
     * succeeds.
     * 
     * @param service
     *            Stratego runtime service to get a runtime from.
     * @param impl
     *            Language implementation to invoke the strategy in.
     * @param context
     *            Context to initialize the runtime with.
     * @param input
     *            Input term
     * @param strategy
     *            Name of the strategy to call.
     * @return Resulting term, or null if the strategy failed.
     * @throws MetaborgException
     *             When an error occurs getting a Stratego runtime.
     * @throws MetaborgException
     *             When invoking the strategy fails unexpectedly.
     */
    public abstract IStrategoTerm invoke(ILanguageImpl impl, IContext context, IStrategoTerm input, String strategy)
        throws MetaborgException;

    /**
     * Invokes a Strategy strategy in given runtime.
     * 
     * @param runtime
     *            Stratego runtime to invoke the strategy in.
     * @param input
     *            Input term
     * @param strategy
     *            Name of the strategy to call.
     * @return Resulting term, or null if the strategy failed.
     * @throws MetaborgException
     *             When invoking the strategy fails unexpectedly.
     */
    public abstract IStrategoTerm invoke(HybridInterpreter runtime, IStrategoTerm input, String strategy)
        throws MetaborgException;

    /**
     * Executes given strategy and creates a transformation result.
     * 
     * @param component
     *            Component to initialize Stratego code in.
     * @param context
     *            Context to initialize Stratego runtime with.
     * @param prevResult
     *            Originating result
     * @param strategy
     *            Strategy to execute
     * @param input
     *            Term to execute the strategy with.
     * @param resource
     *            Origin resource of the input term.
     * @return Transformation result
     * @throws TransformException
     *             When Stratego invocation fails.
     */
    public abstract <PrevT> TransformResult<PrevT, IStrategoTerm> transform(ILanguageComponent component,
        IContext context, PrevT prevResult, String strategy, IStrategoTerm input, FileObject resource)
        throws MetaborgException;

    /**
     * Converts a location into a Stratego string.
     * 
     * @param localLocation
     *            Location to convert.
     * @return Stratego string with location.
     */
    public abstract IStrategoString localLocationTerm(File localLocation);

    /**
     * Converts a resource relative to a location into a Stratego string.
     * 
     * @param localResource
     *            Resource to convert.
     * @param localLocation
     *            Location to convert relative to.
     * @return Stratego string with resource.
     */
    public abstract IStrategoString localResourceTerm(File localResource, File localLocation);

    /**
     * Creates an input term for a builder.
     * 
     * @param ast
     *            Term to use as the AST.
     * @param resource
     *            Location of the input resource.
     * @param location
     *            Location of the input context.
     * @return A 5-tuple input term (selected, position, ast, path, project-path).
     * @throws MetaborgException
     *             When {@code resource} or {@code location} do not reside on the local file system.
     */
    public abstract IStrategoTerm builderInputTerm(IStrategoTerm ast, FileObject resource, FileObject location)
        throws MetaborgException;

    /**
     * Attempts to write given builder result to a file.
     * 
     * @param result
     *            Builder result to write, should be a 2-tuple (term, filename).
     * @param location
     *            Directory to write the file to.
     * @return Written file, or null if writing a file failed.
     */
    public abstract FileObject builderWriteResult(IStrategoTerm result, FileObject location);

    /**
     * Turns the result of a builder into a string. If the result is a string, return the string. If the result is a
     * term, return a pretty-printed term. If the result has no subterms, return an empty string.
     * 
     * @param result
     *            Result to convert to a string.
     * @return Result as a string.
     */
    public abstract String builderResultToString(IStrategoTerm result);

    /**
     * Pretty prints an ATerm.
     * 
     * @param term
     *            ATerm to pretty print.
     * @return Pretty printed ATerm as a Stratego string.
     */
    public abstract IStrategoString prettyPrint(IStrategoTerm term);
}
