package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.termAt;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.attachments.OriginAttachment;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;

/**
 * @author Maartje de Jonge
 */
public class StrategoTextChangeCalculator {
	
	public Collection<TextFileChange> getFileChanges(final IStrategoTerm astChanges, final StrategoObserver observer, File file){
		IStrategoTerm textReplaceTerm = null;
    try {
      textReplaceTerm = observer.invoke("construct-textual-change", astChanges, file);
    } catch (InterpreterException e) {
      return null;
    }
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
