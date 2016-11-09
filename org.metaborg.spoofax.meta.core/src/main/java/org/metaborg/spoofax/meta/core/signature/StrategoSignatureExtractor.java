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
import org.metaborg.meta.core.signature.AnySortType;
import org.metaborg.meta.core.signature.ISignatureExtractor;
import org.metaborg.meta.core.signature.ISortType;
import org.metaborg.meta.core.signature.ListSortType;
import org.metaborg.meta.core.signature.OptionalSortType;
import org.metaborg.meta.core.signature.PrimitiveSortKind;
import org.metaborg.meta.core.signature.PrimitiveSortType;
import org.metaborg.meta.core.signature.Signature;
import org.metaborg.meta.core.signature.TupleSortType;
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

public class StrategoSignatureExtractor implements ISignatureExtractor {
    private static final String strategoLangName = "Stratego-Sugar";
    private static final String lexicalSortName = "String";
    private static final String anySortName = "T_Any";

    private static final ILogger logger = LoggerUtils.logger(StrategoSignatureExtractor.class);

    private final ILanguageIdentifierService languageIdentifierService;
    private final ISpoofaxInputUnitService inputService;
    private final ISourceTextService sourceTextService;
    private final ISpoofaxSyntaxService syntaxService;
    private final ILanguagePathService languagePathService;


    @Inject public StrategoSignatureExtractor(ILanguageIdentifierService languageIdentifierService,
        ISpoofaxInputUnitService inputService, ISourceTextService sourceTextService,
        ISpoofaxSyntaxService syntaxService, ILanguagePathService languagePathService) {
        this.languageIdentifierService = languageIdentifierService;
        this.inputService = inputService;
        this.sourceTextService = sourceTextService;
        this.syntaxService = syntaxService;
        this.languagePathService = languagePathService;
    }


    @Override public Collection<Signature> extract(ILanguageSpec languageSpec, @Nullable IFileAccess access)
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

    private Collection<Signature> extractStartingFrom(IdentifiedResource mainFile, Iterable<FileObject> includePaths,
        @Nullable IFileAccess access) throws IOException, ParseException {
        final Set<String> seenImports = Sets.newHashSet();
        final Set<FileObject> seenFiles = Sets.newHashSet();
        final LinkedList<String> importsTodo = Lists.newLinkedList();
        final Collection<Signature> allSignatures = Lists.newArrayList();

        final IStrategoTerm mainTerm = parseStratego(mainFile);
        if(mainTerm == null) {
            return allSignatures;
        }
        final Collection<Signature> mainFileSignatures = extractModule(mainTerm, importsTodo);
        allSignatures.addAll(mainFileSignatures);

        while(!importsTodo.isEmpty()) {
            final String nextImport = importsTodo.pop();
            if(!seenImports.add(nextImport)) {
                continue;
            }

            final Collection<FileObject> files = findStrFiles(nextImport, includePaths);
            if(files.isEmpty()) {
                logger.warn("Could not extract signatures for unresolvable import {}", nextImport);
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
                    final Collection<Signature> signatures = extractModule(term, importsTodo);
                    allSignatures.addAll(signatures);
                }
            }
        }

        return allSignatures;
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

