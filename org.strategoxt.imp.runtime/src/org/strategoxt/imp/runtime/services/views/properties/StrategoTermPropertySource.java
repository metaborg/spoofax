package org.strategoxt.imp.runtime.services.views.properties;

import java.util.HashMap;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;

/**
 * @author Oskar van Rest
 */
public class StrategoTermPropertySource implements IPropertySource {

	private IStrategoTerm element;
	private IPropertyDescriptor[] propertyDescriptors;
	private HashMap<IStrategoTerm, Object> propertyValues;

	public StrategoTermPropertySource(IStrategoTerm element) {
		super();		
		this.element = element;
		createPropertyDescriptors();
		createPropertyValues();
	}

	private void createPropertyDescriptors() {
		if (element instanceof IStrategoList) {
			IStrategoList e = (IStrategoList) element;
			propertyDescriptors = new IPropertyDescriptor[e.size()];
			
			int i = 0;
			while (!e.isEmpty()) {
				assert e.head() instanceof IStrategoTuple;
				propertyDescriptors[i] = toPropertyDescriptor((IStrategoTuple) e.head());
				e = e.tail();
				i++;
			}
		}
		else if (element instanceof IStrategoTuple) {
			propertyDescriptors = new IPropertyDescriptor[1];
			propertyDescriptors[0] = toPropertyDescriptor((IStrategoTuple) element);
		}
		else {
			propertyDescriptors = new IPropertyDescriptor[0];
		}
	}

	private void createPropertyValues() {
		propertyValues = new HashMap<IStrategoTerm, Object>();
		
		if (element instanceof IStrategoList) {
			IStrategoList e = (IStrategoList) element;
			
			while (!e.isEmpty()) {
				assert e.head() instanceof IStrategoTuple;
				propertyValues.put(e.head(), toPropertyValue((IStrategoTuple) e.head()));
				e = e.tail();
			}
		}
		else if (element instanceof IStrategoTuple) {
			propertyValues.put(element, toPropertyValue((IStrategoTuple) element));
		}
	}

	private IPropertyDescriptor toPropertyDescriptor(IStrategoTuple property) {
		assert property.size() == 2; // we expect (name, value)
		assert property.get(0) instanceof IStrategoString;
		String name = ((IStrategoString) property.get(0)).stringValue();
		return new PropertyDescriptor(property, name);
	}

	private Object toPropertyValue(IStrategoTuple property) {
		assert property.size() == 2;
		assert property.get(1) instanceof IStrategoString || property.get(1) instanceof IStrategoList;
		if (property.get(1) instanceof IStrategoString) {
			return ((IStrategoString) property.get(1)).stringValue();
		}
		else {
			return new StrategoTermPropertySource((IStrategoList) property.get(1));
		}
	}

	public Object getEditableValue() {
		return this;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return propertyDescriptors;
	}

	public Object getPropertyValue(Object id) {
		return propertyValues.get(id);
	}

	public boolean isPropertySet(Object id) {
		// properties don't have a default value
		return false;
	}

	public void resetPropertyValue(Object id) {
		// properties don't have a default value
	}

	public void setPropertyValue(Object id, Object value) {
		// for now, properties are read-only
	}

	@Override
	public String toString() {
		return "";
	}
}
