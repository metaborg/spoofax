package org.metaborg.spoofax.meta.core.pluto.build.aux;

import java.io.File;
import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.util.ExecutableCommandStrategy;
import org.metaborg.spoofax.nativebundle.NativeBundle;

import build.pluto.stamp.LastModifiedStamper;

import com.google.common.base.Objects;

public class Sdf2TablePrepareExecutable extends SpoofaxBuilder<SpoofaxInput, Sdf2TablePrepareExecutable.Output> {

    public static SpoofaxBuilderFactory<SpoofaxInput, Output, Sdf2TablePrepareExecutable> factory =
        SpoofaxBuilderFactoryFactory.of(Sdf2TablePrepareExecutable.class, SpoofaxInput.class);

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = -6018464107000421068L;

        public final ExecutableCommandStrategy sdf2table;
        public final ExecutableCommandStrategy implodePT;

        public Output(ExecutableCommandStrategy sdf2table, ExecutableCommandStrategy implodePT) {
            super();
            this.sdf2table = sdf2table;
            this.implodePT = implodePT;
        }

        @Override public boolean equals(Object obj) {
            if(!(obj instanceof Output))
                return false;

            Output other = (Output) obj;
            return Objects.equal(implodePT, other.implodePT) && Objects.equal(sdf2table, other.sdf2table);
        }

        @Override public int hashCode() {
            return Objects.hashCode(implodePT, sdf2table);
        }
    }

    public Sdf2TablePrepareExecutable(SpoofaxInput input) {
        super(input);
    }

    @Override protected String description(SpoofaxInput input) {
        return "Prepare sdf2table exeuctable";
    }

    @Override
    public File persistentPath(SpoofaxInput input) {
        return context.depPath("sdf2Table.executable.dep");
    }

    @Override public Output build(SpoofaxInput input) throws IOException {
        final FileObject sdf2TablePath = context.resourceService.resolve(NativeBundle.getSdf2Table().toString());
        final File sdf2TableFile = context.resourceService.localFile(sdf2TablePath);
        sdf2TableFile.setExecutable(true);

        final FileObject implodePtPath = context.resourceService.resolve(NativeBundle.getImplodePT().toString());
        final File implodePtFile = context.resourceService.localFile(implodePtPath);
        implodePtFile.setExecutable(true);

        provide(sdf2TableFile, LastModifiedStamper.instance);
        provide(implodePtFile, LastModifiedStamper.instance);

        return new Output(ExecutableCommandStrategy.getInstance("sdf2table", sdf2TableFile),
            ExecutableCommandStrategy.getInstance("implodePT", implodePtFile));
    }
}
