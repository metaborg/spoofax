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
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.typesmart.TypesmartContext;
import org.spoofax.terms.typesmart.types.SortType;
import org.spoofax.terms.typesmart.types.TList;
import org.spoofax.terms.typesmart.types.TOption;
import org.spoofax.terms.typesmart.types.TSort;

import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.OutputPersisted;

public class Typesmart extends SpoofaxBuilder<Typesmart.Input, OutputPersisted<File>> {

    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -2379365089609792204L;

        public final File strFile;
        public final List<File> strjIncludeDirs;
        public final File outFile;
        public final Origin origin;

        public Input(SpoofaxContext context, File strFile, List<File> strjIncludeDirs, File outFile,
            @Nullable Origin origin) {
            super(context);
            this.strFile = strFile;
            this.strjIncludeDirs = strjIncludeDirs;
            this.outFile = outFile;
            this.origin = origin;
        }
    }


    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, Typesmart> factory =
        SpoofaxBuilderFactoryFactory.of(Typesmart.class, Input.class);


    public Typesmart(Input input) {
        super(input);
    }

    public static
        BuildRequest<Input, OutputPersisted<File>, Typesmart, SpoofaxBuilderFactory<Input, OutputPersisted<File>, Typesmart>>
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
        return context.depPath("stratego.typesmart.dep");
    }

    private Map<String, Set<List<SortType>>> constructorSignatures = new HashMap<>();
    private Set<SortType> lexicals = new HashSet<>();
    private Set<Entry<SortType, SortType>> injections = new HashSet<>();

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        requireBuild(input.origin);

        Set<File> seen = new HashSet<>();
        LinkedList<String> todo = new LinkedList<>();

        IStrategoTerm term = parseStratego(input.strFile);
        todo.addAll(processModule(term));

        while(!todo.isEmpty()) {
            String next = todo.pop();
            Collection<File> files = findStrFiles(next, input.strjIncludeDirs);
            if(files.isEmpty()) {
                // TODO log warning
            }

            for(File file : files) {
                if(seen.add(file)) {
                    term = parseStratego(file);
                    todo.addAll(processModule(term));
                }
            }
        }

        constructorSignatures = Collections.unmodifiableMap(constructorSignatures);
        lexicals = Collections.unmodifiableSet(lexicals);
        injections = Collections.unmodifiableSet(injections);
        TypesmartContext typesmartContext = new TypesmartContext(constructorSignatures, lexicals, injections);

        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(input.outFile))) {
            oos.writeObject(typesmartContext);
        }

        return OutputPersisted.of(input.outFile);
    }

    private IStrategoTerm parseStratego(File file) {
        require(file);
        // TODO
        return null;
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
        assert ((IStrategoAppl) module).getName().equals("Module");

        List<String> imports = new ArrayList<>();

        IStrategoList decls = (IStrategoList) module.getSubterm(1);
        for(IStrategoTerm decl : decls) {
            String declName = ((IStrategoAppl) decl).getName();
            if(declName.equals("Imports")) {
                extractImports(decl, imports);
            } else if(declName.equals("Signature")) {
                processSignature(decl);
            }
        }

        return imports;
    }

    private void extractImports(IStrategoTerm importDecls, List<String> imports) {
        for(IStrategoTerm importDecl : importDecls) {
            String importName = ((IStrategoString) importDecl.getSubterm(0)).stringValue();

            String importDeclName = ((IStrategoAppl) importDecl).getName();
            if(importDeclName.equals("Import")) {
                imports.add(importName);
            } else {
                // wildcard import
                imports.add(importName + "/-");
            }
        }
    }

    private void processSignature(IStrategoTerm sigDecls) {
        for(IStrategoTerm decl : sigDecls) {
            String declName = ((IStrategoAppl) decl).getName();
            if(!declName.equals("Constructors")) {
                continue;
            }

            for(IStrategoTerm constr : decl) {
                assert ((IStrategoAppl) constr).getName().endsWith("OpDecl");

                String cname = ((IStrategoString) constr.getSubterm(0)).stringValue();
                IStrategoAppl typeTerm = (IStrategoAppl) constr.getSubterm(1);

                List<SortType> sortTypes;

                if(typeTerm.getName().equals("ConstType")) {
                    // no constructor arguments
                    sortTypes = new ArrayList<>(1);
                    sortTypes.set(0, extractConstType(typeTerm));
                } else if(typeTerm.getName().equals("FunType")) {
                    IStrategoTerm[] argTypes = typeTerm.getSubterm(0).getAllSubterms();
                    sortTypes = new ArrayList<>(argTypes.length + 1);

                    for(int j = 0; j < argTypes.length; j++) {
                        sortTypes.set(j, extractConstType((IStrategoAppl) argTypes[j]));
                    }
                    sortTypes.set(argTypes.length, extractConstType((IStrategoAppl) typeTerm.getSubterm(1)));
                } else {
                    throw new IllegalArgumentException("Found constructor declaration in unexpected format " + constr);
                }

                addConstructorSignature(cname, sortTypes);
            }
        }
    }

    private void addConstructorSignature(String cname, List<SortType> sortTypes) {
        if(cname.equals("")) {
            // injection
            assert sortTypes.size() == 2;

            if(sortTypes.get(0).equals(SortType.LEXICAL_SORT)) {
                // lexical
                lexicals.add(sortTypes.get(1));
            } else {
                // non-lexical
                injections.add(new SimpleEntry<>(sortTypes.get(0), sortTypes.get(1)));
            }
        } else {
            // constructor signature
            Set<List<SortType>> csigs = constructorSignatures.get(cname);
            if(csigs == null) {
                csigs = new HashSet<>();
                constructorSignatures.put(cname, csigs);
            }
            csigs.add(Collections.unmodifiableList(sortTypes));
        }
    }

    private SortType extractConstType(IStrategoAppl term) {
        assert term.getName().equals("ConstType");

        IStrategoAppl sort = (IStrategoAppl) term.getSubterm(0);

        String sortName = ((IStrategoString) sort.getSubterm(0)).stringValue();

        if(sortName.equals("List")) {
            return new TList(extractConstType((IStrategoAppl) sort.getSubterm(1).getSubterm(0)));
        } else if(sortName.equals("Option")) {
            return new TOption(extractConstType((IStrategoAppl) sort.getSubterm(1).getSubterm(0)));
        } else if(sort.getSubtermCount() == 1) {
            return new TSort(sortName);
        } else {
            throw new IllegalArgumentException("Found type in unexpected format " + term);
        }
    }
}
