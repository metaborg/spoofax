package org.metaborg.spoofax.build.cleardep.builders;

import java.io.File;
import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.FileState;
import org.strategoxt.imp.runtime.services.OnSaveService;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.services.StrategoObserverUpdateJob;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.Log;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class ForceOnSaveFile extends Builder<SpoofaxBuildContext, Path, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Path, SimpleCompilationUnit, ForceOnSaveFile> factory = new BuilderFactory<SpoofaxBuildContext, Path, SimpleCompilationUnit, ForceOnSaveFile>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4999047735018095459L;

		@Override
		public ForceOnSaveFile makeBuilder(SpoofaxBuildContext context) { return new ForceOnSaveFile(context); }
	};
	
	private ForceOnSaveFile(SpoofaxBuildContext context) {
		super(context, factory);
	}
	
	@Override
	protected String taskDescription(Path input) {
		return "Force on-save handler for " + input;
	}
	
	@Override
	protected Path persistentPath(Path input) {
		RelativePath rel = FileCommands.getRelativePath(context.baseDir, input);
		String relname = rel.getRelativePath().replace(File.separatorChar, '_');
		return context.depPath("forceOnSaveFile/" + relname + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Path input) throws IOException {
		RelativePath p = FileCommands.getRelativePath(context.baseDir, input);
		
		result.addSourceArtifact(p);
		callOnSaveService(p);
		switch(FileCommands.getExtension(p)) {
//			case "tmpl": 
//				break;
		case "sdf3":
			RelativePath sdf3 = FileCommands.getRelativePath(context.basePath("syntax"), p);
			if (sdf3 == null)
				break;
			String sdf3RelNoExt = FileCommands.dropExtension(sdf3.getRelativePath());
			
			RelativePath genSdf = FileCommands.replaceExtension(context.basePath("${syntax}/" + sdf3.getRelativePath()), "sdf");
			RelativePath genPP = context.basePath("${pp}/" + sdf3RelNoExt + "-pp.str");
			RelativePath genCompletions = context.basePath("${completions}/" + sdf3RelNoExt + "-esv.esv");
			RelativePath genSignatures = context.basePath("${signatures}/" + sdf3RelNoExt + "-sig.str");
			result.addGeneratedFile(genSdf);
			result.addGeneratedFile(genPP);
			result.addGeneratedFile(genCompletions);
			result.addGeneratedFile(genSignatures);
			break;
		case "nab":
			RelativePath gen = FileCommands.replaceExtension(p, "str");
			result.addGeneratedFile(gen);
			break;
		case "ts":
			gen = FileCommands.replaceExtension(p, "generated.str");
			result.addGeneratedFile(gen);
			break;
		default:
			throw new UnsupportedOperationException("Dependency management not implemented for files with extension " + FileCommands.getExtension(p) + ". File was " + p);
		}
	}
	
	private void callOnSaveService(RelativePath p) {
		try {
			FileState fileState = FileState.getFile(new org.eclipse.core.runtime.Path(p.getAbsolutePath()), null);
			if (fileState == null) {
				Log.log.logErr("Could not call on-save handler: File state could not be retrieved for file " + p, Log.CORE);
				return;
			}
			StrategoObserver observer = fileState.getDescriptor().createService(StrategoObserver.class, fileState.getParseController());
			SSLLibrary lib = SSLLibrary.instance(observer.getRuntime().getContext());
			if (lib.getIOAgent() instanceof EditorIOAgent && ((EditorIOAgent) lib.getIOAgent()).getJob() == null)
				((EditorIOAgent) lib.getIOAgent()).setJob(new StrategoObserverUpdateJob(observer));
			IStrategoTerm ast = fileState.getAnalyzedAst();
			OnSaveService onSave = fileState.getDescriptor().createService(OnSaveService.class, fileState.getParseController());
			onSave.invokeOnSave(ast);
		} catch (Exception e) {
			throw new RuntimeException("Could not call on-save handler.", e);
		}
	}

}
