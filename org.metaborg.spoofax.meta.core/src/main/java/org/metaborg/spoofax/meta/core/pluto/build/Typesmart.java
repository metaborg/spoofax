package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.build.misc.ParseFile;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.typesmart.TypesmartContext;
import org.spoofax.terms.typesmart.types.SortType;
import org.spoofax.terms.typesmart.types.TAny;
import org.spoofax.terms.typesmart.types.TLexical;
import org.spoofax.terms.typesmart.types.TList;
import org.spoofax.terms.typesmart.types.TOption;
import org.spoofax.terms.typesmart.types.TSort;
import org.spoofax.terms.typesmart.types.TTuple;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.output.Out;
import org.spoofax.terms.util.TermUtils;

public class Typesmart extends SpoofaxBuilder<Typesmart.Input, None> {
    private static final ILogger logger = LoggerUtils.logger("Typesmart");

    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final File strFile;
        public final List<File> strjIncludeDirs;
        public final File typesmartExportedFile;
        public final Origin origin;

        public Input(SpoofaxContext context, File strFile, List<File> strjIncludeDirs, File typesmartExportedFile,
            @Nullable Origin origin) {
            super(context);
            this.strFile = strFile;
            this.strjIncludeDirs = strjIncludeDirs;
            this.typesmartExportedFile = typesmartExportedFile;
            this.origin = origin;
        }
    }


    public static final SpoofaxBuilderFactory<Input, None, Typesmart> factory =
        SpoofaxBuilderFactoryFactory.of(Typesmart.class, Input.class);


    public Typesmart(Input input) {
        super(input);
    }

    public static BuildRequest<Input, None, Typesmart, SpoofaxBuilderFactory<Input, None, Typesmart>>
        request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }

    @Override protected String description(Input input) {
        return "Generate typesmart analysis";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("typesmart.syntax.dep");
    }

    private Map<String, Set<List<SortType>>> constructorSignatures = new HashMap<>();
    private Set<SortType> lexicals = new HashSet<>();
    private Set<Entry<SortType, SortType>> injections = new HashSet<>();

    @Override public None build(Input input) throws IOException {

        processMainStrategoFile(input.strFile, input.strjIncludeDirs);

        constructorSignatures = Collections.unmodifiableMap(constructorSignatures);
        lexicals = Collections.unmodifiableSet(lexicals);
        injections = Collections.unmodifiableSet(injections);
        TypesmartContext typesmartContext = new TypesmartContext(constructorSignatures, lexicals, injections);
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(input.typesmartExportedFile))) {
            oos.writeObject(typesmartContext);
        }
        provide(input.typesmartExportedFile);

        return None.val;
    }

    private void processMainStrategoFile(File strFile, List<File> strjIncludeDirs) throws IOException {
        Set<String> seenImports = new HashSet<>();
        Set<File> seenFiles = new HashSet<>();
        LinkedList<String> todo = new LinkedList<>();
        String basePath = context.baseDir.getAbsolutePath();
        IStrategoTerm term = parseStratego(strFile);
        todo.addAll(processModule(term));

        boolean isRuntimeLibrary =
            strFile.getAbsolutePath().endsWith("org.metaborg.meta.lib.analysis/trans/runtime_libraries.str");

        while(!todo.isEmpty()) {
            String next = todo.pop();
            if(!isRuntimeLibrary && next.startsWith("runtime/") || !seenImports.add(next)) {
                continue;
            }

            Collection<File> files = findStrFiles(next, strjIncludeDirs);

            if(files.isEmpty() && !next.startsWith("lib")) {
                logger.warn("Could not extract typesmart info for unresolvable import " + next);
            }

            for(File file : files) {
                if(file.getAbsolutePath().startsWith(basePath) && seenFiles.add(file)) {
                    // logger.debug("Entering module " + next);
                    term = parseStratego(file);
                    todo.addAll(processModule(term));
                }
            }
        }
    }

    private IStrategoTerm parseStratego(File file) throws IOException {
        Out<IStrategoTerm> out =
            requireBuild(ParseFile.factory, new ParseFile.Input(context, file, true, true, getInput().origin));
        return out == null ? null : out.val();
    }

    private Collection<File> findStrFiles(String imp, List<File> strjIncludeDirs) {
        if(imp.endsWith("/-")) {
            String path = imp.substring(0, imp.length() - 2);
            for(File include : strjIncludeDirs) {
                File file = new File(include, path);
                if(file.exists() && file.isDirectory()) {
                    return FileUtils.listFiles(file, new String[] { "str" }, false);
                }
            }
        } else {
            for(File include : strjIncludeDirs) {
                File file = new File(include, imp + ".str");
                if(file.exists()) {
                    return Collections.singletonList(file);
                }
            }
        }
        return Collections.emptyList();
    }

    private List<String> processModule(IStrategoTerm module) {
    	if(module == null) {
    		return Collections.emptyList();
    	}
        assert ((IStrategoAppl) module).getName().equals("Module");

        List<String> imports = new ArrayList<>();

        IStrategoList decls = TermUtils.toListAt(module, 1);
        for(IStrategoTerm decl : decls.getSubterms()) {
            String declName = ((IStrategoAppl) decl).getName();
            if(declName.equals("Imports")) {
                extractImports(decl.getSubterm(0), imports);
            } else if(declName.equals("Signature")) {
                processSignature(decl.getSubterm(0));
            }
        }

        return imports;
    }

    private void extractImports(IStrategoTerm importDecls, List<String> imports) {
        for(IStrategoTerm importDecl : importDecls.getSubterms()) {
            String importName = TermUtils.toJavaStringAt(importDecl, 0);

            String importDeclName = TermUtils.toAppl(importDecl).getName();
            if(importDeclName.equals("Import")) {
                imports.add(importName);
            } else {
                // wildcard import
                imports.add(importName + "/-");
            }
        }
    }

    private void processSignature(IStrategoTerm sigDecls) {
        for(IStrategoTerm decl : sigDecls.getSubterms()) {
            String declName = TermUtils.toAppl(decl).getName();
            if(!declName.equals("Constructors")) {
                continue;
            }

            next_constr: for(IStrategoTerm constr : decl.getSubterm(0).getSubterms()) {
                String kind = TermUtils.toAppl(constr).getName();

                String cname;
                IStrategoAppl typeTerm;
                if(kind.equals("OpDeclInj") || kind.equals("ExtOpDeclInj")) {
                    cname = "";
                    typeTerm = TermUtils.toApplAt(constr, 0);
                } else {
                    cname = TermUtils.toJavaStringAt(constr, 0);
                    typeTerm = TermUtils.toApplAt(constr, 1);
                }

                List<SortType> sortTypes;
                if(typeTerm.getName().equals("ConstType")) {
                    // no constructor arguments
                    sortTypes = new ArrayList<>(1);
                    SortType t = extractSortType(typeTerm.getSubterm(0));
                    if(t == null) {
                        continue next_constr;
                    }
                    sortTypes.add(t);
                } else if(typeTerm.getName().equals("FunType")) {
                    IStrategoTerm[] argTypes = typeTerm.getSubterm(0).getAllSubterms();
                    sortTypes = new ArrayList<>(argTypes.length + 1);

                    for(IStrategoTerm argType : argTypes) {
                        SortType t = extractSortType(argType.getSubterm(0));
                        if(t == null) {
                            continue next_constr;
                        }
                        sortTypes.add(t);
                    }
                    SortType t = extractSortType(typeTerm.getSubterm(1).getSubterm(0));
                    if(t == null) {
                        continue next_constr;
                    }
                    sortTypes.add(t);
                } else {
                    throw new IllegalArgumentException("Found constructor declaration in unexpected format " + constr);
                }

                addConstructorSignature(cname, sortTypes);
            }
        }
    }

    private void addConstructorSignature(String cname, List<SortType> sortTypes) {
        if(cname.equals("") && sortTypes.size() == 2) {
            // injection
            assert sortTypes.size() == 2;

            if(sortTypes.get(0).equals(TLexical.instance)) {
                // lexical
                lexicals.add(sortTypes.get(1));
            } else {
                // non-lexical
                injections.add(new SimpleEntry<>(sortTypes.get(0), sortTypes.get(1)));
            }
        } else if(!cname.equals("")) {
            // constructor signature
            // logger.debug(" " + cname + ": " + sortTypes);
            Set<List<SortType>> csigs = constructorSignatures.get(cname);
            if(csigs == null) {
                csigs = new HashSet<>();
                constructorSignatures.put(cname, csigs);
            }
            csigs.add(Collections.unmodifiableList(sortTypes));
        }
    }

    private SortType extractSortType(IStrategoTerm sort) {
        String kind = ((IStrategoAppl) sort).getName();

        if(kind.equals("SortList") || kind.equals("SortListTl") || kind.equals("SortVar")) {
            logger.error("Unsupported Stratego signature: " + sort);
            return TAny.instance;
        } else if(kind.equals("SortTuple")) {
            IStrategoTerm[] kids = sort.getSubterm(0).getAllSubterms();
            SortType[] sorts = new SortType[kids.length];
            for(int i = 0; i < kids.length; i++) {
                sorts[i] = extractSortType(kids[i]);
            }
            return new TTuple(sorts);
        }
        
        if(!TermUtils.isStringAt(sort, 0)) {
            throw new IllegalArgumentException("Found type in unexpected format " + sort);
        }
        
        String sortName = TermUtils.toJavaStringAt(sort, 0);
        if(kind.equals("SortNoArgs") && sortName.equals(SortType.LEXICAL_SORT)) {
            return TLexical.instance;
        } else if(kind.equals("SortNoArgs") && sortName.equals(SortType.ANY_SORT)) {
            return TAny.instance;
        } else if(kind.equals("SortNoArgs")) {
            return new TSort(sortName);
        } else if(kind.equals("Sort") && sortName.equals("List")) {
            SortType t = extractSortType(sort.getSubterm(1).getSubterm(0));
            return t == null ? null : new TList(t);
        } else if(kind.equals("Sort") && sortName.equals("Option")) {
            SortType t = extractSortType(sort.getSubterm(1).getSubterm(0));
            return t == null ? null : new TOption(t);
        } else {
            logger.error("Unsupported Stratego signature: " + sort);
            return TAny.instance;
        }
    }
}
