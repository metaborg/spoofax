package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.termAt;

import java.util.Collection;
import java.util.HashSet;
import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.attachments.OriginAttachment;
import org.strategoxt.imp.generator.construct_textual_change_4_0;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

/**
 * @author Maartje de Jonge
 */
public class StrategoTextChangeCalculator {
	
	private final String ppStrategy;

	private final String parenthesizeStrategy;

	private final String overrideReconstructionStrategy;

	private final String resugarStrategy;
	
	public StrategoTextChangeCalculator(String ppStrategy, String parenthesize, String violatesHomomorphism, String resugar){
		this.ppStrategy = ppStrategy;
		this.parenthesizeStrategy = parenthesize;
		this.overrideReconstructionStrategy = violatesHomomorphism;
		this.resugarStrategy = resugar;
	}
	
	public Collection<TextFileChange> getFileChanges(final IStrategoTerm astChanges, final StrategoObserver observer){
		IStrategoTerm textReplaceTerm = getTextReplacement(astChanges, observer);
		if (textReplaceTerm == null) {
			return null;
		}
		assert(textReplaceTerm.getSubtermCount() == astChanges.getSubtermCount());
		Collection<TextFileChange> fileChanges = new HashSet<TextFileChange>();
		for (int i = 0; i < astChanges.getSubtermCount(); i++) {
			TextFileChange fChange = createTextChange(termAt(astChanges.getSubterm(i),0), textReplaceTerm.getSubterm(i));	
			fileChanges.add(fChange);
		}
		return fileChanges;
	}
	
	private IStrategoTerm getTextReplacement(final IStrategoTerm resultTuple, final StrategoObserver observer) {
		IStrategoTerm textreplace=construct_textual_change_4_0.instance.invoke(
				observer.getRuntime().getCompiledContext(), 
				resultTuple, 
				createStrategy(ppStrategy, observer),
				createStrategy(parenthesizeStrategy, observer),
				createStrategy(overrideReconstructionStrategy, observer),
				createStrategy(resugarStrategy, observer)
			);
		return textreplace;
	}

	private Strategy createStrategy(final String sname, final StrategoObserver observer) {
		return new Strategy() {
			@Override
			public IStrategoTerm invoke(Context context, IStrategoTerm current) {
				if (sname != null)
					return observer.invokeSilent(sname, current);
				return null;
			}
		};
	}
	
	private TextFileChange createTextChange(IStrategoTerm originalTerm, IStrategoTerm textReplaceTerm) {
		final int startLocation=Tools.asJavaInt(termAt(textReplaceTerm, 0));
		final int endLocation=Tools.asJavaInt(termAt(textReplaceTerm, 1));
		final String resultText = Tools.asJavaString(termAt(textReplaceTerm, 2));
		final IStrategoTerm originTerm = OriginAttachment.tryGetOrigin(originalTerm);
		final IFile file = (IFile)SourceAttachment.getResource(originTerm);

		TextFileChange textChange = new TextFileChange("", file); 
		textChange.setTextType(file.getFileExtension());
		MultiTextEdit edit= new MultiTextEdit();
		edit.addChild(new ReplaceEdit(startLocation, endLocation - startLocation, resultText));
		textChange.setEdit(edit);
		return textChange;
	}

}
