package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermString;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getImploderOrigin;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.hasImploderOrigin;
import static org.spoofax.terms.Term.isTermList;
import static org.spoofax.terms.attachments.OriginAttachment.tryGetOrigin;
import static org.spoofax.terms.attachments.ParentAttachment.getParent;

import java.util.Arrays;
import java.util.List;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.parser.ast.StrategoSubList;

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
	
	@Override
	public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if (!isTermString(tvars[0]) || !isTermList(tvars[1]))//|| (tvars[1].getTermType()!= IStrategoTerm.LIST)
			return false;
		if(tvars[1].getTermType()!= IStrategoTerm.LIST)
			return false;
		IStrategoList list=(IStrategoList)tvars[1];
		if(list.isEmpty())
			return false;
		for (IStrategoTerm child : list.getAllSubterms()) {
			if(!hasImploderOrigin(child))
				return false;
		}
		IStrategoTerm firstChildNode= tryGetOrigin(list.getSubterm(0));
		IStrategoTerm commonParentList=getParent(firstChildNode);
		if(commonParentList == null)
			return false;
		List<IStrategoTerm> childNodes= Arrays.asList(commonParentList.getAllSubterms());
		if(!(isTermList(commonParentList)))
			return false;
		int startIndex=-1;
		for (int i = 0; i < childNodes.size(); i++) {
			if(childNodes.get(i)==firstChildNode){
				startIndex=i;
				break;
			}
		}
		// XXX: Maartje - many of these primitives don't check hasImploderOrigin() before getting the origin...
		//      What if a term has no origin?!
		//      Before, this could result in a ClassCastException. now, with tryGetOrigin, you just get a non-origin term.
		//      Or with getOrigin()/getImploderOrigin(), you get null
		for (int i = 0; i < list.size(); i++) {
			if(childNodes.size()<=i+startIndex)
				return false;
			IStrategoTerm childNode=getImploderOrigin(list.getSubterm(i));
			if(childNodes.get(i+startIndex)!=childNode)
				return false;
		}
		IStrategoTerm lastChildNode= getImploderOrigin(list.getSubterm(list.size()-1));
		IStrategoTerm result = StrategoSubList.createSublist((IStrategoList) commonParentList, firstChildNode, lastChildNode, true); 
		if (result == null) 
			return false;
		env.setCurrent(result);
		return true;
	}
	
}
