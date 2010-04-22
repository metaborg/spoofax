package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermString;

import java.util.ArrayList;
import java.util.List;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.ast.AstNodeFactory;
import org.strategoxt.imp.runtime.parser.ast.ListAstNode;
import org.strategoxt.imp.runtime.parser.ast.SubListAstNode;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * Returns the start and end position of the selection
 * The start and end position are relevant for selected sublists
 * @author Maartje de Jonge
 */
public class SelectedFromToPrimitive extends AbstractPrimitive {
	
	private static final String NAME = "SSL_EXT_selected_from_to_position";

	public SelectedFromToPrimitive() {
		super(NAME, 0, 2);
	}
	
	@Override
	public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		EditorState editor =EditorState.getActiveEditor();
		IStrategoAstNode node = editor.getSelectionAst(true);
		if (node == null) 
			node = editor.getCurrentAst();
		IStrategoList posStart;
		IStrategoList posEnd;
		if (node instanceof SubListAstNode){
			int start = ((SubListAstNode)node).getIndexStart();
			posStart = createPathToSublistChild((SubListAstNode)node, start);
			int end = ((SubListAstNode)node).getIndexEnd();
			posEnd = createPathToSublistChild((SubListAstNode)node, end);

		} else{
			posStart=StrategoTermPath.createPath(node);
			posEnd=StrategoTermPath.createPath(node);
		}
		env.setCurrent(
			env.getFactory().makeTuple(
					posStart,
					posEnd
			)
		);
		return true;
	}

	private IStrategoList createPathToSublistChild(SubListAstNode node, int posInCompleteList) {
		IStrategoList posStart;
		List<Integer> pathToStart = StrategoTermPath.createPathList(node);
		pathToStart.add(Integer.valueOf(posInCompleteList));
		posStart =StrategoTermPath.toStrategoPath(pathToStart);
		return posStart;
	}
	
}