    private Collection<Signature> extractModule(IStrategoTerm module, List<String> outImports) {
        final List<Signature> allSignatures = Lists.newArrayList();
        if(!((IStrategoAppl) module).getConstructor().getName().equals("Module")) {
            logger.error("Unknown Stratego module {}; skipping", module);
            return allSignatures;
        }

        final IStrategoTerm decls = module.getSubterm(1);
        for(IStrategoTerm decl : decls) {
            final String declName = ((IStrategoAppl) decl).getConstructor().getName();
            if(declName.equals("Imports")) {
                final Collection<String> imports = extractImports(decl.getSubterm(0));
                outImports.addAll(imports);
            } else if(declName.equals("Signature")) {
                final Collection<Signature> signatures = extractSignatures(decl.getSubterm(0));
                allSignatures.addAll(signatures);
            }
        }

        return allSignatures;
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

    private Collection<Signature> extractSignatures(IStrategoTerm sigDecls) {
        final List<Signature> signatures = Lists.newArrayList();

        for(IStrategoTerm decl : sigDecls) {
            final String declName = ((IStrategoAppl) decl).getConstructor().getName();
            if(!declName.equals("Constructors")) {
                continue;
            }

            next_constr: for(IStrategoTerm signatureTerm : decl.getSubterm(0)) {
                final String kind = ((IStrategoAppl) signatureTerm).getConstructor().getName();

                final @Nullable String constructor;
                final IStrategoAppl typeTerm;
                if(kind.equals("OpDeclInj") || kind.equals("ExtOpDeclInj")) {
                    constructor = null;
                    typeTerm = (IStrategoAppl) signatureTerm.getSubterm(0);
                } else {
                    constructor = ((IStrategoString) signatureTerm.getSubterm(0)).stringValue();
                    typeTerm = (IStrategoAppl) signatureTerm.getSubterm(1);
                }

                final Signature signature;
                if(typeTerm.getName().equals("ConstType")) {
                    // no constructor arguments
                    final ISortType type = extractSortType(typeTerm.getSubterm(0));
                    if(type == null) {
                        continue next_constr;
                    }
                    signature = new Signature(type, constructor);
                } else if(typeTerm.getName().equals("FunType")) {
                    final IStrategoTerm[] argTypeTerms = typeTerm.getSubterm(0).getAllSubterms();
                    final List<ISortType> argTypes = Lists.newArrayList();
                    for(IStrategoTerm argTypeTerm : argTypeTerms) {
                        final ISortType argType = extractSortType(argTypeTerm.getSubterm(0));
                        if(argType == null) {
                            continue next_constr;
                        }
                        argTypes.add(argType);
                    }
                    final ISortType type = extractSortType(typeTerm.getSubterm(1).getSubterm(0));
                    if(type == null) {
                        continue next_constr;
                    }
                    signature = new Signature(type, constructor, argTypes);
                } else {
                    logger.error("Unknown Stratego signature declaration {}; skipping", signatureTerm);
                    continue next_constr;
                }

                signatures.add(signature);
            }
        }

        return signatures;
    }

    private @Nullable ISortType extractSortType(IStrategoTerm sort) {
        final String kind = ((IStrategoAppl) sort).getConstructor().getName();

        if(kind.equals("SortList") || kind.equals("SortListTl") || kind.equals("SortVar")) {
            logger.error("Unsupported Stratego sort in {}; skipping ", sort);
            return null;
        } else if(kind.equals("SortTuple")) {
            final List<ISortType> nestedSortTypes = Lists.newArrayList();
            for(IStrategoTerm subterm : sort) {
                nestedSortTypes.add(extractSortType(subterm));
            }
            return new TupleSortType(nestedSortTypes);
        }

        final IStrategoTerm sortTerm = sort.getSubterm(0);
        if(sortTerm.getTermType() != IStrategoTerm.STRING) {
            logger.error("Unknown Stratego sort in {}; skipping", sort);
            return null;
        }
        final String sortName = ((IStrategoString) sortTerm).stringValue();

        if(kind.equals("SortNoArgs") && sortName.equals(lexicalSortName)) {
            return new PrimitiveSortType(PrimitiveSortKind.String);
        } else if(kind.equals("SortNoArgs") && sortName.equals(anySortName)) {
            return new AnySortType();
        } else if(kind.equals("SortNoArgs")) {
            return new org.metaborg.meta.core.signature.SortType(sortName);
        } else if(kind.equals("Sort") && sortName.equals("List")) {
            final ISortType argument = extractSortType(sort.getSubterm(1).getSubterm(0));
            if(argument != null) {
                return new ListSortType(argument);
            }
        } else if(kind.equals("Sort") && sortName.equals("Option")) {
            final ISortType argument = extractSortType(sort.getSubterm(1).getSubterm(0));
            if(argument != null) {
                return new OptionalSortType(argument);
            }
        } else if(kind.equals("SortVar")) {
            return null;
        } else {
            logger.error("Unknown Stratego sort type in {}; skipping", sort);
        }
        return null;
    }


    @Override public String toString() {
        return "stratego";
    }
}
