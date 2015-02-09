package org.metaborg.spoofax.build.cleardep;


import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.Log;
import org.sugarj.common.path.Path;

public class PPPack extends Builder<SpoofaxBuildContext, PPPack.Input, SimpleCompilationUnit> {

	public static class Input {
		public final Path ppInput;
		public final Path ppTermOutput;
		public Input(Path ppInput, Path ppTermOutput) {
			this.ppInput = ppInput;
			this.ppTermOutput = ppTermOutput;
		}
	}
	
	public PPPack(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) {
		Log.log.beginTask("Package pretty-print table", Log.CORE);
		
		Log.log.endTask();
	}

}
