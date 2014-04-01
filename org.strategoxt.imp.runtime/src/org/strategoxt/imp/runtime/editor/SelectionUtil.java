package org.strategoxt.imp.runtime.editor;

import org.eclipse.jface.text.Region;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;

public class SelectionUtil {

	public static IStrategoTerm getSelectionAst(int offset, int length, boolean ignoreEmptySelection, SGLRParseController parseController) {
		if (ignoreEmptySelection && length == 0)
			return null;
		
		IToken start = parseController.getTokenIterator(new Region(offset, 0), true).next();
		IToken end = parseController.getTokenIterator(new Region(offset + Math.max(0, length-1), 0), true).next();
		
		ITokenizer tokens = start.getTokenizer();
		int layout = IToken.TK_LAYOUT;
		int eof = IToken.TK_EOF;
		
		while (start.getKind() == layout && start.getIndex() < tokens.getTokenCount())
			start = tokens.getTokenAt(start.getIndex() + 1);
		
		while ((end.getKind() == layout || end.getKind() == eof) && end.getIndex() > 0)
			end = tokens.getTokenAt(end.getIndex() - 1);
		
		IStrategoTerm startNode = (IStrategoTerm) start.getAstNode();
		IStrategoTerm endNode = (IStrategoTerm) end.getAstNode();

		return StrategoTermPath.findCommonAncestor(startNode, endNode);
	}
	
	public static IStrategoTerm getSelectionAstAnalyzed(int offset, int length, boolean ignoreEmptySelection, SGLRParseController parseController) {
		IStrategoTerm selectionAst = getSelectionAst(offset, length, ignoreEmptySelection, parseController);
		EditorState editorState = EditorState.getEditorFor(parseController);
		
		if (selectionAst != null) {
			try {
				StrategoObserver observer = editorState.getDescriptor().createService(StrategoObserver.class, editorState.getParseController());
				IStrategoTerm analyzedAst = editorState.getCurrentAnalyzedAst() == null? editorState.getAnalyzedAst() : editorState.getCurrentAnalyzedAst();
				
				IStrategoList path = StrategoTermPath.getTermPathWithOrigin(observer, analyzedAst, selectionAst);
				if (path != null) {
					return StrategoTermPath.getTermAtPath(observer, editorState.getCurrentAnalyzedAst(), path);
				}
			} catch (BadDescriptorException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
