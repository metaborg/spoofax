package org.metaborg.spoofax.core.stratego;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.spoofax.core.dynamicclassloading.BuilderInput;
import org.metaborg.spoofax.core.dynamicclassloading.DynamicClassLoadingFacet;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

public interface IStrategoCommon {
    /**
     * Invokes a Stratego strategy in given component.
     *
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
    @Nullable IStrategoTerm invoke(ILanguageComponent component, IContext context, IStrategoTerm input, String strategy)
        throws MetaborgException;
    

    /**
     * Invokes a Stratego strategy in components of given language implementation. Returns the first result that
     * succeeds.
     *
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
    @Nullable IStrategoTerm invoke(ILanguageImpl impl, IContext context, IStrategoTerm input, String strategy)
        throws MetaborgException;
    
    /**
     * Invokes a Stratego strategy in components of given language implementation. Returns the first result that
     * succeeds.
     *
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
    @Nullable IStrategoTerm invoke(ILanguageImpl impl, IContext context, IStrategoTerm input, String strategy,
    		List<IStrategoTerm> termArguments) throws MetaborgException;

    /**
     * Invokes a Stratego strategy in components of given language implementation. Returns the first result that
     * succeeds.
     *
     * @param impl
     *            Language implementation to invoke the strategy in.
     * @param location
     *            Location to initialize the Stratego runtime with.
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
    @Nullable IStrategoTerm invoke(ILanguageImpl impl, FileObject location, IStrategoTerm input, String strategy)
        throws MetaborgException;
    
    /**
     * Invokes a Stratego strategy in components of given language implementation. Returns the first result that
     * succeeds.
     *
     * @param impl
     *            Language implementation to invoke the strategy in.
     * @param location
     *            Location to initialize the Stratego runtime with.
     * @param input
     *            Input term
     * @param strategy
     *            Name of the strategy to call.
     * @param termArguments
     *            Term arguments the strategy gets called with
     * @return Resulting term, or null if the strategy failed.
     * @throws MetaborgException
     *             When an error occurs getting a Stratego runtime.
     * @throws MetaborgException
     *             When invoking the strategy fails unexpectedly.
     */
    @Nullable IStrategoTerm invoke(ILanguageImpl impl, FileObject location, IStrategoTerm input, String strategy,
    		List<IStrategoTerm> termArguments) throws MetaborgException;

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
    @Nullable IStrategoTerm invoke(HybridInterpreter runtime, IStrategoTerm input, String strategy)
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
     * @param termArguments
     *            Term arguments the strategy gets called with
     * @return Resulting term, or null if the strategy failed.
     * @throws MetaborgException
     *             When invoking the strategy fails unexpectedly.
     */
    @Nullable IStrategoTerm invoke(HybridInterpreter runtime, IStrategoTerm input, String strategy, List<IStrategoTerm> termArguments) 
    		throws MetaborgException;

    /**
     * Converts a location into a Stratego string.
     * 
     * @param location
     *            Location to convert.
     * @return Stratego string with location.
     */
    IStrategoString locationTerm(FileObject location);

    /**
     * Converts a resource relative to a location into a Stratego string.
     * 
     * @param resource
     *            Resource to convert.
     * @param location
     *            Location to convert relative to.
     * @return Stratego string with resource.
     */
    IStrategoString resourceTerm(FileObject resource, FileObject location);

    /**
     * Creates an input term for a builder.
     * 
     * @param ast
     *            Term to use as the AST.
     * @param selectedTerm
     *            Optional selected term.
     * @param resource
     *            Location of the input resource.
     * @param location
     *            Location of the input context.
     * @return A 5-tuple input term (selected, position, ast, path, project-path).
     */
    BuilderInput builderInputTerm(IStrategoTerm ast, @Nullable IStrategoTerm selectedTerm,
                                  @Nullable FileObject resource, @Nullable FileObject location);

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
     */
    default BuilderInput builderInputTerm(IStrategoTerm ast, @Nullable FileObject resource,
        @Nullable FileObject location) {
        return builderInputTerm(ast, null, resource, location);
    }

    /**
     * Turns given term into a string. If the term is a string, return the string. Otherwise, return a pretty-printed
     * term.
     * 
     * @param term
     *            Term to convert to a string.
     * @return Result as a string.
     */
    String toString(IStrategoTerm term);

    /**
     * Pretty prints an ATerm.
     * 
     * @param term
     *            ATerm to pretty print.
     * @return Pretty printed ATerm as a Stratego string.
     */
    IStrategoString prettyPrint(IStrategoTerm term);

    /**
     * @param component
     *            Component to check
     * @return if the component has a facet that refers to Stratego code
     */
    static boolean hasStrategoFacets(ILanguageComponent component) {
        return component.facet(DynamicClassLoadingFacet.class) != null || component.facet(StrategoRuntimeFacet.class) != null;
    }
}