package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermString;

import java.util.ArrayList;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.parser.ast.SubListAstNode;
import static org.spoofax.terms.attachments.ParentAttachment.getParent;

/**
 * Returns the (sub)list with origin nodes by mapping all subterms of a list one by one.
 * It is checked that all origin nodes have the same list-parent and are sequential.
 * @author Maartje de Jonge
 */
public class OriginSublistTermPrimitive extends AbstractPrimitive {
	
	private static final String NAME = "SSL_EXT_origin_sublist_term";

	public OriginSublistTermPrimitive() {
		super(NAME, 0, 2);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if (!isTermString(tvars[0]) || !(tvars[1] instanceof IStrategoList))//|| (tvars[1].getTermType()!= IStrategoTerm.LIST)
			return false;
		if(tvars[1].getTermType()!= IStrategoTerm.LIST)
			return false;
		IStrategoList list=(IStrategoList)tvars[1];
		if(list.isEmpty())
			return false;
		for (IStrategoTerm child : list.getAllSubterms()) {
			if(!(child instanceof IStrategoTerm))
				return false;
		}
		ISimpleTerm firstChildNode=((IStrategoTerm)list.get(0)).getNode();
		ISimpleTerm commonParentList=getParent(firstChildNode);
		ArrayList<ISimpleTerm> childNodes=commonParentList.getChildren();
		if(!(isTermList(commonParentList)))
			return false;
		int startIndex=-1;
		for (int i = 0; i < childNodes.size(); i++) {
			if(childNodes.get(i)==firstChildNode){
				startIndex=i;
				break;
			}
		}
		for (int i = 0; i < list.size(); i++) {
			if(childNodes.size()<=i+startIndex)
				return false;
			ISimpleTerm childNode=((IStrategoTerm)list.get(i)).getNode();
			if(childNodes.get(i+startIndex)!=childNode)
				return false;
		}
		ISimpleTerm lastChildNode=((IStrategoTerm)list.get(list.size()-1)).getNode();
		IStrategoTerm result = SubListAstNode.createSublist((ListAstNode) commonParentList, firstChildNode, lastChildNode, true); 
		if (result == null) 
			return false;
		env.setCurrent(result);
		return true;
	}
	
}
