package org.metaborg.spoofax.meta.core.signature;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.meta.core.config.ILanguageSpecConfig;
import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.meta.core.signature.AnySort;
import org.metaborg.meta.core.signature.ConstructorSig;
import org.metaborg.meta.core.signature.ISig;
import org.metaborg.meta.core.signature.ISigExtractor;
import org.metaborg.meta.core.signature.ISort;
import org.metaborg.meta.core.signature.ISortArg;
import org.metaborg.meta.core.signature.InjectionSig;
import org.metaborg.meta.core.signature.ListSort;
import org.metaborg.meta.core.signature.OptionalSort;
import org.metaborg.meta.core.signature.PrimitiveSort;
import org.metaborg.meta.core.signature.PrimitiveSortType;
import org.metaborg.meta.core.signature.Sort;
import org.metaborg.meta.core.signature.SortArg;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnitService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.meta.core.build.LangSpecCommonPaths;
import org.metaborg.util.file.IFileAccess;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.FileSelectorUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class StrategoSigExtractor implements ISigExtractor {
    private static final String strategoLangName = "Stratego-Sugar";
    private static final String lexicalSortName = "String";
    private static final String anySortName = "T_Any";

    private static final ILogger logger = LoggerUtils.logger(StrategoSigExtractor.class);

    private final ILanguageIdentifierService languageIdentifierService;
    private final ISpoofaxInputUnitService inputService;
    private final ISourceTextService sourceTextService;
    private final ISpoofaxSyntaxService syntaxService;
    private final ILanguagePathService languagePathService;


    @Inject public StrategoSigExtractor(ILanguageIdentifierService languageIdentifierService,
        ISpoofaxInputUnitService inputService, ISourceTextService sourceTextService,
        ISpoofaxSyntaxService syntaxService, ILanguagePathService languagePathService) {
        this.languageIdentifierService = languageIdentifierService;
        this.inputService = inputService;
        this.sourceTextService = sourceTextService;
        this.syntaxService = syntaxService;
        this.languagePathService = languagePathService;
    }


    @Override public Collection<ISig> extract(ILanguageSpec languageSpec, @Nullable IFileAccess access)
        throws IOException, ParseException {
        final FileObject root = languageSpec.location();
        final ILanguageSpecConfig config = languageSpec.config();
        final LangSpecCommonPaths paths = new LangSpecCommonPaths(root);
        final FileObject mainFile = paths.strMainFile(config.name());
        if(access != null) {
            access.read(mainFile);
        }
        final IdentifiedResource identifiedMainFile =
            languageIdentifierService.identifyToResource(mainFile, languageSpec);
        if(identifiedMainFile == null) {
            logger.error("Stratego main file {} is not a Stratego file, or the Stratego meta-language is not loaded",
                mainFile);
            return Lists.newArrayList();
        }

        final Iterable<FileObject> includePaths =
            languagePathService.sourceAndIncludePaths(languageSpec, strategoLangName);

        return extractStartingFrom(identifiedMainFile, includePaths, access);
    }

    private Collection<ISig> extractStartingFrom(IdentifiedResource mainFile, Iterable<FileObject> includePaths,
        @Nullable IFileAccess access) throws IOException, ParseException {
        final Set<String> seenImports = Sets.newHashSet();
        final Set<FileObject> seenFiles = Sets.newHashSet();
        final LinkedList<String> importsTodo = Lists.newLinkedList();
        final Collection<ISig> allSigs = Lists.newArrayList();

        final IStrategoTerm mainTerm = parseStratego(mainFile);
        if(mainTerm == null) {
            return allSigs;
        }
        final Collection<ISig> mainFileSignatures = extractModule(mainTerm, importsTodo);
        allSigs.addAll(mainFileSignatures);

        while(!importsTodo.isEmpty()) {
            final String nextImport = importsTodo.pop();
            if(!seenImports.add(nextImport)) {
                continue;
            }
            if(nextImport.startsWith("libstratego")) {
                // Skip standard libraries
                continue;
            }

            final Collection<FileObject> files = findStrFiles(nextImport, includePaths);
            if(files.isEmpty()) {
                logger.error("Could not extract signatures for unresolvable import {}", nextImport);
            }
            for(FileObject file : files) {
                if(seenFiles.add(file)) {
                    if(access != null) {
                        access.read(file);
                    }
                    final IdentifiedResource identified = languageIdentifierService.identifyToResource(file);
                    if(identified == null) {
                        logger.error(
                            "Imported Stratego file {} is not a Stratego file, or the Stratego meta-language is not loaded",
                            mainFile);
                        continue;
                    }
                    final IStrategoTerm term = parseStratego(identified);
                    if(term == null) {
                        continue;
                    }
                    final Collection<ISig> sigs = extractModule(term, importsTodo);
                    allSigs.addAll(sigs);
                }
            }
        }

        return allSigs;
    }

    private @Nullable IStrategoTerm parseStratego(IdentifiedResource file) throws IOException, ParseException {
        final FileObject resource = file.resource;
        final String text = sourceTextService.text(resource);
        final ISpoofaxInputUnit input = inputService.inputUnit(resource, text, file.language, file.dialect);
        final ISpoofaxParseUnit result = syntaxService.parse(input);
        if(result.valid() && result.success()) {
            return result.ast();
        } else {
            logger.error("Could not parse Stratego file {}; cannot extract its signatures", file);
        }
        return null;
    }

    private Collection<FileObject> findStrFiles(String imprt, Iterable<FileObject> strjIncludeDirs)
        throws FileSystemException {
        if(imprt.endsWith("/-")) {
            final String path = imprt.substring(0, imprt.length() - 2);
            for(FileObject includeDir : strjIncludeDirs) {
                if(!includeDir.exists()) {
                    continue;
                }
                final FileObject searchDir = includeDir.resolveFile(path);
                final FileObject[] files = searchDir.findFiles(FileSelectorUtils.extension("str"));
                if(files != null) {
                    return Lists.newArrayList(files);
                }
            }
        } else {
            for(FileObject includeDir : strjIncludeDirs) {
                if(!includeDir.exists()) {
                    continue;
                }
                final FileObject strategoFile = includeDir.resolveFile(imprt + ".str");
                if(strategoFile.exists()) {
                    return Lists.newArrayList(strategoFile);
                }
            }
        }

        return Lists.newArrayList();
    }

    private Collection<ISig> extractModule(IStrategoTerm module, List<String> outImports) {
        final List<ISig> allSigs = Lists.newArrayList();
        if(!((IStrategoAppl) module).getConstructor().getName().equals("Module")) {
            logger.error("Unknown Stratego module {}; skipping", module);
            return allSigs;
        }

        final IStrategoTerm decls = module.getSubterm(1);
        for(IStrategoTerm decl : decls) {
            final String declName = ((IStrategoAppl) decl).getConstructor().getName();
            if(declName.equals("Imports")) {
                final Collection<String> imports = extractImports(decl.getSubterm(0));
                outImports.addAll(imports);
            } else if(declName.equals("Signature")) {
                final Collection<ISig> sigs = extractAllSigs(decl.getSubterm(0));
                allSigs.addAll(sigs);
            }
        }

        return allSigs;
    }

    private Collection<String> extractImports(IStrategoTerm importDecls) {
        final List<String> imports = Lists.newArrayList();
        for(IStrategoTerm importDecl : importDecls) {
            final String importName = ((IStrategoString) importDecl.getSubterm(0)).stringValue();
            final String importDeclName = ((IStrategoAppl) importDecl).getConstructor().getName();
            if(importDeclName.equals("Import")) {
                imports.add(importName);
            } else {
                // Wildcard import
                imports.add(importName + "/-");
            }
        }
        return imports;
    }

    private Collection<ISig> extractAllSigs(IStrategoTerm consSections) {
        final List<ISig> sigs = Lists.newArrayList();
        for(IStrategoTerm consSection : consSections) {
            final String name = ((IStrategoAppl) consSection).getConstructor().getName();
            if(!name.equals("Constructors")) {
                continue;
            }

            for(IStrategoTerm sigTerm : consSection.getSubterm(0)) {
                final ISig sig = extractSig(sigTerm);
                if(sig != null) {
                    sigs.add(sig);
                }
            }
        }
        return sigs;
    }

    private @Nullable ISig extractSig(IStrategoTerm term) {
        final String kind = ((IStrategoAppl) term).getConstructor().getName();

        final @Nullable String cons;
        final IStrategoAppl sortTerm;
        if(kind.equals("OpDeclInj") || kind.equals("ExtOpDeclInj")) {
            cons = null;
            sortTerm = (IStrategoAppl) term.getSubterm(0);
        } else {
            cons = ((IStrategoString) term.getSubterm(0)).stringValue();
            sortTerm = (IStrategoAppl) term.getSubterm(1);
        }

        if(sortTerm.getName().equals("ConstType")) {
            if(cons == null) {
                logger.error("Stratego signature injection {} has nothing to inject into; skipping", term);
                return null;
            }
            final ISortArg sortArg = extractSortArg(sortTerm.getSubterm(0));
            if(sortArg == null) {
                return null;
            }
            final ISort sort = sortArg.sort();
            if(!(sort instanceof Sort)) {
                logger.error("Sort {} of Stratego constructor signature {} is not a normal sort; skipping", sort, term);
                return null;
            }
            final String sortName = ((Sort) sort).sort;
            return new ConstructorSig(sortName, cons);

        } else if(sortTerm.getName().equals("FunType")) {
            final IStrategoTerm[] argTerms = sortTerm.getSubterm(0).getAllSubterms();
            if(cons == null) {
                if(argTerms.length == 0) {
                    logger.error("Stratego signature injection {} has nothing to inject into; skipping", term);
                    return null;
                } else if(argTerms.length > 1) {
                    logger.error("Stratego signature injection {} has unsupported tuple injection; skipping", term);
                    return null;
                }
            }

            final List<ISortArg> args = Lists.newArrayList();
            for(IStrategoTerm argTerm : argTerms) {
                final ISortArg arg = extractSortArg(argTerm.getSubterm(0));
                if(arg == null) {
                    return null;
                }
                args.add(arg);
            }

            final ISortArg sortArg = extractSortArg(sortTerm.getSubterm(1).getSubterm(0));
            if(sortArg == null) {
                return null;
            }
            final ISort sort = sortArg.sort();
            if(!(sort instanceof Sort)) {
                logger.error("Sort {} of Stratego signature {} is not a normal sort; skipping", sort, term);
                return null;
            }
            final String sortName = ((Sort) sort).sort;
            if(cons != null) {
                return new ConstructorSig(sortName, cons, args);
            } else {
                final ISortArg firstSortArg = args.get(0);
                final ISort firstSort = firstSortArg.sort();
                return new InjectionSig(sortName, firstSort);
            }
        }
        logger.error("Unknown Stratego signature {}; skipping", term);
        return null;
    }

    private @Nullable ISortArg extractSortArg(IStrategoTerm term) {
        final String kind = ((IStrategoAppl) term).getConstructor().getName();

        final IStrategoTerm nameTerm = term.getSubterm(0);
        if(nameTerm.getTermType() != IStrategoTerm.STRING) {
            logger.error("Unsupported Stratego sort in {}; skipping", term);
            return null;
        }
        final String sortName = ((IStrategoString) nameTerm).stringValue();

        final @Nullable ISort sort;
        final String sortId = ""; // TODO: get sort id
        switch(kind) {
            case "SortNoArgs":
                switch(sortName) {
                    case lexicalSortName:
                        sort = new PrimitiveSort(PrimitiveSortType.String);
                        break;
                    case anySortName:
                        sort = new AnySort();
                        break;
                    default:
                        sort = new Sort(sortName);
                        break;
                }
                break;
            case "Sort":
                final ISortArg innerSortArg = extractSortArg(term.getSubterm(1).getSubterm(0));
                final ISort innerSort = innerSortArg.sort();
                if(innerSort != null) {
                    switch(sortName) {
                        case "List":
                            sort = new ListSort(innerSort);
                            break;
                        case "Option":
                            sort = new OptionalSort(innerSort);
                            break;
                        default:
                            logger.error("Unsupported Stratego parametric sort {} in {}; skipping", innerSort, term);
                            return null;
                    }
                } else {
                    return null;
                }
                break;
            default:
                logger.error("Unsupported Stratego sort in {}; skipping", term);
                return null;
        }

        return new SortArg(sort, sortId);
    }


    @Override public String toString() {
        return "stratego";
    }
}
